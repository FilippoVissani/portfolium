from datetime import date, timedelta
from typing import TYPE_CHECKING, Dict, Optional

import numpy as np
import pandas as pd
import pyqtgraph as pg
from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QPushButton, QLabel, QSizePolicy,
)
from PySide6.QtCore import QThread, Signal, Qt

from ..theme import ThemeManager

if TYPE_CHECKING:
    from ...controllers.portfolio_controller import PortfolioController

# period label → days (None = special)
_PERIODS: Dict[str, Optional[int]] = {
    "1M": 30, "3M": 90, "6M": 180, "YTD": -1, "1Y": 365, "5Y": 1825, "MAX": None,
}


class _HistWorker(QThread):
    result_ready = Signal(object)

    def __init__(self, controller, start: date, end: date) -> None:
        super().__init__()
        self._ctrl = controller
        self._start = start
        self._end = end

    def run(self) -> None:
        series = self._ctrl.get_investment_historical_performance(self._start, self._end)
        self.result_ready.emit(series)


class HistoricalChartWidget(QWidget):
    """
    MVC View – interactive pyqtgraph line chart of portfolio value over time.
    """

    def __init__(self, controller: "PortfolioController", parent=None) -> None:
        super().__init__(parent)
        self.controller = controller
        self._period = "1Y"
        self._worker: Optional[_HistWorker] = None
        self._last_series: Optional[pd.Series] = None

        layout = QVBoxLayout(self)
        layout.setContentsMargins(0, 4, 0, 0)
        layout.setSpacing(4)

        # ── Header row ────────────────────────────────────────────────── #
        header = QHBoxLayout()
        self._header_title = QLabel("Historical Performance")
        self._header_title.setStyleSheet(
            "font-size: 11pt; font-weight: bold; margin-left: 4px;"
        )
        header.addWidget(self._header_title)
        header.addStretch()

        self._buttons: Dict[str, QPushButton] = {}
        for label in _PERIODS:
            btn = QPushButton(label)
            btn.setCheckable(True)
            btn.setFixedSize(48, 26)
            btn.clicked.connect(lambda _, p=label: self._set_period(p))
            self._buttons[label] = btn
            header.addWidget(btn)

        self._buttons["1Y"].setChecked(True)
        layout.addLayout(header)

        # ── Loading label ─────────────────────────────────────────────── #
        self._loading = QLabel("Loading…")
        self._loading.setAlignment(Qt.AlignCenter)
        self._loading.setVisible(False)
        layout.addWidget(self._loading)

        # ── pyqtgraph chart ────────────────────────────────────────────── #
        c = ThemeManager().colors()
        pg.setConfigOptions(antialias=True, background=c["bg"], foreground=c["text"])
        date_axis = pg.DateAxisItem(orientation="bottom")
        self._plot = pg.PlotWidget(axisItems={"bottom": date_axis})
        self._plot.showGrid(x=True, y=True, alpha=0.15)
        self._plot.getAxis("left").setTextPen(c["text"])
        self._plot.getAxis("bottom").setTextPen(c["text"])
        self._plot.setLabel("left", "Value (€)", color=c["text"])
        self._plot.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        layout.addWidget(self._plot)

        self._update_label_styles()
        ThemeManager().changed.connect(self._on_theme_changed)

    def _update_label_styles(self) -> None:
        c = ThemeManager().colors()
        self._header_title.setStyleSheet(
            f"font-size: 11pt; font-weight: bold; color: {c['text']}; margin-left: 4px;"
        )
        self._loading.setStyleSheet(f"color: {c['subtext']}; font-size: 9pt;")

    def _on_theme_changed(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self._update_label_styles()
        self._plot.setBackground(c["bg"])
        self._plot.getAxis("left").setTextPen(c["text"])
        self._plot.getAxis("bottom").setTextPen(c["text"])
        self._plot.setLabel("left", "Value (€)", color=c["text"])
        if self._last_series is not None:
            self._draw(self._last_series)

    # ------------------------------------------------------------------ #
    # Period control                                                       #
    # ------------------------------------------------------------------ #

    def _set_period(self, period: str) -> None:
        for p, btn in self._buttons.items():
            btn.setChecked(p == period)
        self._period = period
        self.refresh()

    def _date_range(self) -> tuple[date, date]:
        today = date.today()
        days = _PERIODS[self._period]
        if days == -1:
            return date(today.year, 1, 1), today
        if days is None:
            txns = self.controller.portfolio.get_investment_transactions()
            start = txns[0].date if txns else today - timedelta(days=365)
            return start, today
        return today - timedelta(days=days), today

    # ------------------------------------------------------------------ #
    # Refresh                                                              #
    # ------------------------------------------------------------------ #

    def refresh(self) -> None:
        if self._worker and self._worker.isRunning():
            self._worker.quit()
            self._worker.wait()

        start, end = self._date_range()
        self._loading.setVisible(True)
        for btn in self._buttons.values():
            btn.setEnabled(False)

        self._worker = _HistWorker(self.controller, start, end)
        self._worker.result_ready.connect(self._on_result)
        self._worker.start()

    def _on_result(self, series: pd.Series) -> None:
        self._loading.setVisible(False)
        for btn in self._buttons.values():
            btn.setEnabled(True)
        self._draw(series)

    # ------------------------------------------------------------------ #
    # Drawing                                                              #
    # ------------------------------------------------------------------ #

    def _draw(self, series: pd.Series) -> None:
        self._last_series = series
        self._plot.clear()
        c = ThemeManager().colors()

        if series.empty:
            return

        timestamps = np.array(
            [pd.Timestamp(ts).timestamp() for ts in series.index], dtype=np.float64
        )
        values = series.to_numpy(dtype=np.float64)

        mask = ~np.isnan(values)
        timestamps, values = timestamps[mask], values[mask]

        if len(values) < 2:
            return

        color = c["green"] if values[-1] >= values[0] else c["red"]

        main_curve = pg.PlotDataItem(timestamps, values, pen=pg.mkPen(color=color, width=2))
        floor = np.full(len(values), values.min() * 0.998)
        baseline = pg.PlotDataItem(timestamps, floor, pen=None)
        fill = pg.FillBetweenItem(main_curve, baseline, brush=pg.mkBrush(color + "28"))

        self._plot.addItem(main_curve)
        self._plot.addItem(baseline)
        self._plot.addItem(fill)
        self._plot.setXRange(timestamps[0], timestamps[-1], padding=0.01)

from datetime import date, timedelta
from typing import TYPE_CHECKING, Dict, Optional

import numpy as np
import pandas as pd
import pyqtgraph as pg
from PySide6.QtWidgets import (
    QWidget,
    QVBoxLayout,
    QHBoxLayout,
    QPushButton,
    QLabel,
    QSizePolicy,
)
from PySide6.QtCore import QThread, Signal, Qt

if TYPE_CHECKING:
    from ...controllers.portfolio_controller import PortfolioController

_BG = "#1e1e2e"
_TEXT = "#cdd6f4"
_GRID = "#313244"
_GREEN = "#a6e3a1"
_RED = "#f38ba8"

# period label → days (None = special)
_PERIODS: Dict[str, Optional[int]] = {
    "1M": 30,
    "3M": 90,
    "6M": 180,
    "YTD": -1,    # start of current year
    "1Y": 365,
    "5Y": 1825,
    "MAX": None,  # earliest transaction
}


class _HistWorker(QThread):
    """Background thread that computes historical portfolio values."""

    result_ready = Signal(object)  # pd.Series

    def __init__(
        self,
        controller: "PortfolioController",
        start: date,
        end: date,
    ) -> None:
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
    Period selector buttons (1M / 3M / 6M / YTD / 1Y / 5Y / MAX) trigger
    an async data fetch so the UI remains responsive.
    """

    def __init__(self, controller: "PortfolioController", parent=None) -> None:
        super().__init__(parent)
        self.controller = controller
        self._period = "1Y"
        self._worker: Optional[_HistWorker] = None

        layout = QVBoxLayout(self)
        layout.setContentsMargins(0, 4, 0, 0)
        layout.setSpacing(4)

        # ── Header row ────────────────────────────────────────────────── #
        header = QHBoxLayout()
        title = QLabel("Historical Performance")
        title.setStyleSheet(
            "font-size: 11pt; font-weight: bold; color: #cdd6f4; margin-left: 4px;"
        )
        header.addWidget(title)
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

        # ── Loading label (hidden by default) ─────────────────────────── #
        self._loading = QLabel("Loading…")
        self._loading.setAlignment(Qt.AlignCenter)
        self._loading.setStyleSheet("color: #6c7086; font-size: 9pt;")
        self._loading.setVisible(False)
        layout.addWidget(self._loading)

        # ── pyqtgraph chart ────────────────────────────────────────────── #
        pg.setConfigOptions(antialias=True, background=_BG, foreground=_TEXT)
        date_axis = pg.DateAxisItem(orientation="bottom")
        self._plot = pg.PlotWidget(axisItems={"bottom": date_axis})
        self._plot.showGrid(x=True, y=True, alpha=0.15)
        self._plot.getAxis("left").setTextPen(_TEXT)
        self._plot.getAxis("bottom").setTextPen(_TEXT)
        self._plot.setLabel("left", "Value (€)", color=_TEXT)
        self._plot.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        layout.addWidget(self._plot)

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

        if days == -1:          # YTD
            return date(today.year, 1, 1), today
        if days is None:        # MAX
            txns = self.controller.portfolio.get_investment_transactions()
            start = txns[0].date if txns else today - timedelta(days=365)
            return start, today
        return today - timedelta(days=days), today

    # ------------------------------------------------------------------ #
    # Refresh                                                              #
    # ------------------------------------------------------------------ #

    def refresh(self) -> None:
        """Kick off an async fetch; updates chart when data arrives."""
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
        self._plot.clear()

        if series.empty:
            return

        timestamps = np.array(
            [pd.Timestamp(ts).timestamp() for ts in series.index], dtype=np.float64
        )
        values = series.to_numpy(dtype=np.float64)

        # Strip NaN
        mask = ~np.isnan(values)
        timestamps, values = timestamps[mask], values[mask]

        if len(values) < 2:
            return

        color = _GREEN if values[-1] >= values[0] else _RED

        main_curve = pg.PlotDataItem(
            timestamps, values, pen=pg.mkPen(color=color, width=2)
        )
        floor = np.full(len(values), values.min() * 0.998)
        baseline = pg.PlotDataItem(timestamps, floor, pen=None)
        fill = pg.FillBetweenItem(
            main_curve, baseline, brush=pg.mkBrush(color + "28")
        )

        self._plot.addItem(main_curve)
        self._plot.addItem(baseline)
        self._plot.addItem(fill)
        self._plot.setXRange(timestamps[0], timestamps[-1], padding=0.01)

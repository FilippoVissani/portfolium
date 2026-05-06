from typing import Optional

from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QFrame, QHBoxLayout, QLabel, QProgressBar, QSizePolicy,
    QSplitter, QTableWidget, QTableWidgetItem, QVBoxLayout, QWidget, QHeaderView,
)

from ...controllers.portfolio_controller import EmergencyFundStatus, PortfolioController
from ..theme import ThemeManager


class _KpiCard(QFrame):
    def __init__(self, title: str) -> None:
        super().__init__()
        layout = QVBoxLayout(self)
        layout.setContentsMargins(12, 10, 12, 10)
        layout.setSpacing(2)

        self._title_lbl = QLabel(title)
        self._value_lbl = QLabel("-")

        layout.addWidget(self._title_lbl)
        layout.addWidget(self._value_lbl)

        self._apply_theme(ThemeManager().current)
        ThemeManager().changed.connect(self._apply_theme)

    def _apply_theme(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self.setStyleSheet(f"background-color: {c['bg_alt']}; border-radius: 8px;")
        self._title_lbl.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt;")
        self._value_lbl.setStyleSheet(f"color: {c['text']}; font-size: 12pt; font-weight: bold;")

    def set_value(self, text: str, color: Optional[str] = None) -> None:
        c = ThemeManager().colors()
        style = "font-size: 12pt; font-weight: bold;"
        style += f" color: {color if color else c['text']};"
        self._value_lbl.setStyleSheet(style)
        self._value_lbl.setText(text)


class _FundProgressCard(QFrame):
    """Large card that shows the overall emergency-fund coverage with a progress bar."""

    def __init__(self) -> None:
        super().__init__()
        self.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Fixed)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 16, 20, 16)
        layout.setSpacing(10)

        title_row = QHBoxLayout()
        self._title_lbl = QLabel("Emergency Fund Coverage")
        self._title_lbl.setStyleSheet("font-size: 11pt; font-weight: bold;")
        self._status_badge = QLabel()
        self._status_badge.setStyleSheet(
            "border-radius: 6px; padding: 2px 8px; font-size: 8pt; font-weight: bold;"
        )
        title_row.addWidget(self._title_lbl)
        title_row.addStretch()
        title_row.addWidget(self._status_badge)
        layout.addLayout(title_row)

        self._bar = QProgressBar()
        self._bar.setRange(0, 100)
        self._bar.setTextVisible(False)
        self._bar.setFixedHeight(14)
        layout.addWidget(self._bar)

        figures = QHBoxLayout()
        self._saved_label = QLabel()
        self._pct_label = QLabel()
        self._target_label = QLabel()
        figures.addWidget(self._saved_label)
        figures.addStretch()
        figures.addWidget(self._pct_label)
        figures.addStretch()
        figures.addWidget(self._target_label)
        layout.addLayout(figures)

        self._apply_theme(ThemeManager().current)
        ThemeManager().changed.connect(self._apply_theme)

    def _apply_theme(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self.setStyleSheet(f"background-color: {c['bg_alt']}; border-radius: 10px;")
        self._title_lbl.setStyleSheet(f"color: {c['text']}; font-size: 11pt; font-weight: bold;")
        self._saved_label.setStyleSheet(f"color: {c['text']}; font-size: 9pt;")
        self._pct_label.setStyleSheet(f"color: {c['text']}; font-size: 9pt; font-weight: bold;")
        self._target_label.setStyleSheet(f"color: {c['subtext']}; font-size: 9pt;")
        self._bar.setStyleSheet(
            f"QProgressBar {{ background-color: {c['surface']}; border-radius: 7px; }}"
            f"QProgressBar::chunk {{ background-color: {c['accent']}; border-radius: 7px; }}"
        )

    def update_data(self, status: EmergencyFundStatus) -> None:
        c = ThemeManager().colors()
        pct = int(status.coverage_pct)
        self._bar.setValue(pct)

        if status.is_funded or status.coverage_pct >= 75:
            chunk_color = c["green"]
            badge_text = "FULLY FUNDED" if status.is_funded else "NEARLY FUNDED"
            badge_style = f"background: {c['bg']}; color: {c['green']};"
        elif status.coverage_pct >= 40:
            chunk_color = c["yellow"]
            badge_text = "IN PROGRESS"
            badge_style = f"background: {c['bg']}; color: {c['yellow']};"
        else:
            chunk_color = c["red"]
            badge_text = "UNDERFUNDED"
            badge_style = f"background: {c['bg']}; color: {c['red']};"

        self._bar.setStyleSheet(
            f"QProgressBar {{ background-color: {c['surface']}; border-radius: 7px; }}"
            f"QProgressBar::chunk {{ background-color: {chunk_color}; border-radius: 7px; }}"
        )
        self._status_badge.setText(badge_text)
        self._status_badge.setStyleSheet(
            f"border-radius: 6px; padding: 2px 8px; font-size: 8pt; font-weight: bold; {badge_style}"
        )
        self._saved_label.setText(f"Saved: €{status.current_value:,.2f}")
        self._pct_label.setText(f"{pct}%")
        if status.target_capital is not None:
            self._target_label.setText(f"Target: €{status.target_capital:,.2f}")
        else:
            self._target_label.setText("No target set")


class _AllocationPieChart(QWidget):
    def __init__(self) -> None:
        super().__init__()
        c = ThemeManager().colors()
        self._fig = Figure(figsize=(4, 4), facecolor=c["bg"])
        self._ax = self._fig.add_subplot(111)
        self._canvas = FigureCanvas(self._fig)
        layout = QVBoxLayout(self)
        layout.setContentsMargins(0, 0, 0, 0)
        layout.addWidget(self._canvas)
        self.setMinimumHeight(220)
        self._last_data: dict = {}
        ThemeManager().changed.connect(lambda _: self.update_data(self._last_data))

    def update_data(self, data: dict) -> None:
        self._last_data = data
        c = ThemeManager().colors()
        palette = c["palette"]
        self._fig.set_facecolor(c["bg"])
        self._ax.clear()
        if not data:
            self._canvas.draw()
            return
        labels = list(data.keys())
        sizes = list(data.values())
        colors = [palette[i % len(palette)] for i in range(len(labels))]
        self._ax.pie(
            sizes, labels=labels, colors=colors, autopct="%1.1f%%",
            textprops={"color": "#000000", "fontsize": 8}, startangle=90,  # Dark text
        )
        self._ax.set_facecolor(c["bg"])
        self._canvas.draw()


class EmergencyFundPage(QWidget):
    """MVC View – Emergency Fund tab page."""

    def __init__(self, controller: PortfolioController) -> None:
        super().__init__()
        self._ctrl = controller
        self._build_ui()

    def _build_ui(self) -> None:
        root = QVBoxLayout(self)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        kpi_row = QHBoxLayout()
        kpi_row.setSpacing(12)
        self._kpi_value    = _KpiCard("Current Value")
        self._kpi_target   = _KpiCard("Target Capital")
        self._kpi_coverage = _KpiCard("Coverage")
        self._kpi_remaining= _KpiCard("Still Needed")
        self._kpi_cash     = _KpiCard("Cash Available")
        for card in (self._kpi_value, self._kpi_target, self._kpi_coverage,
                     self._kpi_remaining, self._kpi_cash):
            kpi_row.addWidget(card, 1)
        root.addLayout(kpi_row)

        self._progress_card = _FundProgressCard()
        root.addWidget(self._progress_card)

        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setChildrenCollapsible(False)

        self._pie = _AllocationPieChart()
        splitter.addWidget(self._pie)

        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(0, 0, 0, 0)
        right_layout.setSpacing(6)
        right_layout.addWidget(_section_label("Holdings"))
        self._table = _build_holdings_table()
        right_layout.addWidget(self._table)
        splitter.addWidget(right)

        splitter.setSizes([350, 650])
        root.addWidget(splitter, 3)

    def refresh(self) -> None:
        c = ThemeManager().colors()
        status = self._ctrl.get_emergency_fund_status()
        asset_infos = self._ctrl.get_emergency_asset_infos()
        cash = self._ctrl.get_emergency_cash_balance()
        allocation = self._ctrl.get_emergency_allocation_data()

        cov_color = (
            c["green"] if status.is_funded or status.coverage_pct >= 75
            else c["yellow"] if status.coverage_pct >= 40
            else c["red"]
        )
        self._kpi_value.set_value(f"€{status.current_value:,.2f}")
        self._kpi_target.set_value(
            f"€{status.target_capital:,.2f}" if status.target_capital else "—"
        )
        self._kpi_coverage.set_value(f"{status.coverage_pct:.1f}%", cov_color)
        self._kpi_remaining.set_value(f"€{status.remaining_amount:,.2f}")
        self._kpi_cash.set_value(f"€{cash:,.2f}")
        self._progress_card.update_data(status)
        self._pie.update_data(allocation)

        self._table.setRowCount(len(asset_infos))
        for row, info in enumerate(asset_infos):
            gl_color      = c["green"] if info.gain_loss_eur >= 0 else c["red"]
            intraday_color= c["green"] if info.intraday_gain_loss_eur >= 0 else c["red"]
            _set_cell(self._table, row, 0, info.name)
            _set_cell(self._table, row, 1, info.symbol)
            _set_cell(self._table, row, 2, f"{info.current_price:,.2f}")
            _set_cell(self._table, row, 3, f"{info.quantity:,.4f}")
            _set_cell(self._table, row, 4, f"{info.gain_loss_eur:+,.2f}", gl_color)
            _set_cell(self._table, row, 5, f"{info.gain_loss_pct:+.2f}%", gl_color)
            _set_cell(self._table, row, 6, f"{info.intraday_gain_loss_eur:+,.2f}", intraday_color)


# ── Helpers ──────────────────────────────────────────────────────────────── #

def _section_label(text: str) -> QLabel:
    lbl = QLabel(text)
    c = ThemeManager().colors()
    lbl.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt; font-weight: bold;")
    return lbl


def _build_holdings_table() -> QTableWidget:
    columns = ["Name", "Ticker", "Price (€)", "Qty", "G/L (€)", "G/L (%)", "Intraday G/L (€)"]
    table = QTableWidget(0, len(columns))
    table.setHorizontalHeaderLabels(columns)
    table.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
    table.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
    table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
    for col in range(1, len(columns)):
        table.horizontalHeader().setSectionResizeMode(col, QHeaderView.ResizeMode.ResizeToContents)
    table.verticalHeader().setVisible(False)
    return table


def _set_cell(
    table: QTableWidget, row: int, col: int, text: str, color: Optional[str] = None
) -> None:
    item = QTableWidgetItem(text)
    item.setTextAlignment(Qt.AlignmentFlag.AlignRight | Qt.AlignmentFlag.AlignVCenter)
    if color:
        from PySide6.QtGui import QColor

        item.setForeground(QColor(color))
    table.setItem(row, col, item)

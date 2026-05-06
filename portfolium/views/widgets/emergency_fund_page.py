from typing import Optional

from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QFrame,
    QHBoxLayout,
    QLabel,
    QProgressBar,
    QSizePolicy,
    QSplitter,
    QTableWidget,
    QTableWidgetItem,
    QVBoxLayout,
    QWidget,
    QHeaderView,
)

from ...controllers.portfolio_controller import EmergencyFundStatus, PortfolioController

_BG = "#1e1e2e"
_TEXT = "#cdd6f4"
_GREEN = "#a6e3a1"
_RED = "#f38ba8"
_YELLOW = "#f9e2af"
_BLUE = "#89b4fa"
_PALETTE = ["#89b4fa", "#a6e3a1", "#fab387", "#f38ba8", "#cba6f7", "#94e2d5"]


class _KpiCard(QFrame):
    def __init__(self, title: str) -> None:
        super().__init__()
        self.setStyleSheet("background-color: #181825; border-radius: 8px;")
        layout = QVBoxLayout(self)
        layout.setContentsMargins(12, 10, 12, 10)
        layout.setSpacing(2)

        self._title = QLabel(title)
        self._title.setStyleSheet("color: #6c7086; font-size: 8pt;")
        self._value = QLabel("-")
        self._value.setStyleSheet(f"color: {_TEXT}; font-size: 12pt; font-weight: bold;")

        layout.addWidget(self._title)
        layout.addWidget(self._value)

    def set_value(self, text: str, color: Optional[str] = None) -> None:
        style = "font-size: 12pt; font-weight: bold;"
        style += f" color: {color if color else _TEXT};"
        self._value.setStyleSheet(style)
        self._value.setText(text)


class _FundProgressCard(QFrame):
    """Large card that shows the overall emergency-fund coverage with a progress bar."""

    def __init__(self) -> None:
        super().__init__()
        self.setStyleSheet("background-color: #181825; border-radius: 10px;")
        self.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Fixed)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 16, 20, 16)
        layout.setSpacing(10)

        # Title row
        title_row = QHBoxLayout()
        self._title = QLabel("Emergency Fund Coverage")
        self._title.setStyleSheet(f"color: {_TEXT}; font-size: 11pt; font-weight: bold;")
        self._status_badge = QLabel()
        self._status_badge.setStyleSheet(
            "border-radius: 6px; padding: 2px 8px; font-size: 8pt; font-weight: bold;"
        )
        title_row.addWidget(self._title)
        title_row.addStretch()
        title_row.addWidget(self._status_badge)
        layout.addLayout(title_row)

        # Progress bar
        self._bar = QProgressBar()
        self._bar.setRange(0, 100)
        self._bar.setTextVisible(False)
        self._bar.setFixedHeight(14)
        self._bar.setStyleSheet(
            "QProgressBar { background-color: #313244; border-radius: 7px; }"
            f"QProgressBar::chunk {{ background-color: {_BLUE}; border-radius: 7px; }}"
        )
        layout.addWidget(self._bar)

        # Bottom figures row
        figures = QHBoxLayout()
        self._saved_label = QLabel()
        self._saved_label.setStyleSheet(f"color: {_TEXT}; font-size: 9pt;")
        self._pct_label = QLabel()
        self._pct_label.setStyleSheet(f"color: {_TEXT}; font-size: 9pt; font-weight: bold;")
        self._target_label = QLabel()
        self._target_label.setStyleSheet("color: #6c7086; font-size: 9pt;")
        figures.addWidget(self._saved_label)
        figures.addStretch()
        figures.addWidget(self._pct_label)
        figures.addStretch()
        figures.addWidget(self._target_label)
        layout.addLayout(figures)

    def update_data(self, status: EmergencyFundStatus) -> None:
        pct = int(status.coverage_pct)
        self._bar.setValue(pct)

        if status.is_funded:
            chunk_color = _GREEN
            badge_text = "FULLY FUNDED"
            badge_style = f"background: #1e3a2a; color: {_GREEN};"
        elif status.coverage_pct >= 75:
            chunk_color = _GREEN
            badge_text = "NEARLY FUNDED"
            badge_style = f"background: #1e3a2a; color: {_GREEN};"
        elif status.coverage_pct >= 40:
            chunk_color = _YELLOW
            badge_text = "IN PROGRESS"
            badge_style = f"background: #3a2e1e; color: {_YELLOW};"
        else:
            chunk_color = _RED
            badge_text = "UNDERFUNDED"
            badge_style = f"background: #3a1e1e; color: {_RED};"

        self._bar.setStyleSheet(
            "QProgressBar { background-color: #313244; border-radius: 7px; }"
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
        fig = Figure(figsize=(4, 4), facecolor=_BG)
        self._ax = fig.add_subplot(111)
        self._canvas = FigureCanvas(fig)
        layout = QVBoxLayout(self)
        layout.setContentsMargins(0, 0, 0, 0)
        layout.addWidget(self._canvas)
        self.setMinimumHeight(220)

    def update_data(self, data: dict) -> None:
        self._ax.clear()
        if not data:
            self._canvas.draw()
            return
        labels = list(data.keys())
        sizes = list(data.values())
        colors = [_PALETTE[i % len(_PALETTE)] for i in range(len(labels))]
        self._ax.pie(
            sizes,
            labels=labels,
            colors=colors,
            autopct="%1.1f%%",
            textprops={"color": _TEXT, "fontsize": 8},
            startangle=90,
        )
        self._ax.set_facecolor(_BG)
        self._canvas.draw()


class EmergencyFundPage(QWidget):
    """
    MVC View – Emergency Fund tab page.

    Layout:
      • KPI cards row (current value, target, coverage %, remaining, cash)
      • Large fund-progress card with labelled progress bar
      • QSplitter: left = allocation pie | right = holdings table
    """

    def __init__(self, controller: PortfolioController) -> None:
        super().__init__()
        self._ctrl = controller
        self._build_ui()

    # ------------------------------------------------------------------ #
    # UI construction                                                      #
    # ------------------------------------------------------------------ #

    def _build_ui(self) -> None:
        root = QVBoxLayout(self)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        # ── KPI cards ──────────────────────────────────────────────────── #
        kpi_row = QHBoxLayout()
        kpi_row.setSpacing(12)
        self._kpi_value = _KpiCard("Current Value")
        self._kpi_target = _KpiCard("Target Capital")
        self._kpi_coverage = _KpiCard("Coverage")
        self._kpi_remaining = _KpiCard("Still Needed")
        self._kpi_cash = _KpiCard("Cash Available")
        for card in (
            self._kpi_value,
            self._kpi_target,
            self._kpi_coverage,
            self._kpi_remaining,
            self._kpi_cash,
        ):
            kpi_row.addWidget(card, 1)
        root.addLayout(kpi_row)

        # ── Progress card ──────────────────────────────────────────────── #
        self._progress_card = _FundProgressCard()
        root.addWidget(self._progress_card)

        # ── Middle: pie chart (left) + holdings table (right) ─────────── #
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

    # ------------------------------------------------------------------ #
    # Refresh                                                              #
    # ------------------------------------------------------------------ #

    def refresh(self) -> None:
        status = self._ctrl.get_emergency_fund_status()
        asset_infos = self._ctrl.get_emergency_asset_infos()
        cash = self._ctrl.get_emergency_cash_balance()
        allocation = self._ctrl.get_emergency_allocation_data()

        # KPI cards
        cov_color = (
            _GREEN if status.is_funded or status.coverage_pct >= 75
            else _YELLOW if status.coverage_pct >= 40
            else _RED
        )
        self._kpi_value.set_value(f"€{status.current_value:,.2f}")
        self._kpi_target.set_value(
            f"€{status.target_capital:,.2f}" if status.target_capital else "—"
        )
        self._kpi_coverage.set_value(f"{status.coverage_pct:.1f}%", cov_color)
        self._kpi_remaining.set_value(f"€{status.remaining_amount:,.2f}")
        self._kpi_cash.set_value(f"€{cash:,.2f}")

        # Progress card
        self._progress_card.update_data(status)

        # Pie chart
        self._pie.update_data(allocation)

        # Holdings table
        self._table.setRowCount(len(asset_infos))
        for row, info in enumerate(asset_infos):
            gl_color = _GREEN if info.gain_loss_eur >= 0 else _RED
            intraday_color = _GREEN if info.intraday_gain_loss_eur >= 0 else _RED
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
    lbl.setStyleSheet("color: #6c7086; font-size: 8pt; font-weight: bold;")
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

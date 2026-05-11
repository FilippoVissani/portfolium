from typing import Optional

from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QFrame,
    QHBoxLayout,
    QLabel,
    QProgressBar,
    QScrollArea,
    QSizePolicy,
    QSplitter,
    QTableWidget,
    QTableWidgetItem,
    QVBoxLayout,
    QWidget,
    QHeaderView,
)

from ...controllers.portfolio_controller import (
    PlannedExpenseProgress,
    PortfolioController,
)
from ..theme import ThemeManager
from .pie_chart_utils import render_pie_chart


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
        self._value_lbl.setStyleSheet(
            f"color: {c['text']}; font-size: 12pt; font-weight: bold;"
        )

    def set_value(self, text: str, color: Optional[str] = None) -> None:
        c = ThemeManager().colors()
        style = "font-size: 12pt; font-weight: bold;"
        style += f" color: {color if color else c['text']};"
        self._value_lbl.setStyleSheet(style)
        self._value_lbl.setText(text)


class _GoalCard(QFrame):
    """Compact card showing a single planned-expense goal with a progress bar."""

    def __init__(self) -> None:
        super().__init__()
        self.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Fixed)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(14, 12, 14, 12)
        layout.setSpacing(6)

        header = QHBoxLayout()
        self._name_label = QLabel()
        self._deadline_label = QLabel()
        header.addWidget(self._name_label)
        header.addStretch()
        header.addWidget(self._deadline_label)
        layout.addLayout(header)

        self._progress_bar = QProgressBar()
        self._progress_bar.setRange(0, 100)
        self._progress_bar.setTextVisible(False)
        self._progress_bar.setFixedHeight(8)
        layout.addWidget(self._progress_bar)

        bottom = QHBoxLayout()
        self._pct_label = QLabel()
        self._amounts_label = QLabel()
        self._days_label = QLabel()
        bottom.addWidget(self._pct_label)
        bottom.addStretch()
        bottom.addWidget(self._amounts_label)
        bottom.addStretch()
        bottom.addWidget(self._days_label)
        layout.addLayout(bottom)

        self._apply_theme(ThemeManager().current)
        ThemeManager().changed.connect(self._apply_theme)

    def _apply_theme(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self.setStyleSheet(f"background-color: {c['bg_alt']}; border-radius: 8px;")
        self._name_label.setStyleSheet(
            f"color: {c['text']}; font-size: 10pt; font-weight: bold;"
        )
        self._deadline_label.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt;")
        self._pct_label.setStyleSheet(f"color: {c['text']}; font-size: 8pt;")
        self._amounts_label.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt;")
        self._progress_bar.setStyleSheet(
            f"QProgressBar {{ background-color: {c['surface']}; border-radius: 4px; }}"
            f"QProgressBar::chunk {{ background-color: {c['accent']}; border-radius: 4px; }}"
        )

    def update_data(self, goal: PlannedExpenseProgress) -> None:
        c = ThemeManager().colors()
        self._name_label.setText(goal.name)
        self._deadline_label.setText(
            f"Due: {goal.expiration_date.strftime('%d %b %Y')}"
        )

        pct = int(goal.progress_pct)
        self._progress_bar.setValue(pct)

        if goal.is_overdue:
            chunk_color = c["red"]
        elif goal.is_completed or goal.progress_pct >= 75:
            chunk_color = c["green"]
        elif goal.progress_pct >= 40:
            chunk_color = c["yellow"]
        else:
            chunk_color = c["accent"]

        self._progress_bar.setStyleSheet(
            f"QProgressBar {{ background-color: {c['surface']}; border-radius: 4px; }}"
            f"QProgressBar::chunk {{ background-color: {chunk_color}; border-radius: 4px; }}"
        )

        self._pct_label.setText(f"{pct}% funded")
        self._amounts_label.setText(
            f"€{goal.total_saved:,.0f} / €{goal.estimated_amount:,.0f}"
        )

        if goal.is_completed:
            days_text = "Goal reached!"
            days_color = c["green"]
        elif goal.is_overdue:
            days_text = "Overdue!"
            days_color = c["red"]
        else:
            days_text = f"{goal.days_remaining} days left"
            days_color = c["subtext"]
        self._days_label.setStyleSheet(f"color: {days_color}; font-size: 8pt;")
        self._days_label.setText(days_text)


class _AllocationPieChart(QWidget):
    """Matplotlib pie chart for planned account allocation."""

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
        self._fig.set_facecolor(c["bg"])
        render_pie_chart(
            self._ax,
            data,
            c,
            title="Allocation",
            empty_text="No allocation data",
        )
        self._canvas.draw()


class PlannedExpensesPage(QWidget):
    """MVC View – Planned Expenses tab page."""

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
        self._kpi_saved = _KpiCard("Total Saved")
        self._kpi_needed = _KpiCard("Total Needed")
        self._kpi_coverage = _KpiCard("Overall Coverage")
        self._kpi_cash = _KpiCard("Cash Available")
        self._kpi_goals = _KpiCard("Goals")
        for card in (
            self._kpi_saved,
            self._kpi_needed,
            self._kpi_coverage,
            self._kpi_cash,
            self._kpi_goals,
        ):
            kpi_row.addWidget(card, 1)
        root.addLayout(kpi_row)

        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setChildrenCollapsible(False)

        self._scroll = QScrollArea()
        self._scroll.setWidgetResizable(True)
        self._scroll.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff)
        self._goals_container = QWidget()
        self._goals_layout = QVBoxLayout(self._goals_container)
        self._goals_layout.setContentsMargins(0, 0, 0, 0)
        self._goals_layout.setSpacing(8)
        self._goals_layout.addStretch()
        self._scroll.setWidget(self._goals_container)
        splitter.addWidget(self._scroll)

        self._pie = _AllocationPieChart()
        splitter.addWidget(self._pie)
        splitter.setSizes([500, 300])
        root.addWidget(splitter, 3)

        root.addWidget(_section_label("Holdings"))
        self._table = _build_holdings_table()
        root.addWidget(self._table, 2)

        self._apply_container_theme(ThemeManager().current)
        ThemeManager().changed.connect(self._apply_container_theme)

    def _apply_container_theme(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self._scroll.setStyleSheet(
            f"QScrollArea {{ background: {c['bg']}; border: none; }}"
        )
        self._goals_container.setStyleSheet(f"background: {c['bg']};")

    def refresh(self) -> None:
        c = ThemeManager().colors()
        goals = self._ctrl.get_planned_expense_progress()
        asset_infos = self._ctrl.get_planned_asset_infos()
        cash = self._ctrl.get_planned_cash_balance()
        total_saved = self._ctrl.get_total_planned_value()
        total_needed = sum(g.estimated_amount for g in goals)
        allocation = self._ctrl.get_planned_allocation_data()

        coverage = (total_saved / total_needed * 100) if total_needed else 0.0
        cov_color = (
            c["green"]
            if coverage >= 75
            else c["yellow"]
            if coverage >= 40
            else c["red"]
        )
        self._kpi_saved.set_value(f"€{total_saved:,.2f}")
        self._kpi_needed.set_value(f"€{total_needed:,.2f}")
        self._kpi_coverage.set_value(f"{coverage:.1f}%", cov_color)
        self._kpi_cash.set_value(f"€{cash:,.2f}")
        self._kpi_goals.set_value(str(len(goals)))

        while self._goals_layout.count() > 1:
            item = self._goals_layout.takeAt(0)
            if item.widget():
                item.widget().deleteLater()

        for goal in goals:
            card = _GoalCard()
            card.update_data(goal)
            self._goals_layout.insertWidget(self._goals_layout.count() - 1, card)

        self._pie.update_data(allocation)

        self._table.setRowCount(len(asset_infos))
        for row, info in enumerate(asset_infos):
            gl_color = c["green"] if info.gain_loss_eur >= 0 else c["red"]
            intraday_color = (
                c["green"] if info.intraday_gain_loss_eur >= 0 else c["red"]
            )
            _set_cell(self._table, row, 0, info.name)
            _set_cell(self._table, row, 1, info.symbol)
            _set_cell(self._table, row, 2, f"{info.current_price:,.2f}")
            _set_cell(self._table, row, 3, f"{info.quantity:,.4f}")
            _set_cell(self._table, row, 4, f"{info.gain_loss_eur:+,.2f}", gl_color)
            _set_cell(self._table, row, 5, f"{info.gain_loss_pct:+.2f}%", gl_color)
            _set_cell(
                self._table,
                row,
                6,
                f"{info.intraday_gain_loss_eur:+,.2f}",
                intraday_color,
            )


# ── Helpers ──────────────────────────────────────────────────────────────── #


def _section_label(text: str) -> QLabel:
    lbl = QLabel(text)
    c = ThemeManager().colors()
    lbl.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt; font-weight: bold;")
    return lbl


def _build_holdings_table() -> QTableWidget:
    columns = [
        "Name",
        "Ticker",
        "Price (€)",
        "Qty",
        "G/L (€)",
        "G/L (%)",
        "Intraday G/L (€)",
    ]
    table = QTableWidget(0, len(columns))
    table.setHorizontalHeaderLabels(columns)
    table.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
    table.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
    table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
    for col in range(1, len(columns)):
        table.horizontalHeader().setSectionResizeMode(
            col, QHeaderView.ResizeMode.ResizeToContents
        )
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

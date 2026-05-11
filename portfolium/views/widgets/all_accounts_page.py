from typing import Optional

from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QFrame,
    QHBoxLayout,
    QLabel,
    QSplitter,
    QTableWidget,
    QTableWidgetItem,
    QVBoxLayout,
    QWidget,
    QHeaderView,
)

from ...controllers.portfolio_controller import (
    AggregatedAccountInfo,
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


class _AllocationPieChart(QWidget):
    def __init__(self, title: str, empty_text: str) -> None:
        super().__init__()
        self._title = title
        self._empty_text = empty_text
        c = ThemeManager().colors()
        self._fig = Figure(figsize=(4, 4), facecolor=c["bg"])
        self._ax = self._fig.add_subplot(111)
        self._canvas = FigureCanvas(self._fig)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(0, 0, 0, 0)
        layout.addWidget(self._canvas)

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
            title=self._title,
            empty_text=self._empty_text,
        )
        self._canvas.draw()


class AllAccountsPage(QWidget):
    """Dashboard tab aggregating all accounts into one overview."""

    def __init__(self, controller: PortfolioController) -> None:
        super().__init__()
        self._ctrl = controller
        self._build_ui()

    def _build_ui(self) -> None:
        root = QVBoxLayout(self)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        title = QLabel("All Accounts Overview")
        title.setStyleSheet(
            f"font-size: 12pt; font-weight: bold; color: {ThemeManager().colors()['text']};"
        )
        root.addWidget(title)
        self._title = title

        kpi_row = QHBoxLayout()
        kpi_row.setSpacing(12)
        self._kpi_total = _KpiCard("Total Value")
        self._kpi_cash = _KpiCard("Total Cash")
        self._kpi_assets = _KpiCard("Invested Assets")
        self._kpi_accounts = _KpiCard("Accounts")
        self._kpi_types = _KpiCard("Account Types")
        for card in (
            self._kpi_total,
            self._kpi_cash,
            self._kpi_assets,
            self._kpi_accounts,
            self._kpi_types,
        ):
            kpi_row.addWidget(card, 1)
        root.addLayout(kpi_row)

        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setChildrenCollapsible(False)

        left = QWidget()
        left_layout = QVBoxLayout(left)
        left_layout.setContentsMargins(0, 0, 0, 0)
        left_layout.setSpacing(8)
        left_layout.addWidget(_section_label("Allocation by Type"))
        self._pie_by_type = _AllocationPieChart(
            title="Value by Account Type",
            empty_text="No account data",
        )
        left_layout.addWidget(self._pie_by_type)

        left_layout.addWidget(_section_label("Allocation by Asset + Cash"))
        self._pie_allocation = _AllocationPieChart(
            title="All Assets and Cash",
            empty_text="No holdings or cash",
        )
        left_layout.addWidget(self._pie_allocation)

        splitter.addWidget(left)

        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(0, 0, 0, 0)
        right_layout.setSpacing(6)
        right_layout.addWidget(_section_label("Account Breakdown"))
        self._table = _build_table()
        right_layout.addWidget(self._table)
        splitter.addWidget(right)

        splitter.setSizes([380, 620])
        root.addWidget(splitter, 1)

        ThemeManager().changed.connect(self._on_theme_changed)

    def _on_theme_changed(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self._title.setStyleSheet(
            f"font-size: 12pt; font-weight: bold; color: {c['text']};"
        )

    def refresh(self) -> None:
        infos = self._ctrl.get_all_accounts_infos()
        total_value = self._ctrl.get_all_accounts_total_value()
        total_cash = self._ctrl.get_all_accounts_total_cash()
        total_assets = self._ctrl.get_all_accounts_total_assets_value()
        type_counts = self._ctrl.get_all_accounts_type_counts()
        non_empty_types = sum(1 for count in type_counts.values() if count > 0)

        self._kpi_total.set_value(f"€{total_value:,.2f}")
        self._kpi_cash.set_value(f"€{total_cash:,.2f}")
        self._kpi_assets.set_value(f"€{total_assets:,.2f}")
        self._kpi_accounts.set_value(str(len(infos)))
        self._kpi_types.set_value(str(non_empty_types))

        self._pie_by_type.update_data(self._ctrl.get_all_accounts_breakdown_by_type())
        self._pie_allocation.update_data(self._ctrl.get_all_accounts_allocation_data())
        self._fill_table(infos, total_value)

    def _fill_table(
        self, infos: list[AggregatedAccountInfo], total_value: float
    ) -> None:
        c = ThemeManager().colors()
        self._table.setRowCount(len(infos))

        for row, info in enumerate(infos):
            share = (info.total_value / total_value * 100) if total_value else 0.0
            total_color = c["green"] if info.total_value >= 0 else c["red"]

            _set_cell(self._table, row, 0, info.name, align_right=False)
            _set_cell(
                self._table, row, 1, info.account_type.capitalize(), align_right=False
            )
            _set_cell(self._table, row, 2, f"{info.cash_balance:,.2f}")
            _set_cell(self._table, row, 3, f"{info.assets_value:,.2f}")
            _set_cell(self._table, row, 4, f"{info.total_value:,.2f}", total_color)
            _set_cell(self._table, row, 5, f"{share:.2f}%")


def _section_label(text: str) -> QLabel:
    lbl = QLabel(text)
    c = ThemeManager().colors()
    lbl.setStyleSheet(f"color: {c['subtext']}; font-size: 8pt; font-weight: bold;")
    return lbl


def _build_table() -> QTableWidget:
    columns = [
        "Account",
        "Type",
        "Cash (€)",
        "Assets (€)",
        "Total (€)",
        "Share (%)",
    ]
    table = QTableWidget(0, len(columns))
    table.setHorizontalHeaderLabels(columns)
    table.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
    table.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
    table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
    table.horizontalHeader().setSectionResizeMode(
        1, QHeaderView.ResizeMode.ResizeToContents
    )
    for col in range(2, len(columns)):
        table.horizontalHeader().setSectionResizeMode(
            col, QHeaderView.ResizeMode.ResizeToContents
        )
    table.verticalHeader().setVisible(False)
    return table


def _set_cell(
    table: QTableWidget,
    row: int,
    col: int,
    text: str,
    color: Optional[str] = None,
    align_right: bool = True,
) -> None:
    item = QTableWidgetItem(text)
    if align_right:
        item.setTextAlignment(
            Qt.AlignmentFlag.AlignRight | Qt.AlignmentFlag.AlignVCenter
        )
    if color:
        from PySide6.QtGui import QColor

        item.setForeground(QColor(color))
    table.setItem(row, col, item)

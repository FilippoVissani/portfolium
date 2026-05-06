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

_BG = "#1e1e2e"
_TEXT = "#cdd6f4"
_GREEN = "#a6e3a1"
_RED = "#f38ba8"
_YELLOW = "#f9e2af"
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
        self._value.setStyleSheet("color: #cdd6f4; font-size: 12pt; font-weight: bold;")

        layout.addWidget(self._title)
        layout.addWidget(self._value)

    def set_value(self, text: str, color: Optional[str] = None) -> None:
        style = "font-size: 12pt; font-weight: bold;"
        if color:
            style += f" color: {color};"
        else:
            style += f" color: {_TEXT};"
        self._value.setStyleSheet(style)
        self._value.setText(text)


class _GoalCard(QFrame):
    """Compact card showing a single planned-expense goal with a progress bar."""

    def __init__(self) -> None:
        super().__init__()
        self.setStyleSheet("background-color: #181825; border-radius: 8px;")
        self.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Fixed)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(14, 12, 14, 12)
        layout.setSpacing(6)

        # Header row: name + deadline
        header = QHBoxLayout()
        self._name_label = QLabel()
        self._name_label.setStyleSheet(
            f"color: {_TEXT}; font-size: 10pt; font-weight: bold;"
        )
        self._deadline_label = QLabel()
        self._deadline_label.setStyleSheet("color: #6c7086; font-size: 8pt;")
        header.addWidget(self._name_label)
        header.addStretch()
        header.addWidget(self._deadline_label)
        layout.addLayout(header)

        # Progress bar
        self._progress_bar = QProgressBar()
        self._progress_bar.setRange(0, 100)
        self._progress_bar.setTextVisible(False)
        self._progress_bar.setFixedHeight(8)
        self._progress_bar.setStyleSheet(
            "QProgressBar { background-color: #313244; border-radius: 4px; }"
            "QProgressBar::chunk { background-color: #89b4fa; border-radius: 4px; }"
        )
        layout.addWidget(self._progress_bar)

        # Bottom row: progress % + amounts + days remaining
        bottom = QHBoxLayout()
        self._pct_label = QLabel()
        self._pct_label.setStyleSheet(f"color: {_TEXT}; font-size: 8pt;")
        self._amounts_label = QLabel()
        self._amounts_label.setStyleSheet("color: #6c7086; font-size: 8pt;")
        self._days_label = QLabel()
        self._days_label.setStyleSheet("color: #6c7086; font-size: 8pt;")
        bottom.addWidget(self._pct_label)
        bottom.addStretch()
        bottom.addWidget(self._amounts_label)
        bottom.addStretch()
        bottom.addWidget(self._days_label)
        layout.addLayout(bottom)

    def update_data(self, goal: PlannedExpenseProgress) -> None:
        self._name_label.setText(goal.name)
        self._deadline_label.setText(
            f"Due: {goal.expiration_date.strftime('%d %b %Y')}"
        )

        pct = int(goal.progress_pct)
        self._progress_bar.setValue(pct)

        if goal.is_overdue:
            chunk_color = _RED
        elif goal.is_completed:
            chunk_color = _GREEN
        elif goal.progress_pct >= 75:
            chunk_color = _GREEN
        elif goal.progress_pct >= 40:
            chunk_color = _YELLOW
        else:
            chunk_color = "#89b4fa"
        self._progress_bar.setStyleSheet(
            "QProgressBar { background-color: #313244; border-radius: 4px; }"
            f"QProgressBar::chunk {{ background-color: {chunk_color}; border-radius: 4px; }}"
        )

        self._pct_label.setText(f"{pct}% funded")
        self._amounts_label.setText(
            f"€{goal.total_saved:,.0f} / €{goal.estimated_amount:,.0f}"
        )

        if goal.is_completed:
            days_text = "Goal reached!"
            days_color = _GREEN
        elif goal.is_overdue:
            days_text = "Overdue!"
            days_color = _RED
        else:
            days_text = f"{goal.days_remaining} days left"
            days_color = "#6c7086"
        self._days_label.setStyleSheet(f"color: {days_color}; font-size: 8pt;")
        self._days_label.setText(days_text)


class _AllocationPieChart(QWidget):
    """Matplotlib pie chart for planned account allocation."""

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


class PlannedExpensesPage(QWidget):
    """
    MVC View – Planned Expenses tab page.

    Layout:
      • KPI summary bar (total saved, total needed, overall coverage)
      • QSplitter: left = scrollable goal cards | right = allocation pie
      • Holdings table
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

        # ── Middle: goal cards (left) + pie chart (right) ─────────────── #
        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setChildrenCollapsible(False)

        # Left – scrollable goal cards
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff)
        scroll.setStyleSheet(f"QScrollArea {{ background: {_BG}; border: none; }}")
        goals_container = QWidget()
        goals_container.setStyleSheet(f"background: {_BG};")
        self._goals_layout = QVBoxLayout(goals_container)
        self._goals_layout.setContentsMargins(0, 0, 0, 0)
        self._goals_layout.setSpacing(8)
        self._goals_layout.addStretch()
        scroll.setWidget(goals_container)
        splitter.addWidget(scroll)

        # Right – allocation pie
        self._pie = _AllocationPieChart()
        splitter.addWidget(self._pie)
        splitter.setSizes([500, 300])
        root.addWidget(splitter, 3)

        # ── Holdings table ─────────────────────────────────────────────── #
        root.addWidget(_section_label("Holdings"))
        self._table = _build_holdings_table()
        root.addWidget(self._table, 2)

    # ------------------------------------------------------------------ #
    # Refresh                                                              #
    # ------------------------------------------------------------------ #

    def refresh(self) -> None:
        goals = self._ctrl.get_planned_expense_progress()
        asset_infos = self._ctrl.get_planned_asset_infos()
        cash = self._ctrl.get_planned_cash_balance()
        total_saved = self._ctrl.get_total_planned_value()
        total_needed = sum(g.estimated_amount for g in goals)
        allocation = self._ctrl.get_planned_allocation_data()

        # KPI cards
        coverage = (total_saved / total_needed * 100) if total_needed else 0.0
        cov_color = _GREEN if coverage >= 75 else _YELLOW if coverage >= 40 else _RED
        self._kpi_saved.set_value(f"€{total_saved:,.2f}")
        self._kpi_needed.set_value(f"€{total_needed:,.2f}")
        self._kpi_coverage.set_value(f"{coverage:.1f}%", cov_color)
        self._kpi_cash.set_value(f"€{cash:,.2f}")
        self._kpi_goals.set_value(str(len(goals)))

        # Goal cards – rebuild
        while self._goals_layout.count() > 1:
            item = self._goals_layout.takeAt(0)
            if item.widget():
                item.widget().deleteLater()

        for goal in goals:
            card = _GoalCard()
            card.update_data(goal)
            self._goals_layout.insertWidget(self._goals_layout.count() - 1, card)

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
    lbl.setStyleSheet("color: #6c7086; font-size: 8pt; font-weight: bold;")
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

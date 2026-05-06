from datetime import date, timedelta
from typing import Dict, Optional

import numpy as np
import pyqtgraph as pg
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QFrame,
    QHBoxLayout,
    QLabel,
    QPushButton,
    QTableWidget,
    QTableWidgetItem,
    QVBoxLayout,
    QWidget,
    QHeaderView,
)

from ...controllers.portfolio_controller import PortfolioController

_BG = "#1e1e2e"
_TEXT = "#cdd6f4"
_GREEN = "#a6e3a1"
_RED = "#f38ba8"
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
            style += " color: #cdd6f4;"
        self._value.setStyleSheet(style)
        self._value.setText(text)


class BaseAccountPage(QWidget):
    """Dashboard for base bank accounts."""

    def __init__(self, controller: PortfolioController, parent=None) -> None:
        super().__init__(parent)
        self.controller = controller
        self._period = "6M"
        self._period_days: Dict[str, Optional[int]] = {
            "1M": 30,
            "3M": 90,
            "6M": 180,
            "1Y": 365,
            "MAX": None,
        }

        root = QVBoxLayout(self)
        root.setContentsMargins(0, 0, 0, 0)
        root.setSpacing(8)

        top = QHBoxLayout()
        top.addWidget(self._make_title())
        top.addStretch()
        self._period_buttons: Dict[str, QPushButton] = {}
        for p in self._period_days:
            btn = QPushButton(p)
            btn.setCheckable(True)
            btn.clicked.connect(lambda _=False, period=p: self._set_period(period))
            top.addWidget(btn)
            self._period_buttons[p] = btn
        self._period_buttons[self._period].setChecked(True)
        root.addLayout(top)

        kpis = QHBoxLayout()
        self._balance = _KpiCard("Current Balance")
        self._income = _KpiCard("Income")
        self._expenses = _KpiCard("Expenses")
        self._savings = _KpiCard("Savings")
        self._avg = _KpiCard("Avg Monthly Expenses")
        for card in [
            self._balance,
            self._income,
            self._expenses,
            self._savings,
            self._avg,
        ]:
            kpis.addWidget(card)
        root.addLayout(kpis)

        middle = QHBoxLayout()

        self._pie_fig = Figure(facecolor=_BG)
        self._pie_canvas = FigureCanvas(self._pie_fig)
        middle.addWidget(self._pie_canvas, 3)

        pg.setConfigOptions(antialias=True, background=_BG, foreground=_TEXT)
        self._bars = pg.PlotWidget()
        self._bars.setLabel("left", "EUR", color=_TEXT)
        self._bars.showGrid(x=True, y=True, alpha=0.12)
        middle.addWidget(self._bars, 4)

        root.addLayout(middle, 3)

        self._tx_table = QTableWidget(0, 4)
        self._tx_table.setHorizontalHeaderLabels(
            ["Date", "Description", "Category", "Amount (€)"]
        )
        hh = self._tx_table.horizontalHeader()
        hh.setSectionResizeMode(0, QHeaderView.ResizeToContents)
        hh.setSectionResizeMode(1, QHeaderView.Stretch)
        hh.setSectionResizeMode(2, QHeaderView.ResizeToContents)
        hh.setSectionResizeMode(3, QHeaderView.ResizeToContents)
        self._tx_table.verticalHeader().setVisible(False)
        self._tx_table.setSortingEnabled(True)
        root.addWidget(self._tx_table, 2)

    def _make_title(self) -> QLabel:
        title = QLabel("Base Account")
        title.setStyleSheet("font-size: 12pt; font-weight: bold; color: #cdd6f4;")
        return title

    def _set_period(self, period: str) -> None:
        self._period = period
        for key, btn in self._period_buttons.items():
            btn.setChecked(key == period)
        self.refresh()

    def _date_range(self) -> tuple[date, date]:
        today = date.today()
        days = self._period_days[self._period]
        if days is None:
            movements = self.controller.get_base_movements()
            if not movements:
                return today - timedelta(days=365), today
            return movements[0][1].date, today
        return today - timedelta(days=days), today

    def refresh(self) -> None:
        start, end = self._date_range()

        summary = self.controller.get_base_summary(start, end)
        self._balance.set_value(f"EUR {summary['current_balance']:,.2f}")
        self._income.set_value(f"EUR {summary['income']:,.2f}", _GREEN)
        self._expenses.set_value(f"EUR {summary['expenses']:,.2f}", _RED)
        savings = summary["savings"]
        self._savings.set_value(f"EUR {savings:,.2f}", _GREEN if savings >= 0 else _RED)
        self._avg.set_value(f"EUR {summary['avg_monthly_expenses']:,.2f}")

        self._draw_expenses_pie(
            self.controller.get_base_expenses_by_category(start, end)
        )
        self._draw_monthly_bars(self.controller.get_base_monthly_cashflow(start, end))
        self._fill_transactions(start, end)

    def _draw_expenses_pie(self, categories: Dict[str, float]) -> None:
        self._pie_fig.clear()
        ax = self._pie_fig.add_subplot(111)
        ax.set_facecolor(_BG)

        if categories:
            labels = list(categories.keys())
            values = list(categories.values())
            colors = (_PALETTE * ((len(labels) // len(_PALETTE)) + 1))[: len(labels)]
            ax.pie(
                values,
                labels=None,
                autopct="%1.1f%%",
                startangle=90,
                colors=colors,
                wedgeprops={"edgecolor": _BG, "linewidth": 1.5},
            )
            ax.legend(
                labels,
                loc="lower center",
                bbox_to_anchor=(0.5, -0.1),
                ncol=min(len(labels), 3),
                frameon=False,
                labelcolor=_TEXT,
                fontsize=8,
            )
            ax.set_title("Expenses by Category", color=_TEXT, fontsize=10)
        else:
            ax.text(0.5, 0.5, "No expense data", color=_TEXT, ha="center", va="center")
            ax.axis("off")

        self._pie_fig.tight_layout()
        self._pie_canvas.draw()

    def _draw_monthly_bars(self, monthly_df) -> None:
        self._bars.clear()
        if monthly_df.empty:
            return

        months = list(monthly_df.index)
        x = np.arange(len(months), dtype=float)

        income = monthly_df["income"].to_numpy(dtype=float)
        expenses = monthly_df["expenses"].to_numpy(dtype=float)

        bar_income = pg.BarGraphItem(
            x=x - 0.18, height=income, width=0.34, brush=_GREEN
        )
        bar_expenses = pg.BarGraphItem(
            x=x + 0.18, height=expenses, width=0.34, brush=_RED
        )

        self._bars.addItem(bar_income)
        self._bars.addItem(bar_expenses)

        axis = self._bars.getAxis("bottom")
        axis.setTicks([[(float(i), month) for i, month in enumerate(months)]])

    def _fill_transactions(self, start: date, end: date) -> None:
        movements = list(reversed(self.controller.get_base_movements(start, end)))
        self._tx_table.setSortingEnabled(False)
        self._tx_table.setRowCount(len(movements))

        for row, (_, txn) in enumerate(movements):
            amount = txn.amount or 0.0
            values = [
                txn.date.isoformat(),
                txn.description or "",
                txn.category or "Uncategorized",
                f"{amount:,.2f}",
            ]
            for col, value in enumerate(values):
                item = QTableWidgetItem(value)
                if col == 3:
                    item.setTextAlignment(Qt.AlignRight | Qt.AlignVCenter)
                    item.setForeground(
                        Qt.GlobalColor.green if amount >= 0 else Qt.GlobalColor.red
                    )
                self._tx_table.setItem(row, col, item)

        self._tx_table.setSortingEnabled(True)

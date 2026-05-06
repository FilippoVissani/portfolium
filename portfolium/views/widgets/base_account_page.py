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
        self._value_lbl.setStyleSheet(
            f"color: {c['text']}; font-size: 12pt; font-weight: bold;"
        )

    def set_value(self, text: str, color: Optional[str] = None) -> None:
        c = ThemeManager().colors()
        style = "font-size: 12pt; font-weight: bold;"
        style += f" color: {color if color else c['text']};"
        self._value_lbl.setStyleSheet(style)
        self._value_lbl.setText(text)


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
        self._title_lbl = self._make_title()
        top.addWidget(self._title_lbl)
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

        c = ThemeManager().colors()
        self._pie_fig = Figure(facecolor=c["bg"])
        self._pie_canvas = FigureCanvas(self._pie_fig)
        middle.addWidget(self._pie_canvas, 3)

        pg.setConfigOptions(antialias=True, background=c["bg"], foreground=c["text"])
        self._bars = pg.PlotWidget()
        self._bars.setLabel("left", "EUR", color=c["text"])
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

        ThemeManager().changed.connect(self._on_theme_changed)

    def _make_title(self) -> QLabel:
        lbl = QLabel("Base Account")
        lbl.setStyleSheet(
            f"font-size: 12pt; font-weight: bold; color: {ThemeManager().colors()['text']};"
        )
        return lbl

    def _on_theme_changed(self, _theme: str) -> None:
        c = ThemeManager().colors()
        self._title_lbl.setStyleSheet(
            f"font-size: 12pt; font-weight: bold; color: {c['text']};"
        )
        self._bars.setBackground(c["bg"])
        self._bars.getAxis("left").setTextPen(c["text"])
        self._bars.setLabel("left", "EUR", color=c["text"])
        # Redraw charts with new colours
        try:
            self.refresh()
        except Exception:
            pass

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
        c = ThemeManager().colors()
        start, end = self._date_range()

        summary = self.controller.get_base_summary(start, end)
        self._balance.set_value(f"EUR {summary['current_balance']:,.2f}")
        self._income.set_value(f"EUR {summary['income']:,.2f}", c["green"])
        self._expenses.set_value(f"EUR {summary['expenses']:,.2f}", c["red"])
        savings = summary["savings"]
        self._savings.set_value(
            f"EUR {savings:,.2f}", c["green"] if savings >= 0 else c["red"]
        )
        self._avg.set_value(f"EUR {summary['avg_monthly_expenses']:,.2f}")

        self._draw_expenses_pie(
            self.controller.get_base_expenses_by_category(start, end)
        )
        self._draw_monthly_bars(self.controller.get_base_monthly_cashflow(start, end))
        self._fill_transactions(start, end)

    def _draw_expenses_pie(self, categories: Dict[str, float]) -> None:
        c = ThemeManager().colors()
        self._pie_fig.clear()
        self._pie_fig.set_facecolor(c["bg"])
        ax = self._pie_fig.add_subplot(111)
        ax.set_facecolor(c["bg"])
        palette = c["palette"]

        if categories:
            labels = list(categories.keys())
            values = list(categories.values())
            colors = (palette * ((len(labels) // len(palette)) + 1))[: len(labels)]
            ax.pie(
                values,
                labels=None,
                autopct="%1.1f%%",
                startangle=90,
                colors=colors,
                wedgeprops={"edgecolor": c["bg"], "linewidth": 1.5},
                textprops={"color": "#000000", "fontsize": 8},  # Dark text
            )
            ax.legend(
                labels,
                loc="lower center",
                bbox_to_anchor=(0.5, -0.1),
                ncol=min(len(labels), 3),
                frameon=False,
                labelcolor=c["text"],
                fontsize=8,
            )
            ax.set_title("Expenses by Category", color=c["text"], fontsize=10)
        else:
            ax.text(
                0.5, 0.5, "No expense data", color=c["text"], ha="center", va="center"
            )
            ax.axis("off")

        self._pie_fig.tight_layout()
        self._pie_canvas.draw()

    def _draw_monthly_bars(self, monthly_df) -> None:
        c = ThemeManager().colors()
        self._bars.clear()
        if monthly_df.empty:
            return

        months = list(monthly_df.index)
        x = np.arange(len(months), dtype=float)
        income = monthly_df["income"].to_numpy(dtype=float)
        expenses = monthly_df["expenses"].to_numpy(dtype=float)

        bar_income = pg.BarGraphItem(
            x=x - 0.18, height=income, width=0.34, brush=c["green"]
        )
        bar_expenses = pg.BarGraphItem(
            x=x + 0.18, height=expenses, width=0.34, brush=c["red"]
        )

        self._bars.addItem(bar_income)
        self._bars.addItem(bar_expenses)

        axis = self._bars.getAxis("bottom")
        axis.setTicks([[(float(i), month) for i, month in enumerate(months)]])

    def _fill_transactions(self, start: date, end: date) -> None:
        c = ThemeManager().colors()
        movements = list(reversed(self.controller.get_base_movements(start, end)))
        self._tx_table.setSortingEnabled(False)
        self._tx_table.setRowCount(len(movements))

        from PySide6.QtGui import QColor

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
                    item.setForeground(QColor(c["green"] if amount >= 0 else c["red"]))
                self._tx_table.setItem(row, col, item)

        self._tx_table.setSortingEnabled(True)

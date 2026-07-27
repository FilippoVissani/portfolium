from datetime import date, timedelta
from typing import Dict, Optional

import numpy as np
import pyqtgraph as pg
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
from PySide6.QtCore import QDate, Qt
from PySide6.QtWidgets import (
    QDateEdit,
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


class BaseAccountPage(QWidget):
    """Dashboard for base bank accounts."""

    def __init__(self, controller: PortfolioController, parent=None) -> None:
        super().__init__(parent)
        self.controller = controller
        self._period: Optional[str] = "6M"
        self._custom_start: Optional[date] = None
        self._custom_end: Optional[date] = None
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

        self._from_lbl = QLabel("From")
        self._start_date = QDateEdit()
        self._start_date.setCalendarPopup(True)
        self._start_date.setDisplayFormat("yyyy-MM-dd")
        self._start_date.dateChanged.connect(lambda _: self._on_custom_date_changed())
        top.addWidget(self._from_lbl)
        top.addWidget(self._start_date)

        self._to_lbl = QLabel("To")
        self._end_date = QDateEdit()
        self._end_date.setCalendarPopup(True)
        self._end_date.setDisplayFormat("yyyy-MM-dd")
        self._end_date.dateChanged.connect(lambda _: self._on_custom_date_changed())
        top.addWidget(self._to_lbl)
        top.addWidget(self._end_date)

        self._apply_custom_btn = QPushButton("Apply")
        self._apply_custom_btn.clicked.connect(self._apply_custom_range)
        top.addWidget(self._apply_custom_btn)

        self._range_error_lbl = QLabel("")
        top.addWidget(self._range_error_lbl)
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
        start, end = self._quick_date_range("6M")
        self._sync_date_edits(start, end)
        self._on_custom_date_changed()
        self._on_theme_changed(ThemeManager().current)

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
        self._from_lbl.setStyleSheet(f"color: {c['subtext']};")
        self._to_lbl.setStyleSheet(f"color: {c['subtext']};")
        self._range_error_lbl.setStyleSheet(f"color: {c['red']}; font-size: 8pt;")
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
        self._custom_start = None
        self._custom_end = None
        for key, btn in self._period_buttons.items():
            btn.setChecked(key == period)
        start, end = self._quick_date_range(period)
        self._sync_date_edits(start, end)
        self._range_error_lbl.setText("")
        self._on_custom_date_changed()
        self.refresh()

    def _quick_date_range(self, period: str) -> tuple[date, date]:
        today = date.today()
        days = self._period_days[period]
        if days is None:
            movements = self.controller.get_base_movements()
            if not movements:
                return today - timedelta(days=365), today
            return movements[0][1].date, today
        return today - timedelta(days=days), today

    def _date_range(self) -> tuple[date, date]:
        if self._period is None and self._custom_start and self._custom_end:
            return self._custom_start, self._custom_end
        active_period = self._period or "6M"
        return self._quick_date_range(active_period)

    def _sync_date_edits(self, start: date, end: date) -> None:
        self._start_date.blockSignals(True)
        self._end_date.blockSignals(True)
        self._start_date.setDate(QDate(start.year, start.month, start.day))
        self._end_date.setDate(QDate(end.year, end.month, end.day))
        self._start_date.blockSignals(False)
        self._end_date.blockSignals(False)

    def _selected_custom_dates(self) -> tuple[date, date]:
        start_q = self._start_date.date()
        end_q = self._end_date.date()
        start = date(start_q.year(), start_q.month(), start_q.day())
        end = date(end_q.year(), end_q.month(), end_q.day())
        return start, end

    def _on_custom_date_changed(self) -> None:
        start, end = self._selected_custom_dates()
        invalid = start > end
        self._apply_custom_btn.setEnabled(not invalid)
        self._range_error_lbl.setText(
            "Start date must be before end date." if invalid else ""
        )

    def _apply_custom_range(self) -> None:
        start, end = self._selected_custom_dates()
        if start > end:
            self._on_custom_date_changed()
            return

        self._custom_start = start
        self._custom_end = end
        self._period = None
        self._range_error_lbl.setText("")
        for btn in self._period_buttons.values():
            btn.setChecked(False)
        self.refresh()

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
        render_pie_chart(
            ax,
            categories,
            c,
            title="Expenses by Category",
            empty_text="No expense data",
        )

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

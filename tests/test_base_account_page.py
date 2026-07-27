from datetime import date, timedelta

import pandas as pd
from PySide6.QtCore import QDate

from portfolium.models.account import Transaction
from portfolium.views.widgets.base_account_page import BaseAccountPage


def _qdate_to_date(qdate: QDate) -> date:
    return date(qdate.year(), qdate.month(), qdate.day())


class _StubController:
    def __init__(self) -> None:
        self._movements = [
            (
                "Base",
                Transaction(
                    type="base",
                    date=date.today() - timedelta(days=45),
                    amount=1200.0,
                    description="Salary",
                    category="Income",
                ),
            ),
            (
                "Base",
                Transaction(
                    type="base",
                    date=date.today() - timedelta(days=10),
                    amount=-120.0,
                    description="Groceries",
                    category="Food",
                ),
            ),
        ]

    def get_base_movements(self, start: date | None = None, end: date | None = None):
        return [
            (acc, txn)
            for acc, txn in self._movements
            if (start is None or txn.date >= start) and (end is None or txn.date <= end)
        ]

    def get_base_summary(self, start: date, end: date):
        return {
            "current_balance": 1080.0,
            "income": 1200.0,
            "expenses": 120.0,
            "savings": 1080.0,
            "avg_monthly_expenses": 120.0,
        }

    def get_base_expenses_by_category(self, start: date, end: date):
        return {"Food": 120.0}

    def get_base_monthly_cashflow(self, start: date, end: date):
        return pd.DataFrame(columns=["income", "expenses", "savings"])


def test_initial_period_syncs_date_inputs(qtbot):
    page = BaseAccountPage(_StubController())
    qtbot.addWidget(page)

    start, end = page._date_range()
    assert _qdate_to_date(page._start_date.date()) == start
    assert _qdate_to_date(page._end_date.date()) == end
    assert page._period_buttons["6M"].isChecked() is True


def test_apply_custom_range_clears_quick_selection(qtbot, monkeypatch):
    page = BaseAccountPage(_StubController())
    qtbot.addWidget(page)
    calls = {"refresh": 0}
    monkeypatch.setattr(page, "refresh", lambda: calls.__setitem__("refresh", calls["refresh"] + 1))

    custom_start = date.today() - timedelta(days=20)
    custom_end = date.today() - timedelta(days=2)
    page._start_date.setDate(QDate(custom_start.year, custom_start.month, custom_start.day))
    page._end_date.setDate(QDate(custom_end.year, custom_end.month, custom_end.day))
    page._apply_custom_range()

    assert page._period is None
    assert page._custom_start == custom_start
    assert page._custom_end == custom_end
    assert all(not btn.isChecked() for btn in page._period_buttons.values())
    assert page._date_range() == (custom_start, custom_end)
    assert calls["refresh"] == 1


def test_invalid_custom_range_is_rejected(qtbot, monkeypatch):
    page = BaseAccountPage(_StubController())
    qtbot.addWidget(page)
    calls = {"refresh": 0}
    monkeypatch.setattr(page, "refresh", lambda: calls.__setitem__("refresh", calls["refresh"] + 1))

    page._start_date.setDate(QDate(2026, 7, 20))
    page._end_date.setDate(QDate(2026, 7, 10))
    page._apply_custom_range()

    assert page._period == "6M"
    assert page._custom_start is None
    assert page._custom_end is None
    assert page._apply_custom_btn.isEnabled() is False
    assert page._range_error_lbl.text() == "Start date must be before end date."
    assert calls["refresh"] == 0


def test_selecting_quick_period_after_custom_resets_inputs(qtbot):
    page = BaseAccountPage(_StubController())
    qtbot.addWidget(page)

    page._start_date.setDate(QDate(2026, 5, 1))
    page._end_date.setDate(QDate(2026, 5, 20))
    page._apply_custom_range()

    page._set_period("1M")
    expected_start, expected_end = page._quick_date_range("1M")

    assert page._period == "1M"
    assert page._period_buttons["1M"].isChecked() is True
    assert all(
        not btn.isChecked() for key, btn in page._period_buttons.items() if key != "1M"
    )
    assert _qdate_to_date(page._start_date.date()) == expected_start
    assert _qdate_to_date(page._end_date.date()) == expected_end

"""Tests for portfolium.controllers module."""
from datetime import date

import pytest

from portfolium.controllers.portfolio_controller import (
    AssetInfo,
    PlannedExpenseProgress,
    PortfolioController,
)
from portfolium.models.portfolio import Portfolio
from portfolium.controllers.portfolio_controller import EmergencyFundStatus
from portfolium.models.account import Account, PlannedExpense, Transaction


class TestAssetInfo:
    """Test AssetInfo view-model."""

    def test_asset_info_creation(self):
        info = AssetInfo(
            symbol="VOO",
            name="Vanguard S&P 500",
            quantity=10.0,
            current_price=425.0,
            avg_cost=400.0,
            previous_close=420.0,
        )
        assert info.symbol == "VOO"
        assert info.name == "Vanguard S&P 500"
        assert info.quantity == 10.0

    def test_asset_current_value(self):
        info = AssetInfo(
            symbol="AAPL",
            name="Apple",
            quantity=5.0,
            current_price=160.0,
            avg_cost=150.0,
            previous_close=158.0,
        )
        assert info.current_value == 800.0

    def test_asset_cost_basis(self):
        info = AssetInfo(
            symbol="AAPL",
            name="Apple",
            quantity=5.0,
            current_price=160.0,
            avg_cost=150.0,
            previous_close=158.0,
        )
        assert info.cost_basis == 750.0

    def test_asset_gain_loss_eur(self):
        info = AssetInfo(
            symbol="AAPL",
            name="Apple",
            quantity=5.0,
            current_price=160.0,
            avg_cost=150.0,
            previous_close=158.0,
        )
        assert info.gain_loss_eur == 50.0  # 800 - 750

    def test_asset_gain_loss_pct(self):
        info = AssetInfo(
            symbol="AAPL",
            name="Apple",
            quantity=5.0,
            current_price=160.0,
            avg_cost=150.0,
            previous_close=158.0,
        )
        assert pytest.approx(info.gain_loss_pct, abs=0.01) == 6.67

    def test_asset_intraday_gain_loss(self):
        info = AssetInfo(
            symbol="AAPL",
            name="Apple",
            quantity=5.0,
            current_price=160.0,
            avg_cost=150.0,
            previous_close=158.0,
        )
        assert info.intraday_gain_loss_eur == 10.0  # 5 * (160 - 158)

    def test_asset_loss_scenario(self):
        info = AssetInfo(
            symbol="TSLA",
            name="Tesla",
            quantity=2.0,
            current_price=180.0,
            avg_cost=200.0,
            previous_close=190.0,
        )
        assert info.current_value == 360.0
        assert info.cost_basis == 400.0
        assert info.gain_loss_eur == -40.0
        assert pytest.approx(info.gain_loss_pct) == -10.0


class TestPortfolioController:
    """Test PortfolioController orchestration."""

    def test_controller_creation(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        assert controller.portfolio is sample_portfolio
        assert controller.market_data is mock_market_data

    def test_refresh_market_data(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        controller.refresh_market_data()

        assert "VOO" in controller._prices
        assert "ACWI" in controller._prices
        assert controller._prices["VOO"] == 425.0

    def test_get_investment_asset_infos(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        controller.refresh_market_data()

        infos = controller.get_investment_asset_infos()
        assert len(infos) == 2

        voo_info = next(i for i in infos if i.symbol == "VOO")
        assert voo_info.quantity == 5.0
        assert voo_info.current_price == 425.0

    def test_get_investment_cash_balance(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        cash = controller.get_investment_cash_balance()
        assert pytest.approx(cash) == 1185.0

    def test_get_total_investment_value(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        controller.refresh_market_data()

        total = controller.get_total_investment_value()
        # VOO: 5 * 425 = 2125
        # ACWI: 20 * 98 = 1960
        # Cash: 1185
        # Total: 5270
        assert pytest.approx(total) == 5270.0

    def test_get_total_investment_gain_loss(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        controller.refresh_market_data()

        gain, gain_pct = controller.get_total_investment_gain_loss()
        # VOO: 5 * (425 - 400.5) = 122.5
        # ACWI: 20 * (98 - 95 - 0.25) = 54.5  (pro-rated commission)
        # Total gain ≈ 177
        assert gain > 0
        assert gain_pct > 0

    def test_get_investment_allocation_data(self, sample_portfolio: Portfolio, mock_market_data):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        controller.refresh_market_data()

        allocation = controller.get_investment_allocation_data()
        assert "VOO" in allocation
        assert "ACWI" in allocation
        assert "Cash" in allocation

        # VOO: 5 * 425 = 2125
        # ACWI: 20 * 98 = 1960
        # Cash: 1185
        assert pytest.approx(allocation["VOO"]) == 2125.0
        assert pytest.approx(allocation["ACWI"]) == 1960.0
        assert pytest.approx(allocation["Cash"]) == 1185.0

    def test_get_investment_allocation_without_cash(self, mock_market_data):
        """Test allocation when fully invested in a single asset."""
        txns = [
            Transaction(
                type="asset_buy",
                date=date(2024, 1, 1),
                name="Apple",
                symbol="AAPL",
                quantity=10.0,
                price=150.0,
                commission=0.0,
            ),
        ]
        acc = Account(
            name="Fully Invested",
            type="investment",
            initial_balance=1500.0,
            transactions=txns,
        )
        portfolio = Portfolio([acc])
        controller = PortfolioController(portfolio, mock_market_data)
        controller.refresh_market_data()

        allocation = controller.get_investment_allocation_data()
        assert "AAPL" in allocation
        # AAPL: 10 * 160 (mock price) = 1600 (not 1500 since mock price differs)
        assert pytest.approx(allocation["AAPL"]) == 1600.0


class TestPortfolioControllerHistorical:
    """Test historical performance computation."""

    def test_get_investment_historical_performance_empty(
        self, sample_portfolio: Portfolio, mock_market_data
    ):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        series = controller.get_investment_historical_performance(
            date(2024, 1, 1), date(2024, 1, 2)
        )
        # With yfinance mock, may return data or empty; just check it returns a Series
        assert hasattr(series, "index")

    def test_investment_historical_performance_with_real_dates(
        self, sample_portfolio: Portfolio, mock_market_data
    ):
        controller = PortfolioController(sample_portfolio, mock_market_data)
        # This would fetch real data; with mocking it may be empty or limited
        series = controller.get_investment_historical_performance(
            date(2024, 1, 1), date(2024, 12, 31)
        )
        # Just verify it returns a Series (may be empty in mock)
        assert hasattr(series, "index")
        assert hasattr(series, "values")


class TestBaseAccountAnalytics:
    """Test controller analytics for base bank accounts."""

    def test_base_summary_and_categories(self, mock_market_data):
        acc = Account(
            name="Main Bank Account",
            type="base",
            initial_balance=2000.0,
            transactions=[
                Transaction(
                    type="deposit",
                    date=date(2026, 2, 10),
                    description="Salary",
                    category="Salary",
                    amount=3500.0,
                ),
                Transaction(
                    type="withdrawal",
                    date=date(2026, 2, 14),
                    description="Rent",
                    category="Housing",
                    amount=-1200.0,
                ),
                Transaction(
                    type="withdrawal",
                    date=date(2026, 2, 12),
                    description="Dining Out",
                    category="Food",
                    amount=-85.0,
                ),
            ],
        )
        portfolio = Portfolio([acc])
        controller = PortfolioController(portfolio, mock_market_data)

        summary = controller.get_base_summary(date(2026, 2, 1), date(2026, 2, 28))
        assert pytest.approx(summary["income"]) == 3500.0
        assert pytest.approx(summary["expenses"]) == 1285.0
        assert pytest.approx(summary["savings"]) == 2215.0
        assert pytest.approx(summary["current_balance"]) == 4215.0

        categories = controller.get_base_expenses_by_category(date(2026, 2, 1), date(2026, 2, 28))
        assert pytest.approx(categories["Housing"]) == 1200.0
        assert pytest.approx(categories["Food"]) == 85.0

    def test_base_monthly_cashflow(self, mock_market_data):
        acc = Account(
            name="Main Bank Account",
            type="base",
            initial_balance=2000.0,
            transactions=[
                Transaction(type="deposit", date=date(2026, 1, 10), amount=1000.0, category="Salary"),
                Transaction(type="withdrawal", date=date(2026, 1, 15), amount=-300.0, category="Food"),
                Transaction(type="deposit", date=date(2026, 2, 10), amount=1200.0, category="Salary"),
            ],
        )
        controller = PortfolioController(Portfolio([acc]), mock_market_data)

        monthly = controller.get_base_monthly_cashflow(date(2026, 1, 1), date(2026, 2, 28))
        assert "2026-01" in monthly.index
        assert "2026-02" in monthly.index
        assert pytest.approx(monthly.loc["2026-01", "income"]) == 1000.0
        assert pytest.approx(monthly.loc["2026-01", "expenses"]) == 300.0
        assert pytest.approx(monthly.loc["2026-01", "savings"]) == 700.0


class TestPlannedAccountController:
    """Test controller analytics for planned accounts."""

    def _make_controller(self, mock_market_data) -> PortfolioController:
        acc = Account(
            name="Planned Savings",
            type="planned",
            initial_balance=0.0,
            transactions=[
                Transaction(type="deposit", date=date(2024, 1, 15), amount=5000.0),
                Transaction(
                    type="asset_buy",
                    date=date(2024, 2, 1),
                    name="Vanguard Total World",
                    symbol="VT",
                    quantity=10.0,
                    price=95.0,
                    commission=5.0,
                ),
            ],
            planned_expenses=[
                PlannedExpense(
                    name="House Down Payment",
                    expiration_date=date(2027, 12, 31),
                    estimated_amount=50000.0,
                ),
                PlannedExpense(
                    name="New Car",
                    expiration_date=date(2026, 6, 30),
                    estimated_amount=25000.0,
                ),
            ],
        )
        return PortfolioController(Portfolio([acc]), mock_market_data)

    def test_get_planned_cash_balance(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        # 5000 deposit - (10*95 + 5) buy = 5000 - 955 = 4045
        assert pytest.approx(ctrl.get_planned_cash_balance()) == 4045.0

    def test_get_planned_asset_infos(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        ctrl.refresh_market_data()
        infos = ctrl.get_planned_asset_infos()
        assert len(infos) == 1
        vt = infos[0]
        assert vt.symbol == "VT"
        assert pytest.approx(vt.quantity) == 10.0
        # VT not in mock prices dict → mock returns the default (100.0)
        assert vt.current_price == mock_market_data.get_current_price("VT")

    def test_get_total_planned_value(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        infos = ctrl.get_planned_asset_infos()
        total_holdings = sum(a.current_value for a in infos)
        expected = total_holdings + ctrl.get_planned_cash_balance()
        assert pytest.approx(ctrl.get_total_planned_value()) == expected

    def test_get_planned_expense_progress_count(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        progress = ctrl.get_planned_expense_progress()
        assert len(progress) == 2

    def test_planned_expense_progress_properties(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        total = ctrl.get_total_planned_value()
        progress = ctrl.get_planned_expense_progress()
        for p in progress:
            assert isinstance(p, PlannedExpenseProgress)
            assert pytest.approx(p.total_saved) == total
            assert 0.0 <= p.progress_pct <= 100.0
            assert p.remaining_amount >= 0.0

    def test_planned_expense_progress_view_model(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        progress = {p.name: p for p in ctrl.get_planned_expense_progress()}
        assert "House Down Payment" in progress
        assert "New Car" in progress
        assert progress["House Down Payment"].estimated_amount == 50000.0
        assert progress["New Car"].estimated_amount == 25000.0

    def test_planned_allocation_data_includes_cash(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        alloc = ctrl.get_planned_allocation_data()
        assert "Cash" in alloc
        assert alloc["Cash"] > 0

    def test_planned_not_in_investment_total(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        # No investment accounts → total investment value should be 0
        assert pytest.approx(ctrl.get_total_investment_value()) == 0.0

    def test_planned_expense_progress_is_overdue(self):
        """A goal past its deadline with insufficient savings is overdue."""
        p = PlannedExpenseProgress(
            name="Old Goal",
            expiration_date=date(2000, 1, 1),
            estimated_amount=10000.0,
            total_saved=5000.0,
        )
        assert p.is_overdue is True
        assert p.is_completed is False

    def test_planned_expense_progress_is_completed(self):
        """A goal with total_saved >= estimated_amount is completed."""
        p = PlannedExpenseProgress(
            name="Done",
            expiration_date=date(2030, 1, 1),
            estimated_amount=1000.0,
            total_saved=1500.0,
        )
        assert p.is_completed is True
        assert p.is_overdue is False
        assert p.progress_pct == 100.0


class TestEmergencyFundController:
    """Test controller analytics for emergency fund accounts."""

    def _make_controller(self, mock_market_data) -> PortfolioController:
        acc = Account(
            name="Emergency Fund",
            type="emergency",
            initial_balance=1000.0,
            target_capital=4000.0,
            transactions=[
                Transaction(type="deposit", date=date(2024, 1, 15), amount=2000.0),
                Transaction(
                    type="asset_buy",
                    date=date(2024, 2, 1),
                    name="Bond ETF",
                    symbol="AGG",
                    quantity=10.0,
                    price=100.0,
                    commission=5.0,
                ),
                Transaction(type="deposit", date=date(2024, 3, 15), amount=1500.0),
            ],
        )
        return PortfolioController(Portfolio([acc]), mock_market_data)

    def test_get_emergency_cash_balance(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        # initial(1000) + deposit(2000) - buy(10*100+5) + deposit(1500)
        expected = 1000.0 + 2000.0 - (10.0 * 100.0 + 5.0) + 1500.0
        assert pytest.approx(ctrl.get_emergency_cash_balance()) == expected

    def test_get_emergency_asset_infos(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        ctrl.refresh_market_data()
        infos = ctrl.get_emergency_asset_infos()
        assert len(infos) == 1
        assert infos[0].symbol == "AGG"
        assert pytest.approx(infos[0].quantity) == 10.0

    def test_get_total_emergency_value(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        total = ctrl.get_total_emergency_value()
        assert total > 0

    def test_get_emergency_allocation_data_has_cash(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        alloc = ctrl.get_emergency_allocation_data()
        assert "Cash" in alloc
        assert alloc["Cash"] > 0

    def test_get_emergency_fund_status_type(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        status = ctrl.get_emergency_fund_status()
        assert isinstance(status, EmergencyFundStatus)
        assert status.target_capital == 4000.0

    def test_emergency_fund_status_coverage_pct(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        status = ctrl.get_emergency_fund_status()
        assert 0.0 < status.coverage_pct <= 100.0

    def test_emergency_fund_status_remaining(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        status = ctrl.get_emergency_fund_status()
        assert status.remaining_amount >= 0.0

    def test_emergency_not_in_investment_total(self, mock_market_data):
        ctrl = self._make_controller(mock_market_data)
        assert pytest.approx(ctrl.get_total_investment_value()) == 0.0

    def test_emergency_fund_status_is_funded(self):
        """is_funded is True when current_value >= target_capital."""
        s = EmergencyFundStatus(current_value=5000.0, target_capital=4000.0)
        assert s.is_funded is True
        assert s.coverage_pct == 100.0
        assert s.remaining_amount == 0.0

    def test_emergency_fund_status_underfunded(self):
        s = EmergencyFundStatus(current_value=1000.0, target_capital=4000.0)
        assert s.is_funded is False
        assert pytest.approx(s.coverage_pct) == 25.0
        assert pytest.approx(s.remaining_amount) == 3000.0

    def test_emergency_fund_status_no_target(self):
        s = EmergencyFundStatus(current_value=1000.0, target_capital=None)
        assert s.coverage_pct == 0.0
        assert s.remaining_amount == 0.0
        assert s.is_funded is False

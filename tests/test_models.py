"""Tests for portfolium.models module."""
from datetime import date

import pytest

from portfolium.models.account import Account, PlannedExpense, Transaction
from portfolium.models.portfolio import Portfolio, Holding


class TestTransaction:
    """Test Transaction dataclass."""

    def test_transaction_creation(self):
        txn = Transaction(
            type="deposit",
            date=date(2024, 1, 1),
            amount=1000.0,
            description="Test deposit",
        )
        assert txn.type == "deposit"
        assert txn.date == date(2024, 1, 1)
        assert txn.amount == 1000.0

    def test_asset_transaction(self):
        txn = Transaction(
            type="asset_buy",
            date=date(2024, 1, 15),
            name="Apple",
            symbol="AAPL",
            quantity=10.0,
            price=150.0,
            commission=5.0,
        )
        assert txn.symbol == "AAPL"
        assert txn.quantity == 10.0
        assert txn.price == 150.0


class TestAccount:
    """Test Account dataclass."""

    def test_account_creation(self, sample_account: Account):
        assert sample_account.name == "Test Portfolio"
        assert sample_account.type == "investment"
        assert sample_account.initial_balance == 5000.0
        assert len(sample_account.transactions) == 5

    def test_empty_account(self):
        acc = Account(
            name="Empty",
            type="investment",
            initial_balance=0.0,
            transactions=[],
        )
        assert acc.initial_balance == 0.0
        assert len(acc.transactions) == 0


class TestHolding:
    """Test Holding dataclass."""

    def test_holding_creation(self):
        h = Holding(symbol="VOO", name="Vanguard S&P 500", quantity=10.0, avg_cost=400.0)
        assert h.symbol == "VOO"
        assert h.quantity == 10.0
        assert h.cost_basis == 4000.0

    def test_holding_cost_basis(self):
        h = Holding(symbol="AAPL", name="Apple", quantity=5.5, avg_cost=150.0)
        assert pytest.approx(h.cost_basis) == 825.0


class TestPortfolioHoldings:
    """Test Portfolio holdings calculation."""

    def test_get_investment_holdings(self, sample_portfolio: Portfolio):
        holdings = sample_portfolio.get_investment_holdings()
        assert "VOO" in holdings
        assert "ACWI" in holdings
        assert holdings["VOO"].quantity == 5.0  # bought 10, sold 5
        assert holdings["ACWI"].quantity == 20.0

    def test_holdings_weighted_avg_cost(self, sample_portfolio: Portfolio):
        holdings = sample_portfolio.get_investment_holdings()
        voo = holdings["VOO"]
        # Bought 10 @ 400 + 5 comm = 4005, then sold 5 @ 420
        # Remaining 5 should have weighted-avg cost
        assert pytest.approx(voo.avg_cost, rel=1e-2) == 400.5

    def test_holdings_up_to_date(self, sample_portfolio: Portfolio):
        holdings_all = sample_portfolio.get_investment_holdings()
        holdings_feb = sample_portfolio.get_investment_holdings(up_to=date(2024, 2, 20))

        # By Feb 20, we had VOO (10) and ACWI (20)
        assert holdings_feb["VOO"].quantity == 10.0
        assert holdings_feb["ACWI"].quantity == 20.0
        # After April 10 sell, VOO drops to 5
        assert holdings_all["VOO"].quantity == 5.0

    def test_no_holdings_initially(self):
        acc = Account(
            name="Empty",
            type="investment",
            initial_balance=1000.0,
            transactions=[],
        )
        portfolio = Portfolio([acc])
        holdings = portfolio.get_investment_holdings()
        assert len(holdings) == 0


class TestPortfolioCash:
    """Test Portfolio cash balance calculation."""

    def test_investment_cash_balance(self, sample_portfolio: Portfolio):
        cash = sample_portfolio.get_investment_cash_balance()
        # Initial: 5000
        # -Buy VOO (Jan 15): -(400*10 + 5) = -4005
        # +Deposit (Feb 15): +2000
        # -Buy ACWI (Feb 20): -(95*20 + 5) = -1905
        # +Sell VOO (Apr 10): +(420*5 - 5) = +2095
        # -Withdrawal (Apr 15): -2000
        # = 5000 - 4005 + 2000 - 1905 + 2095 - 2000 = 1185
        assert pytest.approx(cash) == 1185.0

    def test_cash_up_to_date(self, sample_portfolio: Portfolio):
        cash_all = sample_portfolio.get_investment_cash_balance()
        cash_jan = sample_portfolio.get_investment_cash_balance(up_to=date(2024, 1, 15))

        # By Jan 15: 5000 initial - 4005 (buy VOO) = 995
        assert pytest.approx(cash_jan) == 995.0
        assert pytest.approx(cash_all) == 1185.0

    def test_cash_after_withdrawal(self, sample_portfolio: Portfolio):
        cash_before_withdrawal = sample_portfolio.get_investment_cash_balance(
            up_to=date(2024, 4, 14)
        )
        cash_after_withdrawal = sample_portfolio.get_investment_cash_balance(
            up_to=date(2024, 4, 15)
        )
        # Withdrawal: 2000
        assert pytest.approx(cash_before_withdrawal - cash_after_withdrawal) == 2000.0


class TestPortfolioSymbols:
    """Test Portfolio symbol tracking."""

    def test_get_investment_symbols(self, sample_portfolio: Portfolio):
        symbols = sample_portfolio.get_investment_symbols()
        assert set(symbols) == {"VOO", "ACWI"}

    def test_get_investment_all_symbols(self, sample_portfolio: Portfolio):
        all_symbols = sample_portfolio.get_investment_all_symbols()
        assert set(all_symbols) == {"VOO", "ACWI"}

    def test_get_investment_all_symbols_includes_exited(self):
        txns = [
            Transaction(
                type="asset_buy",
                date=date(2024, 1, 1),
                name="Tesla",
                symbol="TSLA",
                quantity=5.0,
                price=200.0,
                commission=0.0,
            ),
            Transaction(
                type="asset_sell",
                date=date(2024, 2, 1),
                name="Tesla",
                symbol="TSLA",
                quantity=5.0,  # sell all
                price=220.0,
                commission=0.0,
            ),
        ]
        acc = Account(
            name="Test",
            type="investment",
            initial_balance=1000.0,
            transactions=txns,
        )
        portfolio = Portfolio([acc])

        current_symbols = portfolio.get_investment_symbols()
        all_symbols = portfolio.get_investment_all_symbols()

        assert "TSLA" not in current_symbols  # fully exited
        assert "TSLA" in all_symbols  # but it was traded


class TestMultipleAccounts:
    """Test Portfolio with multiple accounts."""

    def test_portfolio_multiple_accounts(self):
        acc1 = Account(
            name="Stocks",
            type="investment",
            initial_balance=5000.0,
            transactions=[
                Transaction(
                    type="asset_buy",
                    date=date(2024, 1, 1),
                    name="Apple",
                    symbol="AAPL",
                    quantity=10.0,
                    price=150.0,
                    commission=0.0,
                )
            ],
        )
        acc2 = Account(
            name="Bonds",
            type="investment",
            initial_balance=3000.0,
            transactions=[
                Transaction(
                    type="asset_buy",
                    date=date(2024, 1, 1),
                    name="Government Bond",
                    symbol="GOVT",
                    quantity=30.0,
                    price=100.0,
                    commission=0.0,
                )
            ],
        )

        portfolio = Portfolio([acc1, acc2])

        symbols = portfolio.get_investment_symbols()
        assert set(symbols) == {"AAPL", "GOVT"}

        cash = portfolio.get_investment_cash_balance()
        assert pytest.approx(cash) == (5000 - 1500) + (3000 - 3000)


class TestBaseAccountCash:
    """Test that base accounts are excluded from investment portfolio calculations."""

    def test_base_account_excluded_from_portfolio_cash(self):
        """Portfolio.get_investment_cash_balance must not include base account funds."""
        base_acc = Account(
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
            ],
        )
        portfolio = Portfolio([base_acc])
        # No investment accounts → cash balance must be 0, not base account funds
        assert pytest.approx(portfolio.get_investment_cash_balance()) == 0.0

    def test_mixed_accounts_only_investment_cash(self):
        """Cash balance considers only investment accounts even when base accounts are present."""
        investment_acc = Account(
            name="Investments",
            type="investment",
            initial_balance=5000.0,
            transactions=[],
        )
        base_acc = Account(
            name="Main Bank Account",
            type="base",
            initial_balance=2000.0,
            transactions=[
                Transaction(type="deposit", date=date(2026, 2, 10), amount=3500.0, category="Salary"),
            ],
        )
        portfolio = Portfolio([investment_acc, base_acc])
        # Only the investment account's initial balance should be counted
        assert pytest.approx(portfolio.get_investment_cash_balance()) == 5000.0


class TestPlannedAccount:
    """Test Portfolio methods for planned accounts."""

    def _make_planned_account(self) -> Account:
        return Account(
            name="Planned Savings",
            type="planned",
            initial_balance=3000.0,
            transactions=[
                Transaction(
                    type="deposit",
                    date=date(2024, 1, 15),
                    amount=3000.0,
                    description="Initial fund",
                ),
                Transaction(
                    type="asset_buy",
                    date=date(2024, 2, 1),
                    name="Vanguard Total World",
                    symbol="VT",
                    quantity=20.0,
                    price=95.0,
                    commission=5.0,
                ),
                Transaction(
                    type="deposit",
                    date=date(2024, 3, 15),
                    amount=2000.0,
                    description="Quarterly savings",
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

    def test_get_planned_accounts(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        assert portfolio.get_planned_accounts() == [acc]

    def test_planned_not_in_investment_accounts(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        assert portfolio.get_investment_cash_balance() == 0.0
        assert portfolio.get_investment_holdings() == {}

    def test_get_planned_transactions(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        txns = portfolio.get_planned_transactions()
        assert len(txns) == 3

    def test_get_planned_cash_balance(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        # initial_balance + deposit(3000) + deposit(2000) - buy(20*95 + 5)
        expected = 3000.0 + 3000.0 + 2000.0 - (20.0 * 95.0 + 5.0)
        assert pytest.approx(portfolio.get_planned_cash_balance()) == expected

    def test_get_planned_holdings(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        holdings = portfolio.get_planned_holdings()
        assert "VT" in holdings
        assert pytest.approx(holdings["VT"].quantity) == 20.0

    def test_get_planned_symbols(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        assert "VT" in portfolio.get_planned_symbols()

    def test_get_planned_expenses(self):
        acc = self._make_planned_account()
        portfolio = Portfolio([acc])
        expenses = portfolio.get_planned_expenses()
        assert len(expenses) == 2
        names = [pe.name for _, pe in expenses]
        assert "House Down Payment" in names
        assert "New Car" in names

    def test_get_planned_expenses_empty(self):
        acc = Account(name="No Goals", type="planned", initial_balance=0.0, transactions=[])
        portfolio = Portfolio([acc])
        assert portfolio.get_planned_expenses() == []


class TestEmergencyAccount:
    """Test Portfolio methods for emergency fund accounts."""

    def _make_emergency_account(self) -> Account:
        return Account(
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
                Transaction(type="withdrawal", date=date(2024, 4, 15), amount=2000.0),
            ],
        )

    def test_get_emergency_accounts(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        assert portfolio.get_emergency_accounts() == [acc]

    def test_emergency_not_in_investment(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        assert portfolio.get_investment_cash_balance() == 0.0
        assert portfolio.get_investment_holdings() == {}

    def test_get_emergency_transactions(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        assert len(portfolio.get_emergency_transactions()) == 4

    def test_get_emergency_cash_balance(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        # initial(1000) + deposit(2000) - buy(10*100+5) + deposit(1500) - withdrawal(2000)
        expected = 1000.0 + 2000.0 - (10.0 * 100.0 + 5.0) + 1500.0 - 2000.0
        assert pytest.approx(portfolio.get_emergency_cash_balance()) == expected

    def test_get_emergency_holdings(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        holdings = portfolio.get_emergency_holdings()
        assert "AGG" in holdings
        assert pytest.approx(holdings["AGG"].quantity) == 10.0

    def test_get_emergency_symbols(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        assert "AGG" in portfolio.get_emergency_symbols()

    def test_get_emergency_target_capital(self):
        acc = self._make_emergency_account()
        portfolio = Portfolio([acc])
        assert portfolio.get_emergency_target_capital() == 4000.0

    def test_get_emergency_target_capital_none(self):
        acc = Account(name="No Target", type="emergency", initial_balance=0.0, transactions=[])
        portfolio = Portfolio([acc])
        assert portfolio.get_emergency_target_capital() is None

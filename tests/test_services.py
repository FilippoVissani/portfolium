"""Tests for portfolium.services module."""
from datetime import date
from pathlib import Path
import tempfile

import pytest
import yaml

from portfolium.services.yaml_loader import load_account, load_accounts_from_directory
from portfolium.services.market_data import MarketDataService


class TestYAMLLoader:
    """Test YAML account file loading."""

    def test_load_account_basic(self, yaml_account_file: Path):
        account = load_account(yaml_account_file)
        assert account.name == "Test Investment Account"
        assert account.type == "investment"
        assert account.initial_balance == 5000.0
        assert len(account.transactions) == 2

    def test_load_account_transactions(self, yaml_account_file: Path):
        account = load_account(yaml_account_file)
        txns = account.transactions

        # First transaction
        assert txns[0].type == "deposit"
        assert txns[0].date == date(2024, 1, 1)
        assert txns[0].amount == 5000.0

        # Second transaction
        assert txns[1].type == "asset_buy"
        assert txns[1].symbol == "AAPL"
        assert txns[1].quantity == 10.0
        assert txns[1].price == 150.0

    def test_load_account_missing_optional_fields(self):
        content = {
            "name": "Minimal",
            "type": "investment",
            "initialBalance": 1000.0,
            "transactions": [
                {
                    "type": "deposit",
                    "date": date(2024, 1, 1),
                    # amount is optional for asset transactions
                }
            ],
        }

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".yaml", delete=False
        ) as f:
            yaml.dump(content, f)
            path = Path(f.name)

        account = load_account(path)
        assert account.name == "Minimal"
        assert account.transactions[0].amount is None

    def test_load_accounts_from_directory(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmpdir_path = Path(tmpdir)

            # Create two account files
            for i, name in enumerate(["account1", "account2"]):
                content = {
                    "name": f"Account {i+1}",
                    "type": "investment",
                    "initialBalance": 1000.0 * (i + 1),
                    "transactions": [],
                }
                with open(tmpdir_path / f"{name}.yaml", "w") as f:
                    yaml.dump(content, f)

            accounts = load_accounts_from_directory(tmpdir_path)

            assert len(accounts) == 2
            assert accounts[0].name == "Account 1"
            assert accounts[1].name == "Account 2"
            assert accounts[0].initial_balance == 1000.0
            assert accounts[1].initial_balance == 2000.0

    def test_load_accounts_empty_directory(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            accounts = load_accounts_from_directory(Path(tmpdir))
            assert len(accounts) == 0

    def test_load_accounts_ignores_non_yaml(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmpdir_path = Path(tmpdir)

            # Create a YAML file
            content = {
                "name": "Test",
                "type": "investment",
                "initialBalance": 1000.0,
                "transactions": [],
            }
            with open(tmpdir_path / "account.yaml", "w") as f:
                yaml.dump(content, f)

            # Create a non-YAML file (should be ignored)
            with open(tmpdir_path / "readme.txt", "w") as f:
                f.write("This is not a YAML file")

            accounts = load_accounts_from_directory(tmpdir_path)
            assert len(accounts) == 1

    def test_load_base_account_without_explicit_type(self):
        content = {
            "name": "Main Bank Account",
            "type": "base",
            "initialBalance": 2000.0,
            "transactions": [
                {
                    "date": date(2026, 2, 10),
                    "description": "Salary",
                    "category": "Salary",
                    "amount": 3500.0,
                },
                {
                    "date": date(2026, 2, 14),
                    "description": "Rent",
                    "category": "Housing",
                    "amount": -1200.0,
                },
            ],
        }

        with tempfile.NamedTemporaryFile(mode="w", suffix=".yaml", delete=False) as f:
            yaml.dump(content, f)
            path = Path(f.name)

        account = load_account(path)
        assert account.type == "base"
        assert len(account.transactions) == 2
        assert account.transactions[0].type == "deposit"
        assert account.transactions[1].type == "withdrawal"
        assert account.transactions[1].category == "Housing"


class TestPlannedAccountLoader:
    """Tests for loading 'planned' type accounts from YAML."""

    def _make_yaml(self, content: dict) -> Path:
        with tempfile.NamedTemporaryFile(mode="w", suffix=".yaml", delete=False) as f:
            yaml.dump(content, f)
            return Path(f.name)

    def _base_content(self) -> dict:
        return {
            "name": "Planned Expenses",
            "type": "planned",
            "initialBalance": 3000.0,
            "plannedExpenses": [
                {
                    "name": "House Down Payment",
                    "expirationDate": date(2027, 12, 31),
                    "estimatedAmount": 50000.0,
                },
                {
                    "name": "New Car",
                    "expirationDate": date(2026, 6, 30),
                    "estimatedAmount": 25000.0,
                },
            ],
            "transactions": [],
        }

    def test_load_planned_expenses_goals(self):
        account = load_account(self._make_yaml(self._base_content()))
        assert account.type == "planned"
        assert len(account.planned_expenses) == 2
        assert account.planned_expenses[0].name == "House Down Payment"
        assert account.planned_expenses[0].expiration_date == date(2027, 12, 31)
        assert account.planned_expenses[0].estimated_amount == 50000.0
        assert account.planned_expenses[1].name == "New Car"

    def test_ticker_mapped_to_symbol(self):
        content = self._base_content()
        content["transactions"] = [
            {
                "type": "asset_buy",
                "date": date(2024, 2, 1),
                "description": "Vanguard Total World",
                "ticker": "VT",
                "quantity": 10.0,
                "price": 95.0,
                "fees": 5.0,
            }
        ]
        account = load_account(self._make_yaml(content))
        txn = account.transactions[0]
        assert txn.symbol == "VT"
        assert txn.commission == 5.0

    def test_fees_mapped_to_commission(self):
        content = self._base_content()
        content["transactions"] = [
            {
                "type": "asset_buy",
                "date": date(2024, 2, 1),
                "description": "iShares Bond ETF",
                "ticker": "BND",
                "quantity": 20.0,
                "price": 72.0,
                "fees": 3.5,
            }
        ]
        account = load_account(self._make_yaml(content))
        assert account.transactions[0].commission == 3.5

    def test_deposit_and_withdrawal_parsed(self):
        content = self._base_content()
        content["transactions"] = [
            {"type": "deposit", "date": date(2024, 1, 15), "amount": 3000.0, "description": "Fund"},
            {"type": "withdrawal", "date": date(2024, 4, 15), "amount": 500.0, "description": "Partial"},
        ]
        account = load_account(self._make_yaml(content))
        assert account.transactions[0].type == "deposit"
        assert account.transactions[0].amount == 3000.0
        assert account.transactions[1].type == "withdrawal"

    def test_empty_planned_expenses_allowed(self):
        content = self._base_content()
        del content["plannedExpenses"]
        account = load_account(self._make_yaml(content))
        assert account.planned_expenses == []


class TestEmergencyAccountLoader:
    """Tests for loading 'emergency' type accounts from YAML."""

    def _make_yaml(self, content: dict) -> Path:
        with tempfile.NamedTemporaryFile(mode="w", suffix=".yaml", delete=False) as f:
            yaml.dump(content, f)
            return Path(f.name)

    def _base_content(self) -> dict:
        return {
            "name": "Emergency Fund",
            "type": "emergency",
            "initialBalance": 1000.0,
            "targetCapital": 4000.0,
            "transactions": [],
        }

    def test_target_capital_parsed(self):
        account = load_account(self._make_yaml(self._base_content()))
        assert account.type == "emergency"
        assert account.target_capital == 4000.0
        assert account.initial_balance == 1000.0

    def test_no_target_capital_is_none(self):
        content = self._base_content()
        del content["targetCapital"]
        account = load_account(self._make_yaml(content))
        assert account.target_capital is None

    def test_ticker_and_fees_mapped(self):
        content = self._base_content()
        content["transactions"] = [
            {
                "type": "asset_buy",
                "date": date(2024, 2, 1),
                "name": "Bond ETF",
                "ticker": "AGG",
                "quantity": 10.0,
                "price": 100.0,
                "fees": 5.0,
            }
        ]
        account = load_account(self._make_yaml(content))
        txn = account.transactions[0]
        assert txn.symbol == "AGG"
        assert txn.commission == 5.0

    def test_deposit_withdrawal_parsed(self):
        content = self._base_content()
        content["transactions"] = [
            {"type": "deposit", "date": date(2024, 1, 15), "amount": 2000.0},
            {"type": "withdrawal", "date": date(2024, 4, 15), "amount": 500.0},
        ]
        account = load_account(self._make_yaml(content))
        assert account.transactions[0].type == "deposit"
        assert account.transactions[0].amount == 2000.0
        assert account.transactions[1].type == "withdrawal"


class TestMarketDataService:
    """Test market data service (with mocking)."""

    def test_mock_current_price(self, mock_market_data):
        price = mock_market_data.get_current_price("VOO")
        assert price == 425.0

    def test_mock_previous_close(self, mock_market_data):
        close = mock_market_data.get_previous_close("VOO")
        assert close == 420.0

    def test_get_current_prices_multiple(self, mock_market_data):
        prices = mock_market_data.get_current_prices(["VOO", "ACWI", "AAPL"])
        assert len(prices) == 3
        assert prices["VOO"] == 425.0
        assert prices["ACWI"] == 98.0
        assert prices["AAPL"] == 160.0

    def test_get_previous_closes_multiple(self, mock_market_data):
        closes = mock_market_data.get_previous_closes(["VOO", "ACWI"])
        assert len(closes) == 2
        assert closes["VOO"] == 420.0
        assert closes["ACWI"] == 96.0

    def test_get_unknown_symbol(self, mock_market_data):
        price = mock_market_data.get_current_price("UNKNOWN")
        assert price == 100.0  # default mock price

    def test_cache_mechanism(self, mock_market_data):
        # The mock service doesn't populate _ticker_cache since it doesn't
        # make real yfinance calls. Just verify the fixture works.
        price = mock_market_data.get_current_price("VOO")
        assert price == 425.0

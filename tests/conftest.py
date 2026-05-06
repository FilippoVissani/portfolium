"""Shared test fixtures and utilities."""
from datetime import date
from pathlib import Path
from typing import List

import pytest
import tempfile
import yaml

from portfolium.models.account import Account, Transaction
from portfolium.models.portfolio import Portfolio
from portfolium.services.market_data import MarketDataService


@pytest.fixture
def sample_transactions() -> List[Transaction]:
    """Create sample transactions for testing (initial_balance covers the starting capital)."""
    return [
        Transaction(
            type="asset_buy",
            date=date(2024, 1, 15),
            name="Vanguard S&P 500 ETF",
            symbol="VOO",
            quantity=10.0,
            price=400.0,
            commission=5.0,
        ),
        Transaction(
            type="deposit",
            date=date(2024, 2, 15),
            amount=2000.0,
            description="Monthly investment",
        ),
        Transaction(
            type="asset_buy",
            date=date(2024, 2, 20),
            name="iShares MSCI ACWI ETF",
            symbol="ACWI",
            quantity=20.0,
            price=95.0,
            commission=5.0,
        ),
        Transaction(
            type="asset_sell",
            date=date(2024, 4, 10),
            name="Vanguard S&P 500 ETF",
            symbol="VOO",
            quantity=5.0,
            price=420.0,
            commission=5.0,
        ),
        Transaction(
            type="withdrawal",
            date=date(2024, 4, 15),
            amount=2000.0,
            description="Partial withdrawal",
        ),
    ]


@pytest.fixture
def sample_account(sample_transactions: List[Transaction]) -> Account:
    """Create a sample account for testing."""
    return Account(
        name="Test Portfolio",
        type="investment",
        initial_balance=5000.0,
        transactions=sample_transactions,
    )


@pytest.fixture
def sample_portfolio(sample_account: Account) -> Portfolio:
    """Create a sample portfolio with one account."""
    return Portfolio([sample_account])


@pytest.fixture
def yaml_account_file() -> Path:
    """Create a temporary YAML account file and return its path."""
    content = {
        "name": "Test Investment Account",
        "type": "investment",
        "initialBalance": 5000.0,
        "transactions": [
            {
                "type": "deposit",
                "date": date(2024, 1, 1),
                "amount": 5000.0,
                "description": "Initial capital",
            },
            {
                "type": "asset_buy",
                "date": date(2024, 1, 15),
                "name": "Apple Inc",
                "symbol": "AAPL",
                "quantity": 10.0,
                "price": 150.0,
                "commission": 5.0,
            },
        ],
    }

    with tempfile.NamedTemporaryFile(
        mode="w", suffix=".yaml", delete=False
    ) as f:
        yaml.dump(content, f, default_flow_style=False)
        return Path(f.name)


@pytest.fixture
def mock_market_data(monkeypatch):
    """Mock market data service to avoid network calls."""
    class MockMarketDataService(MarketDataService):
        def get_current_price(self, symbol: str) -> float:
            prices = {"VOO": 425.0, "ACWI": 98.0, "AAPL": 160.0}
            return prices.get(symbol, 100.0)

        def get_previous_close(self, symbol: str) -> float:
            closes = {"VOO": 420.0, "ACWI": 96.0, "AAPL": 158.0}
            return closes.get(symbol, 100.0)

    return MockMarketDataService()


# Export for use in tests
class MockMarketDataService(MarketDataService):
    """Re-exported mock service for test imports."""
    def get_current_price(self, symbol: str) -> float:
        prices = {"VOO": 425.0, "ACWI": 98.0, "AAPL": 160.0}
        return prices.get(symbol, 100.0)

    def get_previous_close(self, symbol: str) -> float:
        closes = {"VOO": 420.0, "ACWI": 96.0, "AAPL": 158.0}
        return closes.get(symbol, 100.0)

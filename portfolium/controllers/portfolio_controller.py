from datetime import date
from typing import Dict, List, Optional, Tuple

import pandas as pd

from ..models.portfolio import Portfolio
from ..services.market_data import MarketDataService


class AssetInfo:
    """View-model carrying all computed metrics for a single asset position."""

    def __init__(
        self,
        symbol: str,
        name: str,
        quantity: float,
        current_price: float,
        avg_cost: float,
        previous_close: float,
    ) -> None:
        self.symbol = symbol
        self.name = name
        self.quantity = quantity
        self.current_price = current_price
        self.avg_cost = avg_cost
        self.previous_close = previous_close

    @property
    def current_value(self) -> float:
        return self.quantity * self.current_price

    @property
    def cost_basis(self) -> float:
        return self.quantity * self.avg_cost

    @property
    def gain_loss_eur(self) -> float:
        return self.current_value - self.cost_basis

    @property
    def gain_loss_pct(self) -> float:
        return (self.gain_loss_eur / self.cost_basis * 100) if self.cost_basis else 0.0

    @property
    def intraday_gain_loss_eur(self) -> float:
        return self.quantity * (self.current_price - self.previous_close)


class PlannedExpenseProgress:
    """View-model for a single planned expense goal with computed progress metrics."""

    def __init__(
        self,
        name: str,
        expiration_date: date,
        estimated_amount: float,
        total_saved: float,
    ) -> None:
        self.name = name
        self.expiration_date = expiration_date
        self.estimated_amount = estimated_amount
        self.total_saved = total_saved

    @property
    def progress_pct(self) -> float:
        return (
            min(self.total_saved / self.estimated_amount * 100, 100.0)
            if self.estimated_amount
            else 0.0
        )

    @property
    def remaining_amount(self) -> float:
        return max(self.estimated_amount - self.total_saved, 0.0)

    @property
    def days_remaining(self) -> int:
        return max((self.expiration_date - date.today()).days, 0)

    @property
    def is_completed(self) -> bool:
        return self.total_saved >= self.estimated_amount

    @property
    def is_overdue(self) -> bool:
        return date.today() > self.expiration_date and not self.is_completed


class EmergencyFundStatus:
    """View-model for the emergency fund account showing progress toward target capital."""

    def __init__(
        self,
        current_value: float,
        target_capital: Optional[float],
    ) -> None:
        self.current_value = current_value
        self.target_capital = target_capital

    @property
    def coverage_pct(self) -> float:
        if not self.target_capital:
            return 0.0
        return min(self.current_value / self.target_capital * 100, 100.0)

    @property
    def remaining_amount(self) -> float:
        if not self.target_capital:
            return 0.0
        return max(self.target_capital - self.current_value, 0.0)

    @property
    def is_funded(self) -> bool:
        return (
            self.target_capital is not None
            and self.current_value >= self.target_capital
        )


class AggregatedAccountInfo:
    """View-model for a single account row in the all-accounts dashboard."""

    def __init__(
        self,
        name: str,
        account_type: str,
        cash_balance: float,
        assets_value: float,
    ) -> None:
        self.name = name
        self.account_type = account_type
        self.cash_balance = cash_balance
        self.assets_value = assets_value

    @property
    def total_value(self) -> float:
        return self.cash_balance + self.assets_value


class PortfolioController:
    """
    MVC Controller – orchestrates the Portfolio model and MarketDataService,
    then provides computed data to the views.
    """

    def __init__(self, portfolio: Portfolio, market_data: MarketDataService) -> None:
        self.portfolio = portfolio
        self.market_data = market_data
        self._prices: Dict[str, float] = {}
        self._prev_closes: Dict[str, float] = {}

    # ------------------------------------------------------------------ #
    # Market data refresh                                                  #
    # ------------------------------------------------------------------ #

    def refresh_market_data(self) -> None:
        """Pull the latest prices for all currently held symbols across all account types."""
        symbols = list(
            set(self.portfolio.get_investment_symbols())
            | set(self.portfolio.get_planned_symbols())
            | set(self.portfolio.get_emergency_symbols())
        )
        self._prices = self.market_data.get_current_prices(symbols)
        self._prev_closes = self.market_data.get_previous_closes(symbols)

    # ------------------------------------------------------------------ #
    # Asset-level data                                                     #
    # ------------------------------------------------------------------ #

    def get_investment_asset_infos(self) -> List[AssetInfo]:
        infos: List[AssetInfo] = []
        for sym, holding in self.portfolio.get_investment_holdings().items():
            price = self._prices.get(sym, holding.avg_cost)
            prev = self._prev_closes.get(sym, price)
            infos.append(
                AssetInfo(
                    symbol=sym,
                    name=holding.name,
                    quantity=holding.quantity,
                    current_price=price,
                    avg_cost=holding.avg_cost,
                    previous_close=prev,
                )
            )
        return infos

    # ------------------------------------------------------------------ #
    # Portfolio-level data                                                 #
    # ------------------------------------------------------------------ #

    def get_investment_cash_balance(self) -> float:
        return self.portfolio.get_investment_cash_balance()

    def get_total_investment_value(self) -> float:
        return (
            sum(a.current_value for a in self.get_investment_asset_infos())
            + self.get_investment_cash_balance()
        )

    def get_total_investment_gain_loss(self) -> Tuple[float, float]:
        """Return (gain_loss_eur, gain_loss_pct) for the entire portfolio."""
        infos = self.get_investment_asset_infos()
        gain = sum(a.gain_loss_eur for a in infos)
        cost = sum(a.cost_basis for a in infos)
        pct = (gain / cost * 100) if cost else 0.0
        return gain, pct

    def get_investment_allocation_data(self) -> Dict[str, float]:
        """Return {label: current_value} suitable for a pie chart."""
        data = {a.symbol: a.current_value for a in self.get_investment_asset_infos()}
        cash = self.get_investment_cash_balance()
        if cash > 0:
            data["Cash"] = cash
        return data

    # ------------------------------------------------------------------ #
    # Historical performance                                               #
    # ------------------------------------------------------------------ #

    def get_investment_historical_performance(
        self, start: date, end: date
    ) -> pd.Series:
        """
        Compute total portfolio value for every trading day in [start, end].
        Returns a pd.Series with a DatetimeIndex and float values.
        """
        all_symbols = self.portfolio.get_investment_all_symbols()
        if not all_symbols:
            return pd.Series(dtype=float)

        hist = self.market_data.get_historical_prices(all_symbols, start, end)
        if hist.empty:
            return pd.Series(dtype=float)

        values: List[float] = []
        for i, ts in enumerate(hist.index):
            d = ts.date() if hasattr(ts, "date") else ts
            holdings = self.portfolio.get_investment_holdings(up_to=d)
            cash = self.portfolio.get_investment_cash_balance(up_to=d)

            total = cash
            for sym, holding in holdings.items():
                if sym in hist.columns:
                    price_slice = hist[sym].iloc[: i + 1].dropna()
                    if not price_slice.empty:
                        total += holding.quantity * float(price_slice.iloc[-1])

            values.append(total)

        return pd.Series(values, index=hist.index, dtype=float)

    # ------------------------------------------------------------------ #
    # Base account analytics                                             #
    # ------------------------------------------------------------------ #

    def _base_accounts(self):
        return self.portfolio.get_base_accounts()

    def get_base_movements(self, start: date | None = None, end: date | None = None):
        return self.portfolio.get_base_movements(start, end)

    def get_base_summary(self, start: date, end: date) -> Dict[str, float]:
        return self.portfolio.get_base_summary(start, end)

    def get_base_expenses_by_category(self, start: date, end: date) -> Dict[str, float]:
        return self.portfolio.get_base_expenses_by_category(start, end)

    def get_base_monthly_cashflow(self, start: date, end: date) -> pd.DataFrame:
        return self.portfolio.get_base_monthly_cashflow(start, end)

    # ------------------------------------------------------------------ #
    # Planned account analytics                                            #
    # ------------------------------------------------------------------ #

    def get_planned_asset_infos(self) -> List[AssetInfo]:
        infos: List[AssetInfo] = []
        for sym, holding in self.portfolio.get_planned_holdings().items():
            price = self._prices.get(sym, holding.avg_cost)
            prev = self._prev_closes.get(sym, price)
            infos.append(
                AssetInfo(
                    symbol=sym,
                    name=holding.name,
                    quantity=holding.quantity,
                    current_price=price,
                    avg_cost=holding.avg_cost,
                    previous_close=prev,
                )
            )
        return infos

    def get_planned_cash_balance(self) -> float:
        return self.portfolio.get_planned_cash_balance()

    def get_total_planned_value(self) -> float:
        return (
            sum(a.current_value for a in self.get_planned_asset_infos())
            + self.get_planned_cash_balance()
        )

    def get_planned_allocation_data(self) -> Dict[str, float]:
        """Return {label: current_value} for planned holdings + cash."""
        data = {a.symbol: a.current_value for a in self.get_planned_asset_infos()}
        cash = self.get_planned_cash_balance()
        if cash > 0:
            data["Cash"] = cash
        return data

    def get_planned_expense_progress(self) -> List[PlannedExpenseProgress]:
        """
        Compute progress for every planned expense goal.
        The shared pool (total planned portfolio value) is used as the saved amount.
        """
        total_saved = self.get_total_planned_value()
        result: List[PlannedExpenseProgress] = []
        for _, expense in self.portfolio.get_planned_expenses():
            result.append(
                PlannedExpenseProgress(
                    name=expense.name,
                    expiration_date=expense.expiration_date,
                    estimated_amount=expense.estimated_amount,
                    total_saved=total_saved,
                )
            )
        return result

    # ------------------------------------------------------------------ #
    # Emergency fund analytics                                             #
    # ------------------------------------------------------------------ #

    def get_emergency_asset_infos(self) -> List[AssetInfo]:
        infos: List[AssetInfo] = []
        for sym, holding in self.portfolio.get_emergency_holdings().items():
            price = self._prices.get(sym, holding.avg_cost)
            prev = self._prev_closes.get(sym, price)
            infos.append(
                AssetInfo(
                    symbol=sym,
                    name=holding.name,
                    quantity=holding.quantity,
                    current_price=price,
                    avg_cost=holding.avg_cost,
                    previous_close=prev,
                )
            )
        return infos

    def get_emergency_cash_balance(self) -> float:
        return self.portfolio.get_emergency_cash_balance()

    def get_total_emergency_value(self) -> float:
        return (
            sum(a.current_value for a in self.get_emergency_asset_infos())
            + self.get_emergency_cash_balance()
        )

    def get_emergency_allocation_data(self) -> Dict[str, float]:
        """Return {label: current_value} for emergency holdings + cash."""
        data = {a.symbol: a.current_value for a in self.get_emergency_asset_infos()}
        cash = self.get_emergency_cash_balance()
        if cash > 0:
            data["Cash"] = cash
        return data

    def get_emergency_fund_status(self) -> EmergencyFundStatus:
        """Compute progress toward the emergency fund target capital."""
        return EmergencyFundStatus(
            current_value=self.get_total_emergency_value(),
            target_capital=self.portfolio.get_emergency_target_capital(),
        )

    # ------------------------------------------------------------------ #
    # All-accounts aggregates                                              #
    # ------------------------------------------------------------------ #

    def get_all_accounts_infos(self) -> List[AggregatedAccountInfo]:
        infos: List[AggregatedAccountInfo] = []
        for account in self.portfolio.get_all_accounts():
            cash_balance = self.portfolio.get_account_cash_balance(account)
            assets_value = 0.0
            for sym, holding in self.portfolio.get_account_holdings(account).items():
                price = self._prices.get(sym, holding.avg_cost)
                assets_value += holding.quantity * price

            infos.append(
                AggregatedAccountInfo(
                    name=account.name,
                    account_type=account.type,
                    cash_balance=cash_balance,
                    assets_value=assets_value,
                )
            )

        return sorted(infos, key=lambda info: info.total_value, reverse=True)

    def get_all_accounts_total_value(self) -> float:
        return sum(info.total_value for info in self.get_all_accounts_infos())

    def get_all_accounts_total_cash(self) -> float:
        return sum(info.cash_balance for info in self.get_all_accounts_infos())

    def get_all_accounts_total_assets_value(self) -> float:
        return sum(info.assets_value for info in self.get_all_accounts_infos())

    def get_all_accounts_breakdown_by_type(self) -> Dict[str, float]:
        breakdown: Dict[str, float] = {}
        for info in self.get_all_accounts_infos():
            label = info.account_type.capitalize()
            breakdown[label] = breakdown.get(label, 0.0) + info.total_value
        return {k: v for k, v in breakdown.items() if v > 0}

    def get_all_accounts_allocation_data(self) -> Dict[str, float]:
        """Return {label: value} for all held assets across accounts plus global cash."""
        data: Dict[str, float] = {}
        total_cash = 0.0

        for account in self.portfolio.get_all_accounts():
            total_cash += self.portfolio.get_account_cash_balance(account)
            for sym, holding in self.portfolio.get_account_holdings(account).items():
                price = self._prices.get(sym, holding.avg_cost)
                data[sym] = data.get(sym, 0.0) + (holding.quantity * price)

        if total_cash > 0:
            data["Cash"] = total_cash

        return {k: v for k, v in data.items() if v > 0}

    def get_all_accounts_type_counts(self) -> Dict[str, int]:
        return self.portfolio.get_account_type_counts()

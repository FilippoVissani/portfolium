from dataclasses import dataclass
from datetime import date
from typing import Dict, List, Optional, Tuple

import pandas as pd

from .account import Account, PlannedExpense, Transaction


@dataclass
class Holding:
    """A current open asset position."""

    symbol: str
    name: str
    quantity: float
    avg_cost: float  # weighted-average cost per unit, including pro-rated commissions

    @property
    def cost_basis(self) -> float:
        return self.quantity * self.avg_cost


class Portfolio:
    """Aggregates one or more accounts and computes holdings and cash positions."""

    def __init__(self, accounts: List[Account]) -> None:
        self.accounts = accounts
        self._investment_accounts = [
            acc for acc in accounts if acc.type == "investment"
        ]
        self._base_accounts = [acc for acc in accounts if acc.type == "base"]
        self._planned_accounts = [acc for acc in accounts if acc.type == "planned"]
        self._emergency_accounts = [acc for acc in accounts if acc.type == "emergency"]

    # ------------------------------------------------------------------ #
    # Investment account data                                              #
    # ------------------------------------------------------------------ #

    def get_investment_transactions(
        self, up_to: Optional[date] = None
    ) -> List[Transaction]:
        """Return investment-account transactions sorted by date, optionally filtered."""
        txns: List[Transaction] = [
            txn
            for acc in self._investment_accounts
            for txn in acc.transactions
            if up_to is None or txn.date <= up_to
        ]
        return sorted(txns, key=lambda t: t.date)

    # ------------------------------------------------------------------ #
    # Investment holdings                                                  #
    # ------------------------------------------------------------------ #

    def get_investment_holdings(
        self, up_to: Optional[date] = None
    ) -> Dict[str, Holding]:
        """Compute open positions via weighted-average cost method."""
        state: Dict[str, Dict] = {}  # symbol -> {name, quantity, total_cost}

        for txn in self.get_investment_transactions(up_to):
            if txn.type == "asset_buy":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                cost = (txn.price or 0.0) * qty + (txn.commission or 0.0)
                if sym not in state:
                    state[sym] = {
                        "name": txn.name or sym,
                        "quantity": 0.0,
                        "total_cost": 0.0,
                    }
                state[sym]["quantity"] += qty
                state[sym]["total_cost"] += cost

            elif txn.type == "asset_sell":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                if sym in state and state[sym]["quantity"] > 0:
                    ratio = min(qty / state[sym]["quantity"], 1.0)
                    state[sym]["total_cost"] *= 1.0 - ratio
                    state[sym]["quantity"] -= qty
                    if state[sym]["quantity"] < 1e-9:
                        del state[sym]

        result: Dict[str, Holding] = {}
        for sym, data in state.items():
            if data["quantity"] > 1e-9:
                avg = data["total_cost"] / data["quantity"]
                result[sym] = Holding(
                    symbol=sym,
                    name=data["name"],
                    quantity=data["quantity"],
                    avg_cost=avg,
                )
        return result

    # ------------------------------------------------------------------ #
    # Cash                                                                  #
    # ------------------------------------------------------------------ #

    def get_investment_cash_balance(self, up_to: Optional[date] = None) -> float:
        """Compute the net cash balance across investment accounts only."""
        balance = sum(acc.initial_balance for acc in self._investment_accounts)
        for txn in self.get_investment_transactions(up_to):
            if txn.type == "deposit":
                balance += txn.amount or 0.0
            elif txn.type == "withdrawal":
                balance -= txn.amount or 0.0
            elif txn.type == "asset_buy":
                balance -= (txn.price or 0.0) * (txn.quantity or 0.0) + (
                    txn.commission or 0.0
                )
            elif txn.type == "asset_sell":
                balance += (txn.price or 0.0) * (txn.quantity or 0.0) - (
                    txn.commission or 0.0
                )
        return balance

    def get_base_cash_balance(self, up_to: Optional[date] = None) -> float:
        """Compute base account balance from initial balance plus signed movements."""
        balance = sum(acc.initial_balance for acc in self._base_accounts)
        for _, txn in self.get_base_movements(end=up_to):
            balance += txn.amount or 0.0
        return balance

    def get_planned_cash_balance(self, up_to: Optional[date] = None) -> float:
        """Compute net cash balance across planned accounts."""
        balance = sum(acc.initial_balance for acc in self._planned_accounts)
        for txn in self.get_planned_transactions(up_to):
            if txn.type == "deposit":
                balance += txn.amount or 0.0
            elif txn.type == "withdrawal":
                balance -= txn.amount or 0.0
            elif txn.type == "asset_buy":
                balance -= (txn.price or 0.0) * (txn.quantity or 0.0) + (
                    txn.commission or 0.0
                )
            elif txn.type == "asset_sell":
                balance += (txn.price or 0.0) * (txn.quantity or 0.0) - (
                    txn.commission or 0.0
                )
        return balance

    def get_emergency_cash_balance(self, up_to: Optional[date] = None) -> float:
        """Compute net cash balance across emergency accounts."""
        balance = sum(acc.initial_balance for acc in self._emergency_accounts)
        for txn in self.get_emergency_transactions(up_to):
            if txn.type == "deposit":
                balance += txn.amount or 0.0
            elif txn.type == "withdrawal":
                balance -= txn.amount or 0.0
            elif txn.type == "asset_buy":
                balance -= (txn.price or 0.0) * (txn.quantity or 0.0) + (
                    txn.commission or 0.0
                )
            elif txn.type == "asset_sell":
                balance += (txn.price or 0.0) * (txn.quantity or 0.0) - (
                    txn.commission or 0.0
                )
        return balance

    def get_total_cash_balance(self, up_to: Optional[date] = None) -> float:
        """Compute aggregated cash balance across all account types."""
        return (
            self.get_investment_cash_balance(up_to)
            + self.get_base_cash_balance(up_to)
            + self.get_planned_cash_balance(up_to)
            + self.get_emergency_cash_balance(up_to)
        )

    # ------------------------------------------------------------------ #
    # Investment symbols                                                    #
    # ------------------------------------------------------------------ #

    def get_investment_symbols(self) -> List[str]:
        """Symbols of currently open positions."""
        return list(self.get_investment_holdings().keys())

    def get_investment_all_symbols(self) -> List[str]:
        """All symbols ever traded (including fully exited positions)."""
        return list({t.symbol for t in self.get_investment_transactions() if t.symbol})

    # ------------------------------------------------------------------ #
    # Planned account data                                                 #
    # ------------------------------------------------------------------ #

    def get_planned_accounts(self) -> List[Account]:
        return self._planned_accounts

    def get_planned_transactions(
        self, up_to: Optional[date] = None
    ) -> List[Transaction]:
        """Return planned-account transactions sorted by date, optionally filtered."""
        txns: List[Transaction] = [
            txn
            for acc in self._planned_accounts
            for txn in acc.transactions
            if up_to is None or txn.date <= up_to
        ]
        return sorted(txns, key=lambda t: t.date)

    def get_planned_holdings(self, up_to: Optional[date] = None) -> Dict[str, Holding]:
        """Compute open positions in planned accounts via weighted-average cost."""
        state: Dict[str, Dict] = {}

        for txn in self.get_planned_transactions(up_to):
            if txn.type == "asset_buy":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                cost = (txn.price or 0.0) * qty + (txn.commission or 0.0)
                if sym not in state:
                    state[sym] = {
                        "name": txn.name or sym,
                        "quantity": 0.0,
                        "total_cost": 0.0,
                    }
                state[sym]["quantity"] += qty
                state[sym]["total_cost"] += cost

            elif txn.type == "asset_sell":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                if sym in state and state[sym]["quantity"] > 0:
                    ratio = min(qty / state[sym]["quantity"], 1.0)
                    state[sym]["total_cost"] *= 1.0 - ratio
                    state[sym]["quantity"] -= qty
                    if state[sym]["quantity"] < 1e-9:
                        del state[sym]

        result: Dict[str, Holding] = {}
        for sym, data in state.items():
            if data["quantity"] > 1e-9:
                avg = data["total_cost"] / data["quantity"]
                result[sym] = Holding(
                    symbol=sym,
                    name=data["name"],
                    quantity=data["quantity"],
                    avg_cost=avg,
                )
        return result

    def get_planned_symbols(self) -> List[str]:
        """Symbols of currently open positions in planned accounts."""
        return list(self.get_planned_holdings().keys())

    def get_planned_all_symbols(self) -> List[str]:
        """All symbols ever traded in planned accounts."""
        return list({t.symbol for t in self.get_planned_transactions() if t.symbol})

    def get_planned_expenses(self) -> List[Tuple[str, PlannedExpense]]:
        """Return all planned expense goals as (account_name, PlannedExpense) pairs."""
        return [
            (acc.name, pe)
            for acc in self._planned_accounts
            for pe in acc.planned_expenses
        ]

    # ------------------------------------------------------------------ #
    # Emergency fund account data                                          #
    # ------------------------------------------------------------------ #

    def get_emergency_accounts(self) -> List[Account]:
        return self._emergency_accounts

    def get_emergency_transactions(
        self, up_to: Optional[date] = None
    ) -> List[Transaction]:
        """Return emergency-account transactions sorted by date, optionally filtered."""
        txns: List[Transaction] = [
            txn
            for acc in self._emergency_accounts
            for txn in acc.transactions
            if up_to is None or txn.date <= up_to
        ]
        return sorted(txns, key=lambda t: t.date)

    def get_emergency_holdings(
        self, up_to: Optional[date] = None
    ) -> Dict[str, Holding]:
        """Compute open positions in emergency accounts via weighted-average cost."""
        state: Dict[str, Dict] = {}

        for txn in self.get_emergency_transactions(up_to):
            if txn.type == "asset_buy":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                cost = (txn.price or 0.0) * qty + (txn.commission or 0.0)
                if sym not in state:
                    state[sym] = {
                        "name": txn.name or sym,
                        "quantity": 0.0,
                        "total_cost": 0.0,
                    }
                state[sym]["quantity"] += qty
                state[sym]["total_cost"] += cost

            elif txn.type == "asset_sell":
                sym = txn.symbol or ""
                qty = txn.quantity or 0.0
                if sym in state and state[sym]["quantity"] > 0:
                    ratio = min(qty / state[sym]["quantity"], 1.0)
                    state[sym]["total_cost"] *= 1.0 - ratio
                    state[sym]["quantity"] -= qty
                    if state[sym]["quantity"] < 1e-9:
                        del state[sym]

        result: Dict[str, Holding] = {}
        for sym, data in state.items():
            if data["quantity"] > 1e-9:
                avg = data["total_cost"] / data["quantity"]
                result[sym] = Holding(
                    symbol=sym,
                    name=data["name"],
                    quantity=data["quantity"],
                    avg_cost=avg,
                )
        return result

    def get_emergency_symbols(self) -> List[str]:
        """Symbols of currently open positions in emergency accounts."""
        return list(self.get_emergency_holdings().keys())

    def get_emergency_all_symbols(self) -> List[str]:
        """All symbols ever traded in emergency accounts."""
        return list({t.symbol for t in self.get_emergency_transactions() if t.symbol})

    def get_emergency_target_capital(self) -> Optional[float]:
        """Return the target capital of the first emergency account that defines it, or None."""
        for acc in self._emergency_accounts:
            if acc.target_capital is not None:
                return acc.target_capital
        return None

    # ------------------------------------------------------------------ #
    # Base account analytics                                               #
    # ------------------------------------------------------------------ #

    def get_base_accounts(self) -> List[Account]:
        return self._base_accounts

    def get_base_movements(
        self,
        start: Optional[date] = None,
        end: Optional[date] = None,
    ) -> List[tuple[str, Transaction]]:
        movements: List[tuple[str, Transaction]] = []
        for acc in self._base_accounts:
            for txn in acc.transactions:
                if txn.amount is None:
                    continue
                if start is not None and txn.date < start:
                    continue
                if end is not None and txn.date > end:
                    continue
                movements.append((acc.name, txn))
        return sorted(movements, key=lambda row: row[1].date)

    def get_base_summary(self, start: date, end: date) -> Dict[str, float]:
        movements = self.get_base_movements(start, end)
        amounts = [txn.amount or 0.0 for _, txn in movements]

        income = sum(a for a in amounts if a > 0)
        expenses = -sum(a for a in amounts if a < 0)
        savings = income - expenses

        month_span = max(
            1, (end.year - start.year) * 12 + (end.month - start.month) + 1
        )
        avg_monthly_expenses = expenses / month_span

        current_base_balance = self.get_base_cash_balance(end)

        return {
            "income": income,
            "expenses": expenses,
            "savings": savings,
            "avg_monthly_expenses": avg_monthly_expenses,
            "current_balance": current_base_balance,
        }

    def get_base_expenses_by_category(self, start: date, end: date) -> Dict[str, float]:
        categories: Dict[str, float] = {}
        for _, txn in self.get_base_movements(start, end):
            amount = txn.amount or 0.0
            if amount >= 0:
                continue
            key = txn.category or "Uncategorized"
            categories[key] = categories.get(key, 0.0) + abs(amount)
        return categories

    def get_base_monthly_cashflow(self, start: date, end: date) -> pd.DataFrame:
        rows = []
        for _, txn in self.get_base_movements(start, end):
            amount = txn.amount or 0.0
            month_key = f"{txn.date.year:04d}-{txn.date.month:02d}"
            rows.append(
                {
                    "month": month_key,
                    "income": amount if amount > 0 else 0.0,
                    "expenses": abs(amount) if amount < 0 else 0.0,
                }
            )

        if not rows:
            return pd.DataFrame(columns=["income", "expenses", "savings"])

        df = pd.DataFrame(rows)
        grouped = (
            df.groupby("month", as_index=True)[["income", "expenses"]]
            .sum()
            .sort_index()
        )
        grouped["savings"] = grouped["income"] - grouped["expenses"]
        return grouped

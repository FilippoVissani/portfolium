from datetime import date, datetime
from pathlib import Path
from typing import List

import yaml

from ..models.account import Account, PlannedExpense, Transaction


def _parse_date(raw_date) -> date:
    if isinstance(raw_date, date):
        return raw_date
    return datetime.strptime(str(raw_date), "%Y-%m-%d").date()


def load_account(file_path: Path) -> Account:
    """Load a single account from a YAML file."""
    with open(file_path, "r", encoding="utf-8") as fh:
        data = yaml.safe_load(fh)

    account_type = data["type"]

    # ── Planned expenses goals (only present in "planned" accounts) ──── #
    planned_expenses: List[PlannedExpense] = []
    for raw in data.get("plannedExpenses", []):
        planned_expenses.append(
            PlannedExpense(
                name=raw["name"],
                expiration_date=_parse_date(raw["expirationDate"]),
                estimated_amount=float(raw["estimatedAmount"]),
            )
        )

    # ── Transactions ─────────────────────────────────────────────────── #
    transactions: List[Transaction] = []
    for raw in data.get("transactions", []):
        txn_date = _parse_date(raw["date"])

        txn_type = raw.get("type")
        if account_type == "base" and txn_type is None:
            amt = raw.get("amount")
            if amt is not None:
                txn_type = "deposit" if float(amt) >= 0 else "withdrawal"

        # "planned" and "emergency" accounts use "ticker" and "fees" in place of
        # "symbol" and "commission" used by investment accounts.
        symbol = raw.get("symbol") or raw.get("ticker")
        commission = (
            float(raw["commission"])
            if "commission" in raw
            else float(raw["fees"])
            if "fees" in raw
            else None
        )
        # asset description stored under "description" in planned YAMLs
        name = raw.get("name") or (
            raw.get("description") if txn_type in ("asset_buy", "asset_sell") else None
        )
        description = (
            raw.get("description")
            if txn_type not in ("asset_buy", "asset_sell")
            else raw.get("description")
        )

        transactions.append(
            Transaction(
                type=txn_type,
                date=txn_date,
                amount=float(raw["amount"]) if "amount" in raw else None,
                description=description,
                category=raw.get("category"),
                name=name,
                symbol=symbol,
                quantity=float(raw["quantity"]) if "quantity" in raw else None,
                price=float(raw["price"]) if "price" in raw else None,
                commission=commission,
            )
        )

    return Account(
        name=data["name"],
        type=account_type,
        initial_balance=float(data.get("initialBalance", 0.0)),
        transactions=transactions,
        planned_expenses=planned_expenses,
        target_capital=float(data["targetCapital"])
        if "targetCapital" in data
        else None,
    )


def load_accounts_from_directory(directory: Path) -> List[Account]:
    """Load all *.yaml account files from *directory*."""
    directory = Path(directory)
    accounts: List[Account] = []
    for yaml_file in sorted(directory.glob("*.yaml")):
        accounts.append(load_account(yaml_file))
    return accounts

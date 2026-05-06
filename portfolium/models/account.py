from dataclasses import dataclass, field
from datetime import date
from typing import List, Optional


@dataclass
class Transaction:
    """A single financial transaction for investment or base bank accounts."""

    type: Optional[str]
    date: date
    amount: Optional[float] = None
    description: Optional[str] = None
    category: Optional[str] = None
    name: Optional[str] = None
    symbol: Optional[str] = None
    quantity: Optional[float] = None
    price: Optional[float] = None
    commission: Optional[float] = None


@dataclass
class PlannedExpense:
    """A single financial goal tracked inside a planned account."""

    name: str
    expiration_date: date
    estimated_amount: float


@dataclass
class Account:
    """A financial account loaded from a YAML file."""

    name: str
    type: str
    initial_balance: float
    transactions: List[Transaction] = field(default_factory=list)
    planned_expenses: List[PlannedExpense] = field(default_factory=list)
    target_capital: Optional[float] = None

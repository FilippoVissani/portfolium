<p align="center">
  <img src="portfolium-logo.png" alt="Portfolium logo" width="220" />
</p>

# Portfolium

Portfolium is a desktop personal finance application built with Python and PySide6.
It reads account data from YAML files, computes analytics in a model layer, and renders multiple finance dashboards in a GUI.

The application follows a clear Model-View-Controller (MVC) architecture:
- **Models**: domain entities and portfolio calculations
- **Views**: PySide6 widgets and pages
- **Controllers**: orchestration and view-model generation
- **Services**: YAML loading and market data retrieval

## Features

### Investment tracking
- Holdings table with P/L and intraday P/L
- Allocation pie chart
- Historical portfolio performance chart

### Base account analytics
- Income, expenses, savings, average monthly expenses
- Expense breakdown by category
- Monthly cashflow chart
- Movement table by period

### Planned expenses goals
- Goal progress cards with time remaining
- Funding coverage and goal-level progress metrics
- Planned account allocation and holdings table

### Emergency fund monitoring
- Emergency fund target-capital coverage
- Remaining amount to fund
- Emergency allocation and holdings details

### All accounts overview
- Aggregated total value, cash, and invested-assets KPIs
- Account-type allocation breakdown
- Per-account value table with portfolio share

### General
- Multi-account support via YAML files
- Automatic market data refresh (every 60 seconds)
- Unit tests for models, services, and controllers

## Tech stack

- Python 3.14+
- PySide6
- pandas, numpy
- pyqtgraph, matplotlib
- yfinance
- PyYAML
- pytest, pytest-qt

## Project structure

```text
portfolium/
  example_data/
    investment_bank_account.yaml
    main_bank_account.yaml
    planned_expenses_bank_account.yaml
    emergency_fund_bank_account.yaml
  portfolium/
    __main__.py
    controllers/
    models/
    services/
    views/
  tests/
  requirements.txt
```

## Installation

1. Create and activate a virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

2. Install dependencies:

```bash
pip install -r requirements.txt
```

## Run the app

Run and choose a data directory from the startup folder picker:

```bash
python -m portfolium
```

Run with a custom data directory passed as an argument:

```bash
python -m portfolium /path/to/your/yaml-directory
```

## Build a standalone executable (PyInstaller)

Install dependencies first, then build from the project root:

```bash
python -m PyInstaller --clean --noconfirm portfolium.spec
```

Build outputs are created in `dist/`:
- Linux/macOS: `dist/portfolium`
- Windows: `dist/portfolium.exe`

In CI release pipelines, each platform binary is also packaged as a zip artifact.

## YAML account formats

Each YAML file represents one account.

Common root fields:
- `name`
- `type`
- `initialBalance`
- `transactions`

### 1) Investment account (`type: investment`)

```yaml
name: "Investment Account"
type: "investment"
initialBalance: 5000.00
transactions:
  - type: deposit
    date: 2024-01-01
    amount: 5000.00
    description: "Initial capital"
  - type: asset_buy
    date: 2024-01-15
    name: "Apple Inc"
    symbol: "AAPL"
    quantity: 10
    price: 150.00
    commission: 5.00
```

### 2) Base account (`type: base`)

```yaml
name: "Main Bank Account"
type: "base"
initialBalance: 2000.00
transactions:
  - date: 2026-02-10
    description: "Salary"
    category: "Salary"
    amount: 3500.00
  - date: 2026-02-14
    description: "Rent"
    category: "Housing"
    amount: -1200.00
```

Notes:
- For base accounts, transaction `type` can be omitted.
- Type is inferred from `amount` sign: positive -> deposit, negative -> withdrawal.

### 3) Planned expenses account (`type: planned`)

```yaml
name: "Planned Expenses"
type: "planned"
initialBalance: 3000.00
plannedExpenses:
  - name: "House Down Payment"
    expirationDate: 2027-12-31
    estimatedAmount: 50000.00
transactions:
  - type: asset_buy
    date: 2024-02-01
    description: "Vanguard Total World Stock ETF"
    ticker: "VT"
    quantity: 20
    price: 95.00
    fees: 5.00
```

Notes:
- `ticker` is mapped internally to `symbol`.
- `fees` is mapped internally to `commission`.

### 4) Emergency fund account (`type: emergency`)

```yaml
name: "Emergency Fund"
type: "emergency"
initialBalance: 1000.00
targetCapital: 4000.00
transactions:
  - type: deposit
    date: 2024-01-15
    amount: 2000.00
    description: "Initial fund"
  - type: asset_buy
    date: 2024-02-01
    name: "iShares Core U.S. Aggregate Bond ETF"
    ticker: "AGG"
    quantity: 10
    price: 100.00
    fees: 5.00
```

Notes:
- `targetCapital` drives emergency-fund coverage analytics.
- `ticker` and `fees` are supported the same way as planned accounts.

## Architecture overview

### Model layer (`portfolium/models`)
- Data classes: `Transaction`, `PlannedExpense`, `Account`
- Core calculator: `Portfolio`
- Responsibilities:
  - Partition accounts by type (`investment`, `base`, `planned`, `emergency`)
  - Compute cash balances, holdings, symbols, and analytics
  - Keep business logic out of views and mostly out of controllers

### Service layer (`portfolium/services`)
- `yaml_loader.py`: parse YAML files into model objects
- `market_data.py`: wrap yfinance access and cache ticker objects

### Controller layer (`portfolium/controllers`)
- `PortfolioController`: orchestration and view-model generation
- Creates UI-ready structures such as asset info rows and goal/fund progress summaries

### View layer (`portfolium/views`)
- Main window with tabs:
  - Investments
  - All Accounts
  - Base Account
  - Planned Expenses
  - Emergency Fund
- Widget pages render charts, tables, KPI cards, and refresh from controller data

## Testing

Run all tests:

```bash
pytest tests -v
```

Current suite coverage includes:
- Model computations (cash, holdings, account-type isolation)
- YAML loading and schema mapping logic
- Controller-level analytics and view-model behavior

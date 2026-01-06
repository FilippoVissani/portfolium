# Portfolium

[![CI](https://github.com/filippovissani/portfolium/actions/workflows/ci.yml/badge.svg)](https://github.com/filippovissani/portfolium/actions/workflows/ci.yml)

A tiny Kotlin CLI that turns personal finance CSVs into a concise, human‑readable dashboard: liquidity, planned expenses, emergency fund, and investments. It reads your data from simple CSV files and prints a formatted summary in the terminal.

## Features

- Parse transactions and compute income, expenses, net, and 12‑month average monthly expense
- Track planned/predictable expenses with accrued amounts and coverage ratio
- Compute emergency fund target vs. current capital using the 12‑month average expense
- Summarize investments from transactions, including average price, current value, PnL, and portfolio weights
- Produce an aggregated "Total Net Worth" with invested vs. liquid split

## Project structure

- Application entrypoint: `src/main/kotlin/io/github/filippovissani/portfolium/Main.kt`
- Core logic: `src/main/kotlin/io/github/filippovissani/portfolium/logic/Calculators.kt`
- CSV parsing helpers: `src/main/kotlin/io/github/filippovissani/portfolium/csv/*`
- Data models: `src/main/kotlin/io/github/filippovissani/portfolium/model/Models.kt`
- Utility operators: `src/main/kotlin/io/github/filippovissani/portfolium/util/Money.kt`
- Example data: `data/*.csv`
- Tests: `src/test/kotlin/io/github/filippovissani/portfolium/*`

## Requirements

- Java 21 (used via Gradle toolchain)
- Gradle Wrapper (included)

## Build and run

- Build the project:

```zsh
./gradlew build
```

- Run the CLI with default CSV paths (using the bundled `data/*.csv`):

```zsh
./gradlew run
```

- Or pass custom file paths in order:

```zsh
./gradlew run --args="/path/to/transactions.csv /path/to/planned_expenses.csv /path/to/emergency_fund.csv /path/to/investments.csv /path/to/current_prices.csv"
```

You can also run the generated jar after building:

```zsh
java -jar build/libs/portfolium-1.0-SNAPSHOT.jar
# or with args
java -jar build/libs/portfolium-1.0-SNAPSHOT.jar /path/transactions.csv /path/planned_expenses.csv /path/emergency_fund.csv /path/investments.csv /path/current_prices.csv
```

## CSV formats

The app expects headers and values as described below. Extra columns are ignored; missing or invalid values fall back to sensible defaults where possible.

### 1) Transactions (`transactions.csv`)

Header: `date,description,type,category,method,amount,note`

- `type`: `income` or `expense`
- `amount`: income as positive, expense as negative
- `date`: supports formats like `YYYY-MM-DD`, `dd/MM/yyyy`, `MM/dd/yyyy`

Example:

```
2025-01-15,Salary,income,Job,Bank,1000,
2025-02-01,Groceries,expense,Food,Card,-200,
```

Outputs: total income, total expense (absolute), net, average monthly expense over last 12 months.

### 2) Planned expenses (`planned_expenses.csv`)

Header: `name,estimated_amount,horizon,due_date,accrued`

- `estimated_amount`: total expected amount
- `accrued`: amount set aside so far
- `horizon` and `due_date`: optional; date uses the same formats as transactions

Outputs: total estimated, total accrued, coverage ratio (accrued/estimated).

### 3) Emergency fund (`emergency_fund.csv`)

Header: `target_months,current_capital`

- Typically a single row
- `target_months`: desired months of expenses to cover (defaults to 6 if missing)
- `current_capital`: current emergency fund amount

Outputs: target capital, delta to target, status (`OK` if current >= target).

### 4) Investment transactions (`investments.csv`)

Header: `date,etf,ticker,area,quantity,price,fees`

- `quantity`: can be negative for sells
- `price`: per unit
- `fees`: optional per transaction cost
- Positions fully sold (net quantity = 0) are omitted from summary

Outputs: invested and current totals, items with weights, average price, current value, and PnL.

### 5) Current prices (`current_prices.csv`)

Header: `ticker,price`

- Maps tickers to current market prices used for the investment summary

## Output example

Running the app prints something like:

```
=== Personal Finance Dashboard ===

-- Liquidity (Transactions) --
Total income: 1500.00
Total expense: 500.00
Net: 1000.00
Avg monthly expense (12m): 41.67

-- Planned & Predictable Expenses --
Total estimated: 1500.00
Total accrued: 900.00
Coverage: 60.00%

-- Emergency Fund --
Target capital: 6000.00
Current capital: 5000.00
Delta to target: 1000.00
Status: BELOW TARGET

-- Investments (Long Term) --
Total invested: 2000.00
Total current: 2300.00
Breakdown:
  - ETF A (A): current=1100.00, pnl=100.00, weight=47.83%
  - ETF B (B): current=1200.00, pnl=200.00, weight=52.17%

-- Summary --
Total net worth: 9200.00
% Invested: 25.00%
% Liquid: 75.00%
```

Note: Percentages are rounded to 2 decimals for display.

## Data parsing details

- Dates: multiple formats supported; if a date can’t be parsed, the app throws an error listing the offending value
- Numbers: both `,` and `.` decimal separators are supported; `€` and spaces are stripped (e.g., `"1 234,56" -> 1234.56`)
- Missing values: numeric fields default to 0, optional strings may be `null`

## Testing

Run unit tests and view the reports:

```zsh
./gradlew test
# HTML report: build/reports/tests/test/index.html
```

## Release automation (semantic-release)

This repo uses semantic-release to automate versioning, changelog, and GitHub Releases based on Conventional Commits.

- Commit format: `type(scope?): subject` with body/footer for BREAKING CHANGES
  - Common types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
  - BREAKING change: include `BREAKING CHANGE:` in the commit body or use `!` after type/scope (e.g., `feat!: ...`)
- Branches: releases run on `main` or `master`
- What happens on release:
  - Determine next version from commit history
  - Update `CHANGELOG.md`
  - Build artifacts are uploaded to the GitHub Release (the CLI JAR)
  - Create GitHub Release with notes

To cut a release, push Conventional Commits to the default branch. The Release workflow will run automatically.

## FAQ

**Q**: Can I use this for business expenses?  
**A**: Yes, just customize the CSVs to match your data sources.

**Q**: Does it really work?  
**A**: Absolutely, check the tests and example data for verification.

**Q**: How can I contribute?  
**A**: See the `CONTRIBUTING.md` for guidelines on reporting issues and submitting pull requests.

<div align="center">
  <img src="portfolium-logo.png" alt="Portfolium Logo" width="200"/>
  
  # Portfolium
  
  **Professional Personal Finance Dashboard**

  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
  
  *Take control of your finances with a beautiful, data-driven dashboard powered by CSV files*
  
  [Features](#-features) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Contributing](#-contributing)
  
</div>

---

## 📊 Overview

**Portfolium** is a modern Kotlin-based personal finance tool that transforms your financial data into actionable insights. It provides both a **console dashboard** and an **interactive web interface** with real-time charts and visualizations.

Perfect for privacy-conscious individuals who want to track their finances without relying on third-party services. All your data stays on your machine, stored in simple CSV files that you fully control.

### Why Portfolium?

- 📈 **Dual Interface**: Console output + beautiful web dashboard
- 💹 **Investment Tracking**: Real-time portfolio performance with historical charts
- 🎯 **Goal Management**: Track emergency funds and planned expenses
- ⚡ **Flexible Data Sources**: CSV files or Yahoo Finance API

---

## ✨ Features

### 💰 Liquidity Management
- Track income and expenses from transaction history
- Calculate net liquidity and average monthly expenses (12-month rolling)
- Categorize transactions by type, category, and payment method

### 📅 Planned Expenses
- Monitor upcoming predictable expenses
- Track accrued amounts vs. estimated totals
- Calculate coverage ratios to ensure you're saving enough

### 🚨 Emergency Fund
- Set target months of expenses for your emergency fund
- Real-time status tracking (target met or shortfall)
- Automatic calculation based on your spending patterns

### 📊 Investment Portfolio
- Track multiple investment positions (ETFs, stocks, etc.)
- Real-time portfolio valuation with live prices
- Historical performance charts with customizable time periods
- Calculate profit/loss, average prices, and portfolio weights
- Support for transaction fees and multiple buy/sell operations

### 🌐 Web Dashboard
- Modern, responsive web interface on `localhost:8080`
- Interactive charts powered by Chart.js
- Visual breakdown of asset allocation
- Historical performance visualization

### 🔧 Configuration Options
- **Price Data Sources**: 
  - CSV files
  - Yahoo Finance API (real-time market data)
- **Historical Performance**: Optional performance tracking over time
- **Environment Variables**: Configure via `.properties` file or env vars

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** (automatically managed via Gradle toolchain)
- **Gradle** (wrapper included)

### Installation & Running

1. **Clone the repository**
   ```zsh
   git clone https://github.com/filippovissani/portfolium.git
   cd portfolium
   ```

2. **Build the project**
   ```zsh
   ./gradlew build
   ```

3. **Run with default data**
   ```zsh
   ./gradlew run
   ```
   
   This will:
   - Load CSV files from the `data/` directory
   - Print a console dashboard
   - Start a web server at `http://localhost:8080`

4. **Run with custom data directory**
   ```zsh
   ./gradlew run --args="/path/to/your/data"
   ```

5. **Run the JAR directly**
   ```zsh
   java -jar build/libs/portfolium-1.0-SNAPSHOT.jar [data-directory]
   ```

### First Steps

1. Open `http://localhost:8080` in your browser
2. Explore the example data in the `data/` folder
3. Customize the CSV files with your own financial data
4. Refresh the page to see your personalized dashboard

---

## 📂 Project Structure

```
portfolium/
├── src/main/kotlin/io/github/filippovissani/portfolium/
│   ├── Main.kt                          # Application entry point
│   ├── controller/
│   │   ├── Controller.kt                # Main business logic orchestration
│   │   ├── config/                      # Configuration management
│   │   ├── csv/                         # CSV parsers and loaders
│   │   └── datasource/                  # Price data sources (CSV, Yahoo Finance)
│   ├── model/                           # Domain models and calculators
│   └── view/
│       ├── Console.kt                   # Terminal output formatter
│       └── WebView.kt                   # Web dashboard (Ktor + HTML DSL)
├── src/test/kotlin/                     # Unit tests (Kotest)
├── data/                                # Example CSV files
│   ├── transactions.csv
│   ├── planned_expenses.csv
│   ├── emergency_fund.csv
│   ├── investments.csv
│   └── current_prices.csv
├── build.gradle.kts                     # Gradle build configuration
└── portfolium.properties                # Optional configuration file
```

---

## 📋 Documentation

### CSV Data Formats

All CSV files should be placed in a data directory (default: `data/`).

#### 1. Transactions (`transactions.csv`)

Track all income and expenses.

```csv
date,description,type,category,method,amount,note
2026-01-05,Salary Deposit,income,Salary,Bank Transfer,3000.00,January salary
2026-01-10,Grocery Shopping,expense,Groceries,Credit Card,-150.75,Weekly groceries
```

**Fields:**
- `date`: Date in `YYYY-MM-DD`, `dd/MM/yyyy`, or `MM/dd/yyyy` format
- `description`: Transaction description
- `type`: `income` or `expense`
- `category`: Custom category (e.g., Salary, Food, Transport)
- `method`: Payment method (e.g., Bank Transfer, Credit Card, Cash)
- `amount`: Positive for income, negative for expenses
- `note`: Optional notes

#### 2. Planned Expenses (`planned_expenses.csv`)

Track predictable future expenses.

```csv
name,estimated_amount,horizon,due_date,accrued
Car Insurance,1200.00,Annual,2026-12-01,600.00
Vacation Fund,2000.00,Semi-Annual,2026-07-01,800.00
```

**Fields:**
- `name`: Expense name
- `estimated_amount`: Total expected amount
- `horizon`: Optional (e.g., Annual, Monthly)
- `due_date`: Optional due date
- `accrued`: Amount already saved

#### 3. Emergency Fund (`emergency_fund.csv`)

Set your emergency fund target.

```csv
target_months,current_capital
6,15000.00
```

**Fields:**
- `target_months`: Number of months of expenses to cover (default: 6)
- `current_capital`: Current emergency fund balance

#### 4. Investment Transactions (`investments.csv`)

Track all buy/sell transactions for investments.

```csv
date,etf,ticker,area,quantity,price,fees
2022-01-05,VTI,VTI,US,10,291.00,1.00
2023-06-15,VEA,VEA,International,5,47.40,0.50
2024-01-10,VOO,VOO,US,-2,549.32,0.80
```

**Fields:**
- `date`: Transaction date
- `etf`: Investment name/description
- `ticker`: Stock/ETF ticker symbol
- `area`: Geographic area or category (e.g., US, International, Emerging Markets)
- `quantity`: Number of shares (negative for sells)
- `price`: Price per share
- `fees`: Transaction fees (optional, defaults to 0)

#### 5. Current Prices (`current_prices.csv`)

Current market prices for investments (when using CSV price source).

```csv
ticker,price
VTI,295.50
VEA,48.20
VOO,562.80
```

**Fields:**
- `ticker`: Ticker symbol (must match tickers in `investments.csv`)
- `price`: Current market price per share

### Configuration

Create a `portfolium.properties` file in the project root:

```properties
# Price data source: CSV or YAHOO_FINANCE
price.data.source=CSV

# Path to current prices CSV (when using CSV source)
csv.prices.path=data/current_prices.csv

# Enable historical performance tracking
enable.historical.performance=true

# Historical performance period in months
historical.performance.months=12
```

**Or use environment variables:**

```zsh
export PRICE_DATA_SOURCE=YAHOO_FINANCE
export ENABLE_HISTORICAL_PERFORMANCE=true
```

### Console Output Example

```
=== Personal Finance Dashboard ===

-- Liquidity (Transactions) --
Total income: 12,000.00 €
Total expense: 4,500.00 €
Net: 7,500.00 €
Avg monthly expense (12m): 375.00 €

-- Planned & Predictable Expenses --
Total estimated: 3,200.00 €
Total accrued: 1,400.00 €
Coverage: 43.75%

-- Emergency Fund --
Target capital (6 months): 2,250.00 €
Current capital: 15,000.00 €
Delta to target: +12,750.00 €
Status: ✓ TARGET REACHED

-- Investments (Long Term) --
Total invested: 10,000.00 €
Total current value: 11,250.00 €
Total P&L: +1,250.00 € (+12.50%)

Portfolio Breakdown:
  • VTI (US): 10 shares @ avg 291.00 € → 2,955.00 € (+1.71%, weight: 26.27%)
  • VEA (International): 5 shares @ avg 47.40 € → 241.00 € (+1.69%, weight: 2.14%)
  • VOO (US): 8 shares @ avg 549.32 € → 4,502.40 € (+2.77%, weight: 40.02%)

-- Total Net Worth --
Total: 34,000.00 €
% Invested: 33.09%
% Liquid: 66.91%

Web dashboard running at http://localhost:8080
```

---

## 🧪 Testing

Run the test suite:

```zsh
./gradlew test
```

View the HTML test report:

```zsh
open build/reports/tests/test/index.html
```

Tests are written using **Kotest** and cover:
- CSV parsing and data loading
- Financial calculations (liquidity, emergency fund, investments)
- Portfolio construction and aggregation
- Historical performance tracking

---

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9+
- **Runtime**: Java 21 (via Gradle toolchain)
- **Web Framework**: Ktor (Netty engine)
- **CSV Parsing**: kotlin-csv
- **Logging**: SLF4J + Logback
- **Testing**: Kotest
- **Frontend**: Chart.js, vanilla JavaScript
- **Build Tool**: Gradle with Kotlin DSL

---


- Built with ❤️ using Kotlin
- Charts powered by [Chart.js](https://www.chartjs.org/)
- Price data from [Yahoo Finance](https://finance.yahoo.com/)
- Inspired by the principles of financial independence and data ownership

---

<div align="center">
  
  **Made with ☕ by [Filippo Vissani](https://github.com/filippovissani)**
  
  If you find this project useful, please consider giving it a ⭐!
  
</div>


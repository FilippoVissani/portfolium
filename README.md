<div align="center">
  <img src="portfolium-logo.png" alt="Portfolium Logo" width="200"/>
  
  # Portfolium
  
  **Personal Finance Dashboard**

  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
  [![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org/)
  [![CI](https://github.com/FilippoVissani/portfolium/actions/workflows/main.yml/badge.svg)](https://github.com/FilippoVissani/portfolium/actions/workflows/main.yml)

  *Take control of your finances with a beautiful, data-driven dashboard powered by CSV files*
  
  [Features](#-features) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Configuration](#-configuration)
  
</div>

---

## 📊 Overview

**Portfolium** is a privacy-focused personal finance tool built with Kotlin that transforms your financial data into actionable insights. Keep all your data on your machine in simple CSV files while enjoying a modern web dashboard with real-time analytics and visualizations.

### Why Portfolium?

- 🔒 **Complete Privacy**: All data stays on your machine—no cloud, no third parties
- 📈 **Dual Interface**: Clean console output + stunning web dashboard
- 💹 **Smart Investment Tracking**: Real-time prices via Yahoo Finance with caching
- 🎯 **Goal Management**: Emergency funds and planned expenses tracking
- 📊 **Historical Performance**: Track portfolio growth with interactive charts
- ⚡ **Lightning Fast**: Built on Ktor with efficient Kotlin coroutines
- 🎨 **Beautiful UI**: Modern responsive design with Chart.js visualizations

---

## ✨ Features

### 💰 Liquidity Management
- Track all income and expenses from transaction history
- Calculate net liquidity automatically
- 12-month rolling average for monthly expenses
- Categorize by type, category, and payment method
- Support for multiple transaction formats

### 📅 Planned Expenses
- Monitor upcoming predictable expenses (insurance, vacations, etc.)
- Track accrued vs. estimated amounts
- Coverage ratio calculation
- Separate liquid vs. invested tracking
- Instrument specification (liquid, ETF, bond, etc.)

### 🚨 Emergency Fund
- Configure target months of expenses
- Real-time status tracking (OK/BELOW TARGET)
- Automatic target calculation based on spending
- Support for liquid or invested emergency funds
- Visual status indicators on web dashboard

### 📊 Investment Portfolio
- Track multiple investment positions (ETFs, stocks, etc.)
- Transaction-based accounting (buy/sell with fees)
- Real-time prices from Yahoo Finance API
- Automatic average price calculation
- Profit/loss and portfolio weight computation
- Historical performance tracking with configurable intervals
- Support for multiple geographic areas
- Fully sold positions automatically excluded

### 🌐 Web Dashboard
- Modern, responsive interface on `http://localhost:8080`
- Interactive charts powered by Chart.js
- Multiple visualizations:
  - Asset allocation pie chart
  - Net worth distribution
  - Investment portfolio breakdown
  - Planned expenses coverage
  - Historical performance with time period filters (1M, 6M, YTD, 5Y, ALL)
- Detailed investment table with P&L
- Configurable server port

### 🔧 Advanced Features
- **Smart Price Caching**: 24-hour cache for Yahoo Finance prices (configurable)
- **Flexible Configuration**: Properties file or environment variables
- **Error Handling**: Graceful degradation with logging
- **Historical Data**: Configurable interval for performance tracking
- **Efficient Data Processing**: Optimized calculation algorithms

---

## 🚀 Quick Start

### Prerequisites

- **Java 25** (automatically managed via Gradle toolchain)
- **Gradle** (wrapper included - no installation needed)

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

3. **Run the application**
   ```zsh
   ./gradlew run
   ```
   
   This will:
   - Load CSV files from the `data/` directory
   - Print a console dashboard to the terminal
   - Start a web server at `http://localhost:8080`
   - Fetch real-time prices from Yahoo Finance (cached for 24 hours)

4. **Access the web dashboard**
   
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

5. **Customize your data**
   
   Edit the CSV files in the `data/` directory:
   - `transactions.csv` - Your income and expenses
   - `planned_expenses.csv` - Upcoming planned costs
   - `emergency_fund.csv` - Emergency fund configuration
   - `investments.csv` - Investment transactions (buy/sell)

6. **Optional: Build and run the JAR**
   ```zsh
   ./gradlew build
   java -jar build/libs/portfolium-4.0.0.jar
   ```

---

## 📂 Project Structure

```
portfolium/
├── src/
│   ├── main/
│   │   ├── kotlin/io/github/filippovissani/portfolium/
│   │   │   ├── Main.kt                     # Application entry point
│   │   │   ├── controller/
│   │   │   │   ├── Controller.kt           # Main orchestration logic
│   │   │   │   ├── config/
│   │   │   │   │   ├── Config.kt           # Configuration data class
│   │   │   │   │   └── ConfigLoader.kt     # Properties file loader
│   │   │   │   ├── csv/
│   │   │   │   │   ├── CsvUtils.kt         # CSV utilities
│   │   │   │   │   └── Loaders.kt          # CSV data loaders
│   │   │   │   └── datasource/
│   │   │   │       ├── PriceDataSource.kt       # Price source interface
│   │   │   │       ├── YahooFinancePriceDataSource.kt  # Yahoo Finance API
│   │   │   │       ├── CsvPriceDataSource.kt    # CSV-based prices
│   │   │   │       └── CachedPriceDataSource.kt # Caching decorator
│   │   │   ├── model/
│   │   │   │   ├── Models.kt               # Data models
│   │   │   │   ├── Calculators.kt          # Business logic calculators
│   │   │   │   └── HistoricalPerformanceCalculator.kt
│   │   │   └── view/
│   │   │       ├── Console.kt              # Terminal output
│   │   │       └── WebView.kt              # Web dashboard (Ktor)
│   │   └── resources/
│   │       ├── application.properties       # Configuration file
│   │       ├── logback.xml                  # Logging configuration
│   │       └── static/
│   │           ├── css/styles.css           # Dashboard styles
│   │           └── js/charts.js             # Chart.js initialization
│   └── test/
│       └── kotlin/                          # Unit tests (Kotest)
├── data/                                    # CSV data files
│   ├── transactions.csv
│   ├── planned_expenses.csv
│   ├── emergency_fund.csv
│   ├── investments.csv
│   └── price_cache.csv                      # Auto-generated price cache
├── gradle/
│   └── libs.versions.toml                   # Dependency version catalog
├── build.gradle.kts                         # Gradle build script
└── settings.gradle.kts
```

---

## 📋 Documentation

### CSV Data Formats

All CSV files are stored in the `data/` directory (configurable via `application.properties`).

#### 1. Transactions (`transactions.csv`)

Track all income and expenses.

```csv
date,description,type,category,method,amount,note
2025-01-01,Salary Deposit,income,Salary,Bank Transfer,4442.24,January salary
2025-01-05,Grocery Shopping,expense,Groceries,Credit Card,-85.50,Weekly shopping
2025-01-10,Freelance Payment,income,Consulting,Bank Transfer,800.00,Client project
```

**Fields:**
- `date` (required): Transaction date in `YYYY-MM-DD` format
- `description` (required): Transaction description
- `type` (required): `income` or `expense`
- `category` (required): Custom category (e.g., Salary, Food, Transport, Utilities)
- `method` (required): Payment method (e.g., Bank Transfer, Credit Card, Cash, PayPal)
- `amount` (required): Amount in decimal format (positive for income, negative for expenses)
- `note` (optional): Additional notes

#### 2. Planned Expenses (`planned_expenses.csv`)

Track predictable future expenses and savings goals.

```csv
name,estimated_amount,horizon,due_date,accrued,instrument
Car Insurance,1200.00,Annual,2025-12-01,600.00,liquid
Vacation Fund,2500.00,Summer,2025-07-01,1200.00,etf
Home Renovation,15000.00,Long-term,2026-06-01,8000.00,bond
```

**Fields:**
- `name` (required): Expense or goal name
- `estimated_amount` (required): Total expected amount
- `horizon` (optional): Time frame description (e.g., Annual, Monthly, Summer)
- `due_date` (optional): Target date in `YYYY-MM-DD` format
- `accrued` (required): Amount already saved
- `instrument` (optional): Where the accrued amount is held (`liquid`, `etf`, `bond`, etc.)
  - Empty or `liquid` = included in liquid capital
  - Any other value = included in invested capital

#### 3. Emergency Fund (`emergency_fund.csv`)

Configure your emergency fund target.

```csv
target_months,current_capital,instrument
6,3006.01,liquid
```

**Fields:**
- `target_months` (required): Number of months of expenses to cover
- `current_capital` (required): Current emergency fund balance
- `instrument` (optional): Type of holding (`liquid` or investment instrument)
  - `liquid` (default) = cash/savings account
  - Other values = invested emergency fund (bonds, money market, etc.)

#### 4. Investment Transactions (`investments.csv`)

Track all buy/sell transactions for your investment portfolio.

```csv
date,etf,ticker,area,quantity,price,fees
2025-01-15,XETRA,XEON.DE,Germany,10,145.02,1.5
2024-06-10,Vanguard S&P 500,VOO,US,5,420.50,2.00
2024-12-01,iShares MSCI World,IWDA.AS,Global,-2,85.30,1.20
```

**Fields:**
- `date` (required): Transaction date in `YYYY-MM-DD` format
- `etf` (required): Investment name or description
- `ticker` (required): Stock/ETF ticker symbol (used for price lookup)
- `area` (optional): Geographic area or category (e.g., US, Europe, Asia, Global)
- `quantity` (required): Number of shares
  - Positive for buy transactions
  - Negative for sell transactions
- `price` (required): Price per share at transaction time
- `fees` (optional): Transaction fees/commissions (defaults to 0)

**Notes:**
- Fully sold positions (net quantity = 0) are automatically excluded from the portfolio
- Average price is calculated automatically including fees
- Current prices are fetched from Yahoo Finance using the ticker symbol

---

## ⚙️ Configuration

Portfolium is configured via the `src/main/resources/application.properties` file.

### Default Configuration

```properties
# Data Directory Paths
data.path=data
data.transactions=transactions.csv
data.planned.expenses=planned_expenses.csv
data.emergency.fund=emergency_fund.csv
data.investments=investments.csv
data.price.cache=price_cache.csv

# Cache Settings
cache.duration.hours=24

# Historical Performance Settings
historical.performance.interval.days=7

# Web Server Settings
server.port=8080
```

### Configuration Options

| Property | Description | Default | Type |
|----------|-------------|---------|------|
| `data.path` | Directory containing CSV files | `data` | String |
| `data.transactions` | Transactions CSV filename | `transactions.csv` | String |
| `data.planned.expenses` | Planned expenses CSV filename | `planned_expenses.csv` | String |
| `data.emergency.fund` | Emergency fund CSV filename | `emergency_fund.csv` | String |
| `data.investments` | Investments CSV filename | `investments.csv` | String |
| `data.price.cache` | Price cache CSV filename | `price_cache.csv` | String |
| `cache.duration.hours` | How long to cache Yahoo Finance prices | `24` | Long |
| `historical.performance.interval.days` | Days between performance data points | `7` | Long |
| `server.port` | Web dashboard port | `8080` | Integer |

### Customizing Configuration

Edit the `application.properties` file to customize paths and settings:

```properties
# Example: Use a different data directory
data.path=/home/user/my-finance-data

# Example: Reduce cache duration
cache.duration.hours=12

# Example: Daily performance tracking
historical.performance.interval.days=1

# Example: Custom port
server.port=3000
```

---

## 🖥️ Console Output Example

```
=== Personal Finance Dashboard ===

-- Liquidity (Transactions) --
Total income: 5242.24
Total expense: 85.50
Net: 5156.74
Avg monthly expense (12m): 7.13

-- Planned & Predictable Expenses --
Total estimated: 0
Total accrued: 0
  - Liquid: 0
  - Invested: 0
Coverage: 0.00%

-- Emergency Fund --
Target capital: 42.78
Current capital: 3006.01
Delta to target: -2963.23
Status: OK
Type: Liquid

-- Investments (Long Term) --
Total invested: 1451.50
Total current: 1450.20
Breakdown:
  - XETRA (XEON.DE): current=1450.20, pnl=-1.30, weight=100.00%

-- Summary --
Total net worth: 9613.95
% Invested: 15.09%
% Liquid: 84.91%

Tip: You can pass CSV path as argument
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

Generate code coverage report:

```zsh
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

Tests are written using **Kotest** and cover:
- CSV parsing and data loading
- Financial calculations (liquidity, emergency fund, investments)
- Portfolio construction and aggregation
- Historical performance tracking
- Price data source implementations

---

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin 2.3.0
- **Runtime**: Java 25 (via Gradle toolchain)
- **Build Tool**: Gradle 8.x with Kotlin DSL
- **Web Framework**: Ktor 3.3.3 (Netty engine)

### Libraries & Dependencies
- **CSV Parsing**: [kotlin-csv](https://github.com/jsoizo/kotlin-csv) 1.10.0
- **Logging**: SLF4J + Logback 1.5.24
- **Testing**: Kotest 6.0.7
- **Serialization**: Gson (for JSON in web responses)
- **HTTP Client**: Java 11+ HttpClient (for Yahoo Finance API)

### Frontend
- **Charts**: [Chart.js](https://www.chartjs.org/) 4.4.1
- **Icons**: Font Awesome 6.5.1
- **Fonts**: Inter (Google Fonts)
- **CSS**: Custom responsive design
- **JavaScript**: Vanilla JS (no frameworks)

### Architecture Patterns
- **MVC**: Model-View-Controller separation
- **Data Sources**: Strategy pattern for price providers
- **Caching**: Decorator pattern for price caching
- **Configuration**: Properties file with type-safe loading

---

## 📊 How It Works

### Data Flow

1. **Startup**: Application loads configuration from `application.properties`
2. **Data Loading**: CSV files are parsed and validated
3. **Price Fetching**: Current prices fetched from Yahoo Finance (with 24h cache)
4. **Calculations**: 
   - Liquidity summary from transactions
   - Planned expenses coverage
   - Emergency fund status
   - Investment portfolio aggregation from transactions
   - Historical performance over time
5. **Output**:
   - Console: Formatted text dashboard
   - Web: HTTP server with interactive charts

### Price Data Sources

#### Yahoo Finance (Default)
- Real-time market data via public API
- Supports historical prices for performance tracking
- Automatic caching to reduce API calls
- Fallback for weekends/holidays

#### Price Cache
- Stores fetched prices in `price_cache.csv`
- Configurable duration (default: 24 hours)
- Persists between application runs
- Format: `ticker,price,timestamp`

### Historical Performance

The application tracks portfolio value over time:

1. Calculates portfolio value at regular intervals (default: 7 days)
2. Uses historical prices from Yahoo Finance
3. Computes total return and annualized return
4. Displays interactive chart with time period filters

---

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./gradlew test`
5. Check code coverage: `./gradlew jacocoTestReport`
6. Commit your changes: `git commit -m 'Add amazing feature'`
7. Push to the branch: `git push origin feature/amazing-feature`
8. Open a Pull Request

### Code Quality

- Write tests for new features
- Maintain code coverage above 80%
- Follow Kotlin coding conventions
- Add documentation for public APIs

---

## 📜 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Built with ❤️ using Kotlin
- Charts powered by [Chart.js](https://www.chartjs.org/)
- Market data from [Yahoo Finance](https://finance.yahoo.com/)
- Inspired by the principles of financial independence and data ownership
- Special thanks to the open-source community

---

## 📞 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/filippovissani/portfolium/issues)
- **Discussions**: [GitHub Discussions](https://github.com/filippovissani/portfolium/discussions)
- **Author**: [Filippo Vissani](https://github.com/filippovissani)

---

<div align="center">
  
  **Made with ☕ by [Filippo Vissani](https://github.com/filippovissani)**
  
  If you find this project useful, please consider giving it a ⭐!
  
</div>

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


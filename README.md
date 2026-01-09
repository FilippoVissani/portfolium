# Portfolium

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg)
![Java](https://img.shields.io/badge/java-25-orange.svg)

A comprehensive personal finance portfolio management tool built with Kotlin. Track your investments, manage multiple bank accounts, monitor planned expenses, and visualize your financial health through an intuitive web dashboard.

![Portfolium Logo](portfolium-logo.png)

## Features

### 📊 Multi-Account Management
- **Main Bank Account**: Track income and expenses with categorization
- **Investment Account**: Monitor ETF holdings with real-time pricing
- **Planned Expenses Account**: Save for future goals (house, car, etc.)
- **Emergency Fund**: Maintain your financial safety net

### 💹 Investment Tracking
- Real-time ETF price fetching from Yahoo Finance
- Automatic price caching to minimize API calls
- Support for multiple tickers and geographic areas
- Calculate profit/loss (P&L) and current portfolio value
- Track average purchase prices

### 📈 Historical Performance Analysis
- Calculate time-weighted returns for individual accounts
- Overall portfolio performance tracking
- Annualized return calculations
- Configurable data point intervals

### 📉 Expense Analytics
- Monthly income/expense trends
- Category-based expense analysis
- 12-month rolling average calculations
- Top expense and income categories

### 🌐 Web Dashboard
- Beautiful, responsive web interface
- Interactive charts powered by Chart.js
- Real-time portfolio visualization
- Breakdown by account type and asset allocation
- Performance graphs and statistics

### 💻 Console Output
- Detailed command-line portfolio summary
- Color-coded financial data
- Quick overview without running the web server

## Architecture

Portfolium follows a clean architecture with clear separation of concerns:

```
src/main/kotlin/io/github/filippovissani/portfolium/
├── Main.kt                          # Application entry point
├── controller/                      # Business logic layer
│   ├── Controller.kt                # Main controller
│   ├── config/                      # Configuration management
│   │   ├── Config.kt
│   │   └── ConfigLoader.kt
│   ├── datasource/                  # Price data sources
│   │   ├── PriceDataSource.kt       # Interface
│   │   ├── YahooFinancePriceDataSource.kt
│   │   ├── CachedPriceDataSource.kt # Caching decorator
│   │   └── CsvPriceDataSource.kt
│   ├── yaml/                        # YAML file loaders
│   │   └── BankAccountLoaders.kt
│   └── csv/                         # CSV utilities
│       └── CsvUtils.kt
├── model/                           # Domain models
│   ├── Models.kt                    # Core data structures
│   ├── Calculators.kt               # Financial calculations
│   ├── HistoricalPerformanceCalculator.kt
│   └── util/
│       └── Money.kt                 # Money arithmetic utilities
└── view/                            # Presentation layer
    ├── Console.kt                   # CLI output
    └── WebView.kt                   # Web dashboard (Ktor)
```

## Getting Started

### Prerequisites

- **Java 25** (JDK 25 or later)
- **Gradle** (wrapper included)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/filippovissani/portfolium.git
cd portfolium
```

2. Build the project:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew run
```

The web dashboard will start on `http://localhost:8080` (configurable).

## Configuration

Configure Portfolium through `src/main/resources/application.properties`:

```properties
# Data Directory Path
data.path=data

# Bank Account YAML Files
data.main.bank.account=main_bank_account.yaml
data.planned.expenses.bank.account=planned_expenses_bank_account.yaml
data.emergency.fund.bank.account=emergency_fund_bank_account.yaml
data.investment.bank.account=investment_bank_account.yaml

# Price Cache (CSV)
data.price.cache=price_cache.csv

# Cache Settings (hours)
cache.duration.hours=24

# Historical Performance Settings (days between data points)
historical.performance.interval.days=7

# Web Server Settings
server.port=8080
```

## Data Structure

### Main Bank Account (`main_bank_account.yaml`)

Track liquid money with categorized transactions:

```yaml
name: "Main Bank Account"
initialBalance: 2000.00
transactions:
  - date: 2025-01-15
    description: "January Salary"
    category: "Salary"
    amount: 3500.00
    note: "Monthly salary"
  - date: 2025-01-20
    description: "Rent Payment"
    category: "Housing"
    amount: -1200.00
  - date: 2025-01-22
    description: "Grocery Shopping"
    category: "Food"
    amount: -250.00
```

### Investment Account (`investment_bank_account.yaml`)

Track ETF investments:

```yaml
name: "Investments"
initialBalance: 5000.00
transactions:
  - type: deposit
    date: 2024-01-01
    amount: 5000.00
    description: "Initial capital"
  - type: etf_buy
    date: 2024-01-15
    name: "Vanguard S&P 500 ETF"
    ticker: "VOO"
    area: "US"
    quantity: 10
    price: 400.00
    fees: 5.00
  - type: etf_sell
    date: 2024-06-15
    name: "Vanguard S&P 500 ETF"
    ticker: "VOO"
    area: "US"
    quantity: 2
    price: 420.00
    fees: 3.00
```

### Planned Expenses Account (`planned_expenses_bank_account.yaml`)

Save for specific future goals:

```yaml
name: "Planned Expenses"
initialBalance: 3000.00
plannedExpenses:
  - name: "House Down Payment"
    expirationDate: 2027-12-31
    estimatedAmount: 50000.00
  - name: "New Car"
    expirationDate: 2026-06-30
    estimatedAmount: 25000.00
transactions:
  - type: deposit
    date: 2024-01-15
    amount: 3000.00
    description: "Initial planned expenses fund"
  - type: etf_buy
    date: 2024-02-01
    description: "Vanguard Total World Stock ETF"
    ticker: "VT"
    area: "Global"
    quantity: 20
    price: 95.00
    fees: 5.00
```

### Emergency Fund Account (`emergency_fund_bank_account.yaml`)

Maintain your safety net:

```yaml
name: "Emergency Fund"
initialBalance: 1000.00
targetMonthlyExpenses: 6  # Target: 6 months of expenses
transactions:
  - type: deposit
    date: 2024-01-15
    amount: 2000.00
    description: "Initial fund"
  - type: etf_buy
    date: 2024-02-01
    name: "iShares Core U.S. Aggregate Bond ETF"
    ticker: "AGG"
    area: "US"
    quantity: 10
    price: 100.00
    fees: 5.00
```

## Usage Examples

### Running the Application

```bash
# Build and run
./gradlew run

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# Check code quality (ktlint + tests + coverage)
./gradlew check

# Build distribution
./gradlew build
```

### Price Data Sources

Portfolium supports multiple price data sources:

1. **Yahoo Finance** (default): Fetches real-time prices via web scraping
2. **Cached**: Wraps any data source with automatic caching
3. **CSV**: Read prices from a custom CSV file

The price cache is automatically maintained in `data/price_cache.csv` and respects the configured cache duration.

### Portfolio Calculations

The application automatically calculates:

- **Net Worth**: Total value across all accounts
- **Asset Allocation**: Percentage invested vs. liquid
- **Emergency Fund Status**: Whether target is met
- **Planned Expenses Coverage**: Ratio of saved vs. estimated amounts
- **Investment P&L**: Profit/loss for each holding
- **Historical Performance**: Time-weighted returns with annualization

## Development

### Code Quality

This project enforces high code quality standards:

- **ktlint**: Kotlin code style checking and formatting
- **JaCoCo**: Code coverage reporting
- **JUnit 5 + Kotest**: Comprehensive test suite

```bash
# Format code
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck

# Run all checks
./gradlew check
```

### Technology Stack

- **Language**: Kotlin 2.3.0
- **Build**: Gradle with Kotlin DSL
- **Web Server**: Ktor 3.3.3 (Netty engine)
- **Templating**: kotlinx.html DSL
- **YAML Parsing**: SnakeYAML 2.5
- **CSV Processing**: kotlin-csv 1.10.0
- **Logging**: Logback 1.5.24
- **Testing**: JUnit 5, Kotest 6.0.7
- **Charts**: Chart.js (client-side)

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for commit messages:

```bash
feat(dashboard): add pie chart for asset allocation
fix(yaml): handle missing plannedExpenses field
docs(readme): update installation instructions
```

## Testing

The project includes comprehensive tests covering:

- Model calculations and business logic
- YAML file parsing and loading
- Price data source caching
- CSV utilities
- Bank account operations

Run tests with:

```bash
./gradlew test --info
```

View coverage report (generated in `build/reports/jacoco/test/html/`):

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.

## Roadmap

Future enhancements under consideration:

- [ ] Support for cryptocurrency tracking
- [ ] Multiple currency support with conversion
- [ ] Export reports to PDF
- [ ] Database persistence option
- [ ] Mobile-responsive dashboard improvements
- [ ] Real-time price updates via WebSocket
- [ ] Budget planning and tracking
- [ ] Tax reporting features

## Author

**Filippo Vissani** - [GitHub](https://github.com/filippovissani)

---

**Note**: This tool is for personal finance tracking and educational purposes. Always consult with a qualified financial advisor for investment decisions.


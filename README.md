# Portfolium

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Latest Release](https://img.shields.io/github/v/release/FilippoVissani/portfolium)](https://github.com/FilippoVissani/portfolium/releases/latest)

**Portfolium** is a personal finance and investment portfolio management tool written in Kotlin. Track your liquidity, planned expenses, emergency fund, and investment performance—all from CSV files. View your financial dashboard in the terminal or via a web interface with interactive charts.

<p align="center">
  <img src="portfolium-logo.png" alt="Portfolium Logo" width="400" />
</p>

## Features

- **Liquidity Tracking**: Monitor income and expenses, calculate average monthly spending, and track your liquid capital
- **Planned Expenses**: Keep track of future expenses with target amounts and due dates
- **Emergency Fund**: Define emergency fund goals and monitor progress against monthly expense averages
- **Investment Portfolio**: Track investment transactions (buys/sells) with real-time price data from Yahoo Finance
- **Historical Performance**: Visualize your portfolio performance over time with configurable intervals
- **Price Caching**: Minimize API calls with CSV-based caching of price data
- **Dual Interface**: 
  - Terminal-based dashboard for quick checks
  - Web interface (default port 8080) with interactive charts powered by Chart.js
- **Configurable**: All settings managed via `application.properties`

## Technology Stack

- **Language**: Kotlin 2.3.0
- **Build Tool**: Gradle with Kotlin DSL
- **JVM**: Java 25
- **Web Framework**: Ktor 3.3.3 (Netty server, HTML builder, JSON serialization)
- **Testing**: Kotest 6.0.7 + Kotlin Test
- **CSV Parsing**: kotlin-csv 1.10.0
- **Logging**: Logback 1.5.24
- **Code Coverage**: JaCoCo 0.8.14

## Prerequisites

- Java 21 or higher (JVM toolchain configured for Java 25)
- Gradle (wrapper included, no installation required)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/FilippoVissani/portfolium.git
cd portfolium
```

### 2. Prepare Your Data Files

Create a `data/` directory in the project root with the following CSV files:

#### `transactions.csv`
```csv
date,description,type,category,method,amount,note
2025-01-01,Salary,Income,Salary,Bank,3000.00,
2025-01-05,Groceries,Expense,Food,Cash,150.50,Weekly shopping
```

#### `planned_expenses.csv`
```csv
name,estimatedAmount,horizon,dueDate,accrued,instrument
New Laptop,1500.00,2026,2026-06-01,500.00,liquid
Vacation,2000.00,2026,2026-08-15,800.00,etf
```

#### `emergency_fund.csv`
```csv
targetMonths,currentCapital,instrument
6,5000.00,liquid
```

#### `investments.csv`
```csv
date,etf,ticker,area,quantity,price,fees
2025-01-10,S&P 500,SPY,US,10,450.00,9.99
2025-02-15,MSCI World,VWCE.DE,World,5,95.50,5.00
```

#### `price_cache.csv` (auto-generated)
This file is created automatically to cache price data from Yahoo Finance.

### 3. Configure the Application

Edit `src/main/resources/application.properties` to customize settings:

```ini
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

### 4. Build the Application

```bash
./gradlew build
```

### 5. Run the Application

```bash
./gradlew run
```

Or use the distribution:

```bash
./build/scripts/portfolium
```

The application will:
1. Load your data from CSV files
2. Fetch current prices for your investments (with caching)
3. Calculate portfolio metrics
4. Print a dashboard to the terminal
5. Start a web server at `http://localhost:8080` (or your configured port)

## Testing

Run the test suite:

```bash
./gradlew test
```

Generate coverage reports:

```bash
./gradlew jacocoTestReport
```

View the HTML coverage report at `build/reports/jacoco/test/html/index.html`.

## Project Structure

```
portfolium/
├── src/
│   ├── main/
│   │   ├── kotlin/io/github/filippovissani/portfolium/
│   │   │   ├── Main.kt                    # Application entry point
│   │   │   ├── controller/
│   │   │   │   ├── Controller.kt          # Main orchestration logic
│   │   │   │   ├── config/                # Configuration management
│   │   │   │   ├── csv/                   # CSV file loaders
│   │   │   │   └── datasource/            # Price data sources (Yahoo Finance, caching)
│   │   │   ├── model/
│   │   │   │   ├── Models.kt              # Domain models (Transaction, Investment, etc.)
│   │   │   │   ├── Calculators.kt         # Portfolio calculation logic
│   │   │   │   └── HistoricalPerformanceCalculator.kt
│   │   │   └── view/
│   │   │       ├── Console.kt             # Terminal output
│   │   │       └── WebView.kt             # Web server & HTML rendering
│   │   └── resources/
│   │       ├── application.properties     # Configuration file
│   │       ├── logback.xml               # Logging configuration
│   │       └── static/                    # CSS, JS for web UI
│   └── test/
│       └── kotlin/                        # Kotest test suites
├── data/                                  # Your CSV data files (gitignored)
├── logs/                                  # Application logs
├── build.gradle.kts                       # Gradle build configuration
└── gradle/
    └── libs.versions.toml                 # Dependency version catalog
```

## Web Interface

Access the web dashboard at `http://localhost:8080` to view:

- **Portfolio Summary**: Total assets, allocation breakdown, performance metrics
- **Investment Details**: Current holdings, gains/losses, market values
- **Historical Performance Chart**: Interactive time-series visualization of your portfolio value
- **Liquidity Overview**: Cash flow, monthly averages, planned expenses
- **Emergency Fund Progress**: Target vs. current capital

Charts are rendered using Chart.js with responsive design.

## Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `data.path` | Directory containing CSV data files | `data` |
| `data.transactions` | Transactions CSV filename | `transactions.csv` |
| `data.planned.expenses` | Planned expenses CSV filename | `planned_expenses.csv` |
| `data.emergency.fund` | Emergency fund CSV filename | `emergency_fund.csv` |
| `data.investments` | Investments CSV filename | `investments.csv` |
| `data.price.cache` | Price cache CSV filename | `price_cache.csv` |
| `cache.duration.hours` | How long to cache price data (hours) | `24` |
| `historical.performance.interval.days` | Days between performance snapshots | `7` |
| `server.port` | Web server port | `8080` |

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

This project uses:
- **Conventional Commits** for commit messages
- **Semantic Release** for automated versioning and releases
- **JaCoCo** for code coverage tracking

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for the full release history.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Price data provided by [Yahoo Finance](https://finance.yahoo.com/)
- Built with [Ktor](https://ktor.io/) for the web framework
- Charts powered by [Chart.js](https://www.chartjs.org/)

---

**Author**: Filippo Vissani  
**GitHub**: [@FilippoVissani](https://github.com/FilippoVissani)


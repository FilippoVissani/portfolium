package io.github.filippovissani.portfolium.controller.datasource

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

class CsvPriceDataSourceTest :
    FunSpec({

        test("CsvPriceDataSource should load prices from CSV file") {
            val csvFile = File.createTempFile("test_prices", ".csv")
            csvFile.deleteOnExit()

            csvFile.writeText(
                """
                ticker,price
                AAPL,150.50
                MSFT,300.25
                GOOGL,2800.00
                """.trimIndent(),
            )

            val dataSource = CsvPriceDataSource(csvFile)

            dataSource.getCurrentPrice("AAPL") shouldBe BigDecimal("150.50")
            dataSource.getCurrentPrice("MSFT") shouldBe BigDecimal("300.25")
            dataSource.getCurrentPrice("GOOGL") shouldBe BigDecimal("2800.00")
            dataSource.getCurrentPrice("UNKNOWN") shouldBe null
        }

        test("CsvPriceDataSource should handle empty CSV file") {
            val csvFile = File.createTempFile("test_empty_prices", ".csv")
            csvFile.deleteOnExit()
            csvFile.writeText("ticker,price\n")

            val dataSource = CsvPriceDataSource(csvFile)

            dataSource.getCurrentPrice("AAPL") shouldBe null
        }

        test("CsvPriceDataSource should get current prices for multiple tickers") {
            val csvFile = File.createTempFile("test_batch_prices", ".csv")
            csvFile.deleteOnExit()

            csvFile.writeText(
                """
                ticker,price
                AAPL,150.50
                MSFT,300.25
                GOOGL,2800.00
                """.trimIndent(),
            )

            val dataSource = CsvPriceDataSource(csvFile)
            val prices = dataSource.getCurrentPrices(listOf("AAPL", "MSFT", "UNKNOWN"))

            prices.size shouldBe 2
            prices["AAPL"] shouldBe BigDecimal("150.50")
            prices["MSFT"] shouldBe BigDecimal("300.25")
        }

        test("CsvPriceDataSource should return current price for historical requests") {
            val csvFile = File.createTempFile("test_historical_prices", ".csv")
            csvFile.deleteOnExit()

            csvFile.writeText(
                """
                ticker,price
                AAPL,150.50
                """.trimIndent(),
            )

            val dataSource = CsvPriceDataSource(csvFile)
            val historicalDate = LocalDate.of(2024, 1, 1)

            // CSV doesn't support historical data, should return current price
            dataSource.getHistoricalPrice("AAPL", historicalDate) shouldBe BigDecimal("150.50")
        }

        test("CsvPriceDataSource should return empty map for historical prices range") {
            val csvFile = File.createTempFile("test_historical_range", ".csv")
            csvFile.deleteOnExit()

            csvFile.writeText("ticker,price\nAAPL,150.50\n")

            val dataSource = CsvPriceDataSource(csvFile)
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)

            // CSV doesn't support historical data ranges
            dataSource.getHistoricalPrices("AAPL", startDate, endDate) shouldBe emptyMap()
        }
    })


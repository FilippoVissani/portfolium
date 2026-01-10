package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.model.domain.InvestmentTransaction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class HistoricalPerformanceServiceTest :
    FunSpec({

        test("calculateHistoricalPerformance should generate data points at intervals") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal = BigDecimal("420.00")

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 2, 1)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 10,
                )

            // Should have multiple data points
            performance.dataPoints.size shouldBeGreaterThan 1
            performance.dataPoints.first().date shouldBe startDate
            performance.dataPoints.last().date shouldBe endDate
        }

        test("calculateHistoricalPerformance should calculate portfolio value correctly") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal = BigDecimal("450.00")

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2024, 1, 15)
            val endDate = LocalDate.of(2024, 1, 15)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 30,
                )

            // Portfolio value = 10 * 450 = 4500
            performance.dataPoints.first().value shouldBe BigDecimal("4500.00")
        }

        test("calculateHistoricalPerformance should calculate total return") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            var priceSequence = 0
            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal {
                        // Return 400 for start date, 440 for end date (10% increase)
                        return if (priceSequence++ == 0) BigDecimal("400.00") else BigDecimal("440.00")
                    }

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 365,
                )

            // Total return should be 10% (from 4000 to 4400)
            performance.totalReturn shouldBe BigDecimal("10.00")
        }

        test("calculateHistoricalPerformance should handle multiple transactions") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 2, 1),
                        etf = "World ETF",
                        ticker = "VT",
                        area = "World",
                        quantity = BigDecimal("5"),
                        price = BigDecimal("100.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal =
                        when (ticker) {
                            "SPY" -> BigDecimal("420.00")
                            "VT" -> BigDecimal("110.00")
                            else -> BigDecimal.ZERO
                        }

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2024, 3, 1)
            val endDate = LocalDate.of(2024, 3, 1)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 30,
                )

            // Portfolio value = (10 * 420) + (5 * 110) = 4750
            performance.dataPoints.first().value shouldBe BigDecimal("4750.00")
        }

        test("calculateHistoricalPerformance should handle sell transactions") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                    InvestmentTransaction(
                        date = LocalDate.of(2024, 2, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("-5"), // Sell 5 shares
                        price = BigDecimal("420.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal = BigDecimal("430.00")

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2024, 3, 1)
            val endDate = LocalDate.of(2024, 3, 1)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 30,
                )

            // Portfolio value = 5 * 430 = 2150 (only 5 shares remaining)
            performance.dataPoints.first().value shouldBe BigDecimal("2150.00")
        }

        test("calculateHistoricalPerformance should calculate annualized return") {
            val transactions =
                listOf(
                    InvestmentTransaction(
                        date = LocalDate.of(2023, 1, 1),
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            var priceSequence = 0
            val mockPriceSource =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal {
                        // Double the price over 2 years (100% return)
                        return if (priceSequence++ == 0) BigDecimal("400.00") else BigDecimal("800.00")
                    }

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val startDate = LocalDate.of(2023, 1, 1)
            val endDate = LocalDate.of(2025, 1, 1)

            val performance =
                HistoricalPerformanceService.calculateHistoricalPerformance(
                    transactions = transactions,
                    priceSource = mockPriceSource,
                    startDate = startDate,
                    endDate = endDate,
                    intervalDays = 730,
                )

            // Annualized return for 100% over 2 years should be ~41.42%
            val annualizedReturnValue = performance.annualizedReturn?.toDouble()
            annualizedReturnValue?.shouldBeGreaterThan(40.0)
            annualizedReturnValue?.shouldBeLessThan(42.0)
        }
    })

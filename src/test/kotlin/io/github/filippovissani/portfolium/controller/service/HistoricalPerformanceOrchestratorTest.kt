package io.github.filippovissani.portfolium.controller.service

import io.github.filippovissani.portfolium.controller.config.Config
import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.model.domain.DepositTransaction
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class HistoricalPerformanceOrchestratorTest :
    FunSpec({

        val mockConfig =
            Config(
                dataPath = "data",
                mainBankAccountFile = "main.yaml",
                plannedExpensesBankAccountFile = "planned.yaml",
                emergencyFundBankAccountFile = "emergency.yaml",
                investmentBankAccountFile = "investment.yaml",
                priceCacheFile = "cache.csv",
                cacheDurationHours = 24,
                historicalPerformanceIntervalDays = 7,
                serverPort = 8080,
            )

        val mockPriceSource =
            object : PriceDataSource {
                override fun getCurrentPrice(ticker: String): BigDecimal? = null

                override fun getHistoricalPrice(
                    ticker: String,
                    date: LocalDate,
                ): BigDecimal = BigDecimal("100.00")

                override fun getHistoricalPrices(
                    ticker: String,
                    startDate: LocalDate,
                    endDate: LocalDate,
                ): Map<LocalDate, BigDecimal> = emptyMap()
            }

        test("calculateForAccount should work with InvestmentBankAccount") {
            val transactions =
                listOf(
                    EtfBuyTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        name = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("400.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val account = InvestmentBankAccount(transactions = transactions)

            val performance =
                HistoricalPerformanceOrchestrator.calculateForAccount(
                    account,
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldNotBeNull()
            performance.dataPoints.isNotEmpty() shouldBe true
        }

        test("calculateForAccount should work with PlannedExpensesBankAccount") {
            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        amount = BigDecimal("1000.00"),
                    ),
                    EtfBuyTransaction(
                        date = LocalDate.of(2024, 1, 15),
                        name = "World ETF",
                        ticker = "VT",
                        area = "World",
                        quantity = BigDecimal("5"),
                        price = BigDecimal("100.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val account = PlannedExpensesBankAccount(transactions = transactions)

            val performance =
                HistoricalPerformanceOrchestrator.calculateForAccount(
                    account,
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldNotBeNull()
        }

        test("calculateForAccount should work with EmergencyFundBankAccount") {
            val transactions =
                listOf(
                    EtfBuyTransaction(
                        date = LocalDate.of(2024, 1, 1),
                        name = "Bond ETF",
                        ticker = "BND",
                        area = "US",
                        quantity = BigDecimal("20"),
                        price = BigDecimal("80.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val account = EmergencyFundBankAccount(transactions = transactions)

            val performance =
                HistoricalPerformanceOrchestrator.calculateForAccount(
                    account,
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldNotBeNull()
        }

        test("calculateForAccount should return null for empty account") {
            val account = InvestmentBankAccount()

            val performance =
                HistoricalPerformanceOrchestrator.calculateForAccount(
                    account,
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldBeNull()
        }

        test("calculateCombined should combine multiple accounts") {
            val investmentAccount =
                InvestmentBankAccount(
                    transactions =
                        listOf(
                            EtfBuyTransaction(
                                date = LocalDate.of(2024, 1, 1),
                                name = "S&P 500 ETF",
                                ticker = "SPY",
                                area = "US",
                                quantity = BigDecimal("10"),
                                price = BigDecimal("400.00"),
                                fees = BigDecimal.ZERO,
                            ),
                        ),
                )

            val plannedAccount =
                PlannedExpensesBankAccount(
                    transactions =
                        listOf(
                            EtfBuyTransaction(
                                date = LocalDate.of(2024, 2, 1),
                                name = "World ETF",
                                ticker = "VT",
                                area = "World",
                                quantity = BigDecimal("5"),
                                price = BigDecimal("100.00"),
                                fees = BigDecimal.ZERO,
                            ),
                        ),
                )

            val performance =
                HistoricalPerformanceOrchestrator.calculateCombined(
                    listOf(investmentAccount, plannedAccount),
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldNotBeNull()
            performance.dataPoints.isNotEmpty() shouldBe true
        }

        test("calculateCombined should return null for empty accounts list") {
            val performance =
                HistoricalPerformanceOrchestrator.calculateCombined(
                    emptyList(),
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldBeNull()
        }

        test("calculateCombined should handle accounts with no transactions") {
            val emptyAccount = InvestmentBankAccount()

            val performance =
                HistoricalPerformanceOrchestrator.calculateCombined(
                    listOf(emptyAccount),
                    mockPriceSource,
                    mockConfig,
                )

            performance.shouldBeNull()
        }
    })

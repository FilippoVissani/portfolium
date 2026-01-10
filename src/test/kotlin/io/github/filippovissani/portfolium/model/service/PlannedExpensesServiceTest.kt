package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.DepositTransaction
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.github.filippovissani.portfolium.model.domain.PlannedExpenseEntry
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class PlannedExpensesServiceTest :
    FunSpec({

        test("calculatePlannedExpensesSummary should calculate totals for liquid account") {
            val plannedExpenses =
                listOf(
                    PlannedExpenseEntry(
                        name = "Car",
                        expirationDate = LocalDate.of(2026, 12, 31),
                        estimatedAmount = BigDecimal("15000.00"),
                    ),
                    PlannedExpenseEntry(
                        name = "Vacation",
                        expirationDate = LocalDate.of(2025, 8, 1),
                        estimatedAmount = BigDecimal("3000.00"),
                    ),
                )

            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("5000.00"),
                        description = "Initial deposit",
                    ),
                )

            val account =
                PlannedExpensesBankAccount(
                    name = "Planned Expenses",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                    plannedExpenses = plannedExpenses,
                )

            val summary = PlannedExpensesService.calculatePlannedExpensesSummary(account)

            summary.totalEstimated shouldBe BigDecimal("18000.00")
            summary.totalAccrued shouldBe BigDecimal("5000.00")
            summary.liquidAccrued shouldBe BigDecimal("5000.00")
            summary.investedAccrued shouldBe BigDecimal("0.00")
            summary.isInvested shouldBe false
        }

        test("calculatePlannedExpensesSummary should detect invested account") {
            val plannedExpenses =
                listOf(
                    PlannedExpenseEntry(
                        name = "House",
                        expirationDate = LocalDate.of(2030, 12, 31),
                        estimatedAmount = BigDecimal("50000.00"),
                    ),
                )

            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("10000.00"),
                        description = "Initial deposit",
                    ),
                    EtfBuyTransaction(
                        date = LocalDate.of(2025, 1, 15),
                        name = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("450.00"),
                        fees = BigDecimal("5.00"),
                    ),
                )

            val account =
                PlannedExpensesBankAccount(
                    name = "Planned Expenses",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                    plannedExpenses = plannedExpenses,
                )

            val currentPrices = mapOf("SPY" to BigDecimal("480.00"))

            val summary = PlannedExpensesService.calculatePlannedExpensesSummary(account, currentPrices)

            summary.isInvested shouldBe true
            // Invested = 10 * 480 = 4800
            summary.investedAccrued shouldBe BigDecimal("4800.00")
            // Liquid = 10000 - 4505 (10*450 + 5 fees) = 5495
            summary.liquidAccrued shouldBe BigDecimal("5495.00")
            // Total = 4800 + 5495 = 10295
            summary.totalAccrued shouldBe BigDecimal("10295.00")
        }

        test("calculatePlannedExpensesSummary should calculate coverage ratio") {
            val plannedExpenses =
                listOf(
                    PlannedExpenseEntry(
                        name = "Car",
                        expirationDate = LocalDate.of(2026, 12, 31),
                        estimatedAmount = BigDecimal("10000.00"),
                    ),
                )

            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("5000.00"),
                        description = "Initial deposit",
                    ),
                )

            val account =
                PlannedExpensesBankAccount(
                    name = "Planned Expenses",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                    plannedExpenses = plannedExpenses,
                )

            val summary = PlannedExpensesService.calculatePlannedExpensesSummary(account)

            // Coverage = 5000 / 10000 = 0.5
            summary.coverageRatio.compareTo(BigDecimal("0.5")) shouldBe 0
        }

        test("calculatePlannedExpensesSummary should handle empty account") {
            val account = PlannedExpensesBankAccount()
            val summary = PlannedExpensesService.calculatePlannedExpensesSummary(account)

            summary.totalEstimated shouldBe BigDecimal("0.00")
            summary.totalAccrued shouldBe BigDecimal("0.00")
            summary.liquidAccrued shouldBe BigDecimal("0.00")
            summary.investedAccrued shouldBe BigDecimal("0.00")
            summary.isInvested shouldBe false
            summary.coverageRatio shouldBe BigDecimal.ZERO
        }

        test("calculatePlannedExpensesSummary should use average price when current price not available") {
            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("10000.00"),
                    ),
                    EtfBuyTransaction(
                        date = LocalDate.of(2025, 1, 15),
                        name = "ETF",
                        ticker = "XYZ",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("100.00"),
                        fees = BigDecimal.ZERO,
                    ),
                )

            val account =
                PlannedExpensesBankAccount(
                    transactions = transactions,
                    plannedExpenses =
                        listOf(
                            PlannedExpenseEntry(
                                name = "Test",
                                expirationDate = null,
                                estimatedAmount = BigDecimal("1000.00"),
                            ),
                        ),
                )

            // No current prices provided
            val summary = PlannedExpensesService.calculatePlannedExpensesSummary(account, emptyMap())

            // Should use average price: 10 * 100 = 1000
            summary.investedAccrued shouldBe BigDecimal("1000.00")
        }
    })

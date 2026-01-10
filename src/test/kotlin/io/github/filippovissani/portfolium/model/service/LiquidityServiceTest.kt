package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.LiquidTransaction
import io.github.filippovissani.portfolium.model.domain.MainBankAccount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class LiquidityServiceTest :
    FunSpec({

        test("calculateLiquiditySummary should calculate totals correctly") {
            val transactions =
                listOf(
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        description = "Salary",
                        category = "Income",
                        amount = BigDecimal("2000.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 5),
                        description = "Groceries",
                        category = "Food",
                        amount = BigDecimal("-150.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 10),
                        description = "Rent",
                        category = "Housing",
                        amount = BigDecimal("-800.00"),
                    ),
                )

            val account =
                MainBankAccount(
                    name = "Test Account",
                    initialBalance = BigDecimal("1000.00"),
                    transactions = transactions,
                )

            val summary = LiquidityService.calculateLiquiditySummary(account)

            summary.totalIncome shouldBe BigDecimal("2000.00")
            summary.totalExpense shouldBe BigDecimal("950.00")
            summary.net shouldBe BigDecimal("2050.00") // 1000 + 2000 - 150 - 800
        }

        test("calculateLiquiditySummary should calculate average monthly expense over 12 months") {
            val today = LocalDate.of(2025, 6, 15)
            val transactions =
                listOf(
                    // Expenses in last 12 months
                    LiquidTransaction(
                        date = today.minusMonths(6),
                        description = "Expense 1",
                        category = "Food",
                        amount = BigDecimal("-600.00"),
                    ),
                    LiquidTransaction(
                        date = today.minusMonths(3),
                        description = "Expense 2",
                        category = "Housing",
                        amount = BigDecimal("-1200.00"),
                    ),
                    // Expense older than 12 months - should not be included
                    LiquidTransaction(
                        date = today.minusMonths(13),
                        description = "Old Expense",
                        category = "Other",
                        amount = BigDecimal("-1000.00"),
                    ),
                )

            val account =
                MainBankAccount(
                    name = "Test Account",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                )

            val summary = LiquidityService.calculateLiquiditySummary(account, today)

            // Average monthly = (600 + 1200) / 12 = 150
            summary.avgMonthlyExpense12m shouldBe BigDecimal("150.00")
        }

        test("calculateLiquiditySummary should handle empty transactions") {
            val account =
                MainBankAccount(
                    name = "Empty Account",
                    initialBalance = BigDecimal("500.00"),
                    transactions = emptyList(),
                )

            val summary = LiquidityService.calculateLiquiditySummary(account)

            summary.totalIncome shouldBe BigDecimal("0.00")
            summary.totalExpense shouldBe BigDecimal("0.00")
            summary.net shouldBe BigDecimal("500.00")
            summary.avgMonthlyExpense12m shouldBe BigDecimal("0.00")
        }

        test("calculateLiquiditySummary should include transaction statistics") {
            val transactions =
                listOf(
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        description = "Salary",
                        category = "Income",
                        amount = BigDecimal("2000.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 5),
                        description = "Groceries",
                        category = "Food",
                        amount = BigDecimal("-150.00"),
                    ),
                )

            val account =
                MainBankAccount(
                    name = "Test Account",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                )

            val summary = LiquidityService.calculateLiquiditySummary(account)

            summary.statistics.shouldNotBeNull()
            summary.statistics.totalByCategory.size shouldBe 2
        }
    })

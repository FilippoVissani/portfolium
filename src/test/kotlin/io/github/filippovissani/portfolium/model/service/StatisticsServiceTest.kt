package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.LiquidTransaction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class StatisticsServiceTest :
    FunSpec({

        test("calculateTransactionStatistics should group by category") {
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
                        description = "Restaurant",
                        category = "Food",
                        amount = BigDecimal("-50.00"),
                    ),
                )

            val stats = StatisticsService.calculateTransactionStatistics(transactions)

            stats.totalByCategory["Income"] shouldBe BigDecimal("2000.00")
            stats.totalByCategory["Food"] shouldBe BigDecimal("200.00")
        }

        test("calculateTransactionStatistics should create monthly trend") {
            val transactions =
                listOf(
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        description = "Salary",
                        category = "Income",
                        amount = BigDecimal("2000.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 15),
                        description = "Groceries",
                        category = "Food",
                        amount = BigDecimal("-200.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 2, 1),
                        description = "Salary",
                        category = "Income",
                        amount = BigDecimal("2000.00"),
                    ),
                )

            val stats = StatisticsService.calculateTransactionStatistics(transactions)

            stats.monthlyTrend.size shouldBe 2
            stats.monthlyTrend[0].yearMonth shouldBe "2025-01"
            stats.monthlyTrend[0].income shouldBe BigDecimal("2000.00")
            stats.monthlyTrend[0].expense shouldBe BigDecimal("200.00")
            stats.monthlyTrend[0].net shouldBe BigDecimal("1800.00")

            stats.monthlyTrend[1].yearMonth shouldBe "2025-02"
            stats.monthlyTrend[1].income shouldBe BigDecimal("2000.00")
            stats.monthlyTrend[1].expense shouldBe BigDecimal("0.00")
        }

        test("calculateTransactionStatistics should identify top expense categories") {
            val transactions =
                listOf(
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        description = "Rent",
                        category = "Housing",
                        amount = BigDecimal("-1000.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 5),
                        description = "Groceries",
                        category = "Food",
                        amount = BigDecimal("-200.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 10),
                        description = "Transport",
                        category = "Transport",
                        amount = BigDecimal("-50.00"),
                    ),
                )

            val stats = StatisticsService.calculateTransactionStatistics(transactions)

            stats.topExpenseCategories.size shouldBe 3
            stats.topExpenseCategories[0].first shouldBe "Housing"
            stats.topExpenseCategories[0].second shouldBe BigDecimal("1000.00")
            stats.topExpenseCategories[1].first shouldBe "Food"
            stats.topExpenseCategories[1].second shouldBe BigDecimal("200.00")
        }

        test("calculateTransactionStatistics should identify top income categories") {
            val transactions =
                listOf(
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        description = "Salary",
                        category = "Salary",
                        amount = BigDecimal("3000.00"),
                    ),
                    LiquidTransaction(
                        date = LocalDate.of(2025, 1, 15),
                        description = "Freelance",
                        category = "Freelance",
                        amount = BigDecimal("500.00"),
                    ),
                )

            val stats = StatisticsService.calculateTransactionStatistics(transactions)

            stats.topIncomeCategories.size shouldBe 2
            stats.topIncomeCategories[0].first shouldBe "Salary"
            stats.topIncomeCategories[0].second shouldBe BigDecimal("3000.00")
            stats.topIncomeCategories[1].first shouldBe "Freelance"
            stats.topIncomeCategories[1].second shouldBe BigDecimal("500.00")
        }

        test("calculateTransactionStatistics should handle empty transactions") {
            val stats = StatisticsService.calculateTransactionStatistics(emptyList())

            stats.totalByCategory shouldBe emptyMap()
            stats.monthlyTrend shouldBe emptyList()
            stats.topExpenseCategories shouldBe emptyList()
            stats.topIncomeCategories shouldBe emptyList()
        }
    })

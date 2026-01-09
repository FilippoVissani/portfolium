package io.github.filippovissani.portfolium.logic

import io.github.filippovissani.portfolium.model.Calculators
import io.github.filippovissani.portfolium.model.DepositTransaction
import io.github.filippovissani.portfolium.model.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.EmergencyFundSummary
import io.github.filippovissani.portfolium.model.Investment
import io.github.filippovissani.portfolium.model.InvestmentsSummary
import io.github.filippovissani.portfolium.model.LiquidTransaction
import io.github.filippovissani.portfolium.model.LiquiditySummary
import io.github.filippovissani.portfolium.model.MainBankAccount
import io.github.filippovissani.portfolium.model.PlannedExpenseEntry
import io.github.filippovissani.portfolium.model.PlannedExpensesBankAccount
import io.github.filippovissani.portfolium.model.PlannedExpensesSummary
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class CalculatorsTest :
    StringSpec({
        fun bd2(s: String) = s.toBigDecimal().setScale(2)

        fun bd4(s: String) = s.toBigDecimal().setScale(4)

        fun bd6(s: String) = s.toBigDecimal().setScale(6)

        "summarizeLiquidity basic" {
            val today = LocalDate.of(2026, 1, 1)
            val txs =
                listOf(
                    LiquidTransaction(LocalDate.of(2025, 1, 15), "Salary", "Job", BigDecimal("1000"), null),
                    LiquidTransaction(LocalDate.of(2025, 2, 1), "Groceries", "Food", BigDecimal("-200"), null),
                    LiquidTransaction(LocalDate.of(2025, 12, 15), "Rent", "Housing", BigDecimal("-300"), null),
                    LiquidTransaction(LocalDate.of(2024, 12, 31), "Bonus", "Job", BigDecimal("500"), null),
                )
            val account = MainBankAccount("Test", BigDecimal.ZERO, txs)

            val s = Calculators.summarizeLiquidity(account, today)

            s.totalIncome shouldBe bd2("1500.00")
            s.totalExpense shouldBe bd2("500.00")
            s.net shouldBe bd2("1000.00")
            s.avgMonthlyExpense12m shouldBe bd2("41.67")
        }

        "summarizePlanned basic" {
            val items =
                listOf(
                    PlannedExpenseEntry("New Laptop", null, BigDecimal("1000")),
                    PlannedExpenseEntry("Trip", null, BigDecimal("500")),
                )
            val txs =
                listOf(
                    DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("900"), "Initial"),
                )
            val account = PlannedExpensesBankAccount("Test", BigDecimal.ZERO, txs, items)

            val s = Calculators.summarizePlanned(account)

            s.totalEstimated shouldBe bd2("1500.00")
            s.totalAccrued shouldBe bd2("900.00")
            s.coverageRatio shouldBe bd4("0.6000")
            s.isInvested shouldBe false
        }

        "summarizeEmergency basic" {
            val txs =
                listOf(
                    DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("5000"), "Initial"),
                )
            val account = EmergencyFundBankAccount("Test", BigDecimal.ZERO, txs, targetMonthlyExpenses = 6)
            val avgMonthlyExpense = BigDecimal("1000")

            val s = Calculators.summarizeEmergency(account, avgMonthlyExpense)

            s.targetCapital shouldBe bd2("6000.00")
            s.currentCapital shouldBe bd2("5000.00")
            s.deltaToTarget shouldBe bd2("1000.00")
            s.status shouldBe "BELOW TARGET"
            s.isLiquid shouldBe true
        }

        "summarizeInvestments basic" {
            val a =
                Investment(
                    etf = "ETF A",
                    ticker = "A",
                    area = null,
                    quantity = BigDecimal("10"),
                    averagePrice = BigDecimal("100"),
                    currentPrice = BigDecimal("110"),
                )
            val b =
                Investment(
                    etf = "ETF B",
                    ticker = "B",
                    area = null,
                    quantity = BigDecimal("20"),
                    averagePrice = BigDecimal("50"),
                    currentPrice = BigDecimal("60"),
                )

            val s = Calculators.summarizeInvestments(listOf(a, b))

            s.totalInvested shouldBe bd2("2000.00")
            s.totalCurrent shouldBe bd2("2300.00")

            val weights = s.itemsWithWeights.toMap()
            weights[a]!!.setScale(6) shouldBe bd6("0.478261")
            weights[b]!!.setScale(6) shouldBe bd6("0.521739")
        }

        "buildDashboard basic" {
            val today = LocalDate.of(2026, 1, 1)
            // Liquidity (from first test)
            val mainAccount =
                MainBankAccount(
                    "Test",
                    BigDecimal.ZERO,
                    listOf(
                        LiquidTransaction(LocalDate.of(2025, 1, 15), "Salary", "Job", BigDecimal("1000"), null),
                        LiquidTransaction(LocalDate.of(2025, 2, 1), "Groceries", "Food", BigDecimal("-200"), null),
                        LiquidTransaction(LocalDate.of(2025, 12, 15), "Rent", "Housing", BigDecimal("-300"), null),
                        LiquidTransaction(LocalDate.of(2024, 12, 31), "Bonus", "Job", BigDecimal("500"), null),
                    ),
                )
            val liquidity = Calculators.summarizeLiquidity(mainAccount, today)

            // Planned
            val plannedAccount =
                PlannedExpensesBankAccount(
                    "Test",
                    BigDecimal.ZERO,
                    listOf(DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("900"), "Initial")),
                    listOf(
                        PlannedExpenseEntry("New Laptop", null, BigDecimal("1000")),
                        PlannedExpenseEntry("Trip", null, BigDecimal("500")),
                    ),
                )
            val planned = Calculators.summarizePlanned(plannedAccount)

            // Emergency
            val emergencyAccount =
                EmergencyFundBankAccount(
                    "Test",
                    BigDecimal.ZERO,
                    listOf(DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("5000"), "Initial")),
                    targetMonthlyExpenses = 6,
                )
            val emergency = Calculators.summarizeEmergency(emergencyAccount, avgMonthlyExpense = BigDecimal("1000"))

            // Investments
            val inv =
                Calculators.summarizeInvestments(
                    listOf(
                        Investment("ETF A", "A", null, BigDecimal("10"), BigDecimal("100"), BigDecimal("110")),
                        Investment("ETF B", "B", null, BigDecimal("20"), BigDecimal("50"), BigDecimal("60")),
                    ),
                )

            val d = Calculators.buildPortfolio(liquidity, planned, emergency, inv)

            d.totalNetWorth shouldBe bd2("9200.00")
            d.percentInvested shouldBe bd4("0.2500")
            d.percentLiquid shouldBe bd4("0.7500")
        }

        "summarizeLiquidity empty" {
            val account = MainBankAccount("Test", BigDecimal.ZERO, emptyList())
            val s = Calculators.summarizeLiquidity(account, LocalDate.of(2026, 1, 1))
            s.totalIncome shouldBe bd2("0.00")
            s.totalExpense shouldBe bd2("0.00")
            s.net shouldBe bd2("0.00")
            s.avgMonthlyExpense12m shouldBe bd2("0.00")
        }

        "summarizePlanned zeroEstimated" {
            val account =
                PlannedExpensesBankAccount(
                    "Test",
                    BigDecimal.ZERO,
                    listOf(DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("10"), "Test")),
                    listOf(PlannedExpenseEntry("Zero", null, BigDecimal.ZERO)),
                )
            val s = Calculators.summarizePlanned(account)
            s.totalEstimated shouldBe bd2("0.00")
            s.totalAccrued shouldBe bd2("10.00")
            // coverageRatio is computed as BigDecimal.ZERO when totalEstimated is zero
            // compare numerically to avoid scale sensitivity
            s.coverageRatio.compareTo(BigDecimal.ZERO) shouldBe 0
            s.isInvested shouldBe false
        }

        "summarizeEmergency aboveTarget" {
            val account =
                EmergencyFundBankAccount(
                    "Test",
                    BigDecimal.ZERO,
                    listOf(DepositTransaction(LocalDate.of(2025, 1, 1), BigDecimal("5000"), "Initial")),
                    targetMonthlyExpenses = 3,
                )
            val s = Calculators.summarizeEmergency(account, avgMonthlyExpense = BigDecimal("1000"))
            s.targetCapital shouldBe bd2("3000.00")
            s.currentCapital shouldBe bd2("5000.00")
            s.deltaToTarget shouldBe bd2("-2000.00")
            s.status shouldBe "OK"
            s.isLiquid shouldBe true
        }

        "summarizeInvestments empty" {
            val s = Calculators.summarizeInvestments(emptyList())
            s.totalInvested shouldBe bd2("0.00")
            s.totalCurrent shouldBe bd2("0.00")
            s.itemsWithWeights.size shouldBe 0
        }

        "buildDashboard zeroes" {
            val d =
                Calculators.buildPortfolio(
                    LiquiditySummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), bd2("0.00"), null),
                    PlannedExpensesSummary(
                        bd2("0.00"),
                        bd2("0.00"),
                        bd4("0.0000"),
                        bd2("0.00"),
                        bd2("0.00"),
                        false,
                        null,
                    ),
                    EmergencyFundSummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), "OK", true, null),
                    InvestmentsSummary(bd2("0.00"), bd2("0.00"), emptyList()),
                    null,
                    null,
                )
            d.totalNetWorth shouldBe bd2("0.00")
            // compare numerically to avoid scale sensitivity
            d.percentInvested.compareTo(BigDecimal.ZERO) shouldBe 0
            d.percentLiquid.compareTo(BigDecimal.ONE) shouldBe 0
        }
    })

package io.github.filippovissani.portfolium.logic

import io.github.filippovissani.portfolium.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class CalculatorsTest : StringSpec({
    fun bd2(s: String) = s.toBigDecimal().setScale(2)
    fun bd4(s: String) = s.toBigDecimal().setScale(4)
    fun bd6(s: String) = s.toBigDecimal().setScale(6)

    "summarizeLiquidity basic" {
        val today = LocalDate.of(2026, 1, 1)
        val txs = listOf(
            Transaction(LocalDate.of(2025, 1, 15), "Salary", TransactionType.Income, "Job", "Bank", BigDecimal("1000"), null),
            Transaction(LocalDate.of(2025, 2, 1), "Groceries", TransactionType.Expense, "Food", "Card", BigDecimal("-200"), null),
            Transaction(LocalDate.of(2025, 12, 15), "Rent", TransactionType.Expense, "Housing", "Bank", BigDecimal("-300"), null),
            Transaction(LocalDate.of(2024, 12, 31), "Bonus", TransactionType.Income, "Job", "Bank", BigDecimal("500"), null)
        )

        val s = Calculators.summarizeLiquidity(txs, today)

        s.totalIncome shouldBe bd2("1500.00")
        s.totalExpense shouldBe bd2("500.00")
        s.net shouldBe bd2("1000.00")
        s.avgMonthlyExpense12m shouldBe bd2("41.67")
    }

    "summarizePlanned basic" {
        val items = listOf(
            PlannedExpense("New Laptop", BigDecimal("1000"), null, null, BigDecimal("400")),
            PlannedExpense("Trip", BigDecimal("500"), null, null, BigDecimal("500"))
        )

        val s = Calculators.summarizePlanned(items)

        s.totalEstimated shouldBe bd2("1500.00")
        s.totalAccrued shouldBe bd2("900.00")
        s.coverageRatio shouldBe bd4("0.6000")
    }

    "summarizeEmergency basic" {
        val config = EmergencyFundConfig(targetMonths = 6, currentCapital = BigDecimal("5000"))
        val avgMonthlyExpense = BigDecimal("1000")

        val s = Calculators.summarizeEmergency(config, avgMonthlyExpense)

        s.targetCapital shouldBe bd2("6000.00")
        s.currentCapital shouldBe bd2("5000.00")
        s.deltaToTarget shouldBe bd2("1000.00")
        s.status shouldBe "BELOW TARGET"
    }

    "summarizeInvestments basic" {
        val a = Investment(
            etf = "ETF A",
            ticker = "A",
            area = null,
            quantity = BigDecimal("10"),
            averagePrice = BigDecimal("100"),
            currentPrice = BigDecimal("110")
        )
        val b = Investment(
            etf = "ETF B",
            ticker = "B",
            area = null,
            quantity = BigDecimal("20"),
            averagePrice = BigDecimal("50"),
            currentPrice = BigDecimal("60")
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
        val liquidity = Calculators.summarizeLiquidity(
            listOf(
                Transaction(LocalDate.of(2025, 1, 15), "Salary", TransactionType.Income, "Job", "Bank", BigDecimal("1000"), null),
                Transaction(LocalDate.of(2025, 2, 1), "Groceries", TransactionType.Expense, "Food", "Card", BigDecimal("-200"), null),
                Transaction(LocalDate.of(2025, 12, 15), "Rent", TransactionType.Expense, "Housing", "Bank", BigDecimal("-300"), null),
                Transaction(LocalDate.of(2024, 12, 31), "Bonus", TransactionType.Income, "Job", "Bank", BigDecimal("500"), null)
            ), today
        )
        // Planned
        val planned = Calculators.summarizePlanned(
            listOf(
                PlannedExpense("New Laptop", BigDecimal("1000"), null, null, BigDecimal("400")),
                PlannedExpense("Trip", BigDecimal("500"), null, null, BigDecimal("500"))
            )
        )
        // Emergency
        val emergency = Calculators.summarizeEmergency(
            EmergencyFundConfig(targetMonths = 6, currentCapital = BigDecimal("5000")),
            avgMonthlyExpense = BigDecimal("1000")
        )
        // Investments
        val inv = Calculators.summarizeInvestments(
            listOf(
                Investment("ETF A", "A", null, BigDecimal("10"), BigDecimal("100"), BigDecimal("110")),
                Investment("ETF B", "B", null, BigDecimal("20"), BigDecimal("50"), BigDecimal("60"))
            )
        )

        val d = Calculators.buildDashboard(liquidity, planned, emergency, inv)

        d.totalNetWorth shouldBe bd2("9200.00")
        d.percentInvested shouldBe bd4("0.2500")
        d.percentLiquid shouldBe bd4("0.7500")
    }

    "summarizeLiquidity empty" {
        val s = Calculators.summarizeLiquidity(emptyList(), LocalDate.of(2026, 1, 1))
        s.totalIncome shouldBe bd2("0.00")
        s.totalExpense shouldBe bd2("0.00")
        s.net shouldBe bd2("0.00")
        s.avgMonthlyExpense12m shouldBe bd2("0.00")
    }

    "summarizePlanned zeroEstimated" {
        val items = listOf(
            PlannedExpense("Zero", BigDecimal.ZERO, null, null, BigDecimal("10"))
        )
        val s = Calculators.summarizePlanned(items)
        s.totalEstimated shouldBe bd2("0.00")
        s.totalAccrued shouldBe bd2("10.00")
        // coverageRatio is computed as BigDecimal.ZERO when totalEstimated is zero; compare numerically to avoid scale sensitivity
        s.coverageRatio.compareTo(BigDecimal.ZERO) shouldBe 0
    }

    "summarizeEmergency aboveTarget" {
        val s = Calculators.summarizeEmergency(
            EmergencyFundConfig(targetMonths = 3, currentCapital = BigDecimal("5000")),
            avgMonthlyExpense = BigDecimal("1000")
        )
        s.targetCapital shouldBe bd2("3000.00")
        s.currentCapital shouldBe bd2("5000.00")
        s.deltaToTarget shouldBe bd2("-2000.00")
        s.status shouldBe "OK"
    }

    "summarizeInvestments empty" {
        val s = Calculators.summarizeInvestments(emptyList())
        s.totalInvested shouldBe bd2("0.00")
        s.totalCurrent shouldBe bd2("0.00")
        s.itemsWithWeights.size shouldBe 0
    }

    "buildDashboard zeroes" {
        val d = Calculators.buildDashboard(
            LiquiditySummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), bd2("0.00")),
            PlannedExpensesSummary(bd2("0.00"), bd2("0.00"), bd4("0.0000")),
            EmergencyFundSummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), "OK"),
            InvestmentsSummary(bd2("0.00"), bd2("0.00"), emptyList())
        )
        d.totalNetWorth shouldBe bd2("0.00")
        // compare numerically to avoid scale sensitivity
        d.percentInvested.compareTo(BigDecimal.ZERO) shouldBe 0
        d.percentLiquid.compareTo(BigDecimal.ONE) shouldBe 0
    }

    "summarizeInvestmentsFromTransactions basic" {
        val txs = listOf(
            InvestmentTransaction(LocalDate.of(2025,1,10), "ETF A", "A", null, BigDecimal("10"), BigDecimal("100"), BigDecimal("2.50")),
            InvestmentTransaction(LocalDate.of(2025,2,10), "ETF A", "A", null, BigDecimal("5"), BigDecimal("120"), null),
            // partial sell: negative quantity; include fee which reduces total cost
            InvestmentTransaction(LocalDate.of(2025,3,15), "ETF A", "A", null, BigDecimal("-3"), BigDecimal("130"), BigDecimal("1.00")),
            // another instrument fully sold -> should be omitted in summary
            InvestmentTransaction(LocalDate.of(2025,1,5), "ETF B", "B", null, BigDecimal("2"), BigDecimal("50"), null),
            InvestmentTransaction(LocalDate.of(2025,2,5), "ETF B", "B", null, BigDecimal("-2"), BigDecimal("55"), BigDecimal("0.50")),
        )
        val prices = mapOf(
            "A" to BigDecimal("110"),
            "B" to BigDecimal("60")
        )
        val summary = Calculators.summarizeInvestmentsFromTransactions(txs, prices)
        // Only A remains with qty 12 (10 + 5 - 3)
        summary.totalCurrent shouldBe bd2("1320.00")
        // invested value uses computed average price
        // cost: A -> (10*100 + 2.50) + (5*120) + (-3*130 + 1.00) = 1000 + 2.50 + 600 - 390 + 1.00 = 1213.50
        // qty: 12 -> avg price = 1213.50 / 12 = 101.125 -> round to 6 decimals in model
        val item = summary.itemsWithWeights.first().first
        item.averagePrice.setScale(6) shouldBe bd6("101.125000")
        // compare numerically to avoid scale sensitivity
        item.investedValue.compareTo(bd2("1213.50")) shouldBe 0
        // weight should be 1 since only one item
        summary.itemsWithWeights.first().second.setScale(6) shouldBe BigDecimal.ONE.setScale(6)
    }
})

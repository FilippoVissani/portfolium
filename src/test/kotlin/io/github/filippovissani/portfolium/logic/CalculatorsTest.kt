package io.github.filippovissani.portfolium.logic

import io.github.filippovissani.portfolium.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal
import java.time.LocalDate

class CalculatorsTest {
    private fun bd2(s: String) = s.toBigDecimal().setScale(2)
    private fun bd4(s: String) = s.toBigDecimal().setScale(4)
    private fun bd6(s: String) = s.toBigDecimal().setScale(6)

    @Test
    fun testSummarizeLiquidity_basic() {
        val today = LocalDate.of(2026, 1, 1)
        val txs = listOf(
            Transaction(LocalDate.of(2025, 1, 15), "Salary", TransactionType.Income, "Job", "Bank", BigDecimal("1000"), null),
            Transaction(LocalDate.of(2025, 2, 1), "Groceries", TransactionType.Expense, "Food", "Card", BigDecimal("-200"), null),
            Transaction(LocalDate.of(2025, 12, 15), "Rent", TransactionType.Expense, "Housing", "Bank", BigDecimal("-300"), null),
            Transaction(LocalDate.of(2024, 12, 31), "Bonus", TransactionType.Income, "Job", "Bank", BigDecimal("500"), null)
        )

        val s = Calculators.summarizeLiquidity(txs, today)

        assertEquals(bd2("1500.00"), s.totalIncome)
        assertEquals(bd2("500.00"), s.totalExpense)
        assertEquals(bd2("1000.00"), s.net)
        assertEquals(bd2("41.67"), s.avgMonthlyExpense12m)
    }

    @Test
    fun testSummarizePlanned_basic() {
        val items = listOf(
            PlannedExpense("New Laptop", BigDecimal("1000"), null, null, BigDecimal("400"), null),
            PlannedExpense("Trip", BigDecimal("500"), null, null, BigDecimal("500"), null)
        )

        val s = Calculators.summarizePlanned(items)

        assertEquals(bd2("1500.00"), s.totalEstimated)
        assertEquals(bd2("900.00"), s.totalAccrued)
        assertEquals(bd4("0.6000"), s.coverageRatio)
    }

    @Test
    fun testSummarizeEmergency_basic() {
        val config = EmergencyFundConfig(targetMonths = 6, currentCapital = BigDecimal("5000"))
        val avgMonthlyExpense = BigDecimal("1000")

        val s = Calculators.summarizeEmergency(config, avgMonthlyExpense)

        assertEquals(bd2("6000.00"), s.targetCapital)
        assertEquals(bd2("5000.00"), s.currentCapital)
        assertEquals(bd2("1000.00"), s.deltaToTarget)
        assertEquals("BELOW TARGET", s.status)
    }

    @Test
    fun testSummarizeInvestments_basic() {
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

        assertEquals(bd2("2000.00"), s.totalInvested)
        assertEquals(bd2("2300.00"), s.totalCurrent)

        val weights = s.itemsWithWeights.toMap()
        assertEquals(bd6("0.478261"), weights[a])
        assertEquals(bd6("0.521739"), weights[b])
    }

    @Test
    fun testBuildDashboard_basic() {
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
                PlannedExpense("New Laptop", BigDecimal("1000"), null, null, BigDecimal("400"), null),
                PlannedExpense("Trip", BigDecimal("500"), null, null, BigDecimal("500"), null)
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

        assertEquals(bd2("9200.00"), d.totalNetWorth)
        assertEquals(bd4("0.2500"), d.percentInvested)
        assertEquals(bd4("0.7500"), d.percentLiquid)
    }

    @Test
    fun testSummarizeLiquidity_empty() {
        val s = Calculators.summarizeLiquidity(emptyList(), LocalDate.of(2026, 1, 1))
        assertEquals(bd2("0.00"), s.totalIncome)
        assertEquals(bd2("0.00"), s.totalExpense)
        assertEquals(bd2("0.00"), s.net)
        assertEquals(bd2("0.00"), s.avgMonthlyExpense12m)
    }

    @Test
    fun testSummarizePlanned_zeroEstimated() {
        val items = listOf(
            PlannedExpense("Zero", BigDecimal.ZERO, null, null, BigDecimal("10"), null)
        )
        val s = Calculators.summarizePlanned(items)
        assertEquals(bd2("0.00"), s.totalEstimated)
        assertEquals(bd2("10.00"), s.totalAccrued)
        // coverageRatio is computed as BigDecimal.ZERO when totalEstimated is zero; compare numerically to avoid scale sensitivity
        assertEquals(0, s.coverageRatio.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun testSummarizeEmergency_aboveTarget() {
        val s = Calculators.summarizeEmergency(
            EmergencyFundConfig(targetMonths = 3, currentCapital = BigDecimal("5000")),
            avgMonthlyExpense = BigDecimal("1000")
        )
        assertEquals(bd2("3000.00"), s.targetCapital)
        assertEquals(bd2("5000.00"), s.currentCapital)
        assertEquals(bd2("-2000.00"), s.deltaToTarget)
        assertEquals("OK", s.status)
    }

    @Test
    fun testSummarizeInvestments_empty() {
        val s = Calculators.summarizeInvestments(emptyList())
        assertEquals(bd2("0.00"), s.totalInvested)
        assertEquals(bd2("0.00"), s.totalCurrent)
        assertEquals(0, s.itemsWithWeights.size)
    }

    @Test
    fun testBuildDashboard_zeroes() {
        val d = Calculators.buildDashboard(
            LiquiditySummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), bd2("0.00")),
            PlannedExpensesSummary(bd2("0.00"), bd2("0.00"), bd4("0.0000")),
            EmergencyFundSummary(bd2("0.00"), bd2("0.00"), bd2("0.00"), "OK"),
            InvestmentsSummary(bd2("0.00"), bd2("0.00"), emptyList())
        )
        assertEquals(bd2("0.00"), d.totalNetWorth)
        assertEquals(BigDecimal.ZERO, d.percentInvested)
        assertEquals(BigDecimal.ONE, d.percentLiquid)
    }
}

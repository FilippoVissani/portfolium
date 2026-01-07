package io.github.filippovissani.portfolium.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class ModelsTest : StringSpec({
    fun bd(s: String) = s.toBigDecimal()

    "PlannedExpense delta is calculated correctly" {
        val expense = PlannedExpense(
            name = "Laptop",
            estimatedAmount = bd("1000"),
            horizon = "Short-term",
            dueDate = LocalDate.of(2026, 6, 1),
            accrued = bd("600"),
            instrument = null
        )
        expense.delta shouldBe bd("400")
    }

    "PlannedExpense isLiquid returns true for null instrument" {
        val expense = PlannedExpense("Item", bd("100"), null, null, bd("50"), null)
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for empty instrument" {
        val expense = PlannedExpense("Item", bd("100"), null, null, bd("50"), "")
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for liquid instrument" {
        val expense = PlannedExpense("Item", bd("100"), null, null, bd("50"), "liquid")
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for liquid instrument case insensitive" {
        val expense = PlannedExpense("Item", bd("100"), null, null, bd("50"), "LIQUID")
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns false for non-liquid instrument" {
        val expense = PlannedExpense("Item", bd("100"), null, null, bd("50"), "etf")
        expense.isLiquid shouldBe false
    }

    "EmergencyFundConfig isLiquid returns true for null instrument" {
        val config = EmergencyFundConfig(6, bd("5000"), null)
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns true for empty instrument" {
        val config = EmergencyFundConfig(6, bd("5000"), "")
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns true for liquid instrument" {
        val config = EmergencyFundConfig(6, bd("5000"), "liquid")
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns false for non-liquid instrument" {
        val config = EmergencyFundConfig(6, bd("5000"), "bond")
        config.isLiquid shouldBe false
    }

    "Investment investedValue is calculated correctly" {
        val investment = Investment(
            etf = "VWCE",
            ticker = "VWCE.DE",
            area = "EU",
            quantity = bd("10"),
            averagePrice = bd("100"),
            currentPrice = bd("110")
        )
        investment.investedValue shouldBe bd("1000")
    }

    "Investment currentValue is calculated correctly" {
        val investment = Investment(
            etf = "VWCE",
            ticker = "VWCE.DE",
            area = "EU",
            quantity = bd("10"),
            averagePrice = bd("100"),
            currentPrice = bd("110")
        )
        investment.currentValue shouldBe bd("1100")
    }

    "Investment pnl is calculated correctly for profit" {
        val investment = Investment(
            etf = "VWCE",
            ticker = "VWCE.DE",
            area = "EU",
            quantity = bd("10"),
            averagePrice = bd("100"),
            currentPrice = bd("110")
        )
        investment.pnl shouldBe bd("100")
    }

    "Investment pnl is calculated correctly for loss" {
        val investment = Investment(
            etf = "VWCE",
            ticker = "VWCE.DE",
            area = "EU",
            quantity = bd("10"),
            averagePrice = bd("100"),
            currentPrice = bd("90")
        )
        investment.pnl shouldBe bd("-100")
    }

    "Investment pnl is zero when price equals average" {
        val investment = Investment(
            etf = "VWCE",
            ticker = "VWCE.DE",
            area = "EU",
            quantity = bd("10"),
            averagePrice = bd("100"),
            currentPrice = bd("100")
        )
        investment.pnl shouldBe bd("0")
    }
})


package io.github.filippovissani.portfolium.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class ModelsTest : StringSpec({
    fun bd(s: String) = s.toBigDecimal()

    "PlannedExpense delta is calculated correctly" {
        val goal = PlannedExpenseGoal(
            name = "Laptop",
            estimatedAmount = bd("1000"),
            horizon = "Short-term",
            dueDate = LocalDate.of(2026, 6, 1),
            instrument = null
        )
        val expense = PlannedExpense(goal = goal, accrued = bd("600"))
        expense.delta shouldBe bd("400")
    }

    "PlannedExpense isLiquid returns true for null instrument" {
        val goal = PlannedExpenseGoal("Item", bd("100"), null, null, null)
        val expense = PlannedExpense(goal, bd("50"))
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for empty instrument" {
        val goal = PlannedExpenseGoal("Item", bd("100"), null, null, "")
        val expense = PlannedExpense(goal, bd("50"))
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for liquid instrument" {
        val goal = PlannedExpenseGoal("Item", bd("100"), null, null, "liquid")
        val expense = PlannedExpense(goal, bd("50"))
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns true for liquid instrument case insensitive" {
        val goal = PlannedExpenseGoal("Item", bd("100"), null, null, "LIQUID")
        val expense = PlannedExpense(goal, bd("50"))
        expense.isLiquid shouldBe true
    }

    "PlannedExpense isLiquid returns false for non-liquid instrument" {
        val goal = PlannedExpenseGoal("Item", bd("100"), null, null, "etf")
        val expense = PlannedExpense(goal, bd("50"))
        expense.isLiquid shouldBe false
    }

    "EmergencyFundConfig isLiquid returns true for null instrument" {
        val goal = EmergencyFundGoal(6, null)
        val config = EmergencyFundConfig(goal, bd("5000"))
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns true for empty instrument" {
        val goal = EmergencyFundGoal(6, "")
        val config = EmergencyFundConfig(goal, bd("5000"))
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns true for liquid instrument" {
        val goal = EmergencyFundGoal(6, "liquid")
        val config = EmergencyFundConfig(goal, bd("5000"))
        config.isLiquid shouldBe true
    }

    "EmergencyFundConfig isLiquid returns false for non-liquid instrument" {
        val goal = EmergencyFundGoal(6, "bond")
        val config = EmergencyFundConfig(goal, bd("5000"))
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


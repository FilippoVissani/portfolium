package io.github.filippovissani.portfolium.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ModelsTest : StringSpec({
    fun bd(s: String) = s.toBigDecimal()

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


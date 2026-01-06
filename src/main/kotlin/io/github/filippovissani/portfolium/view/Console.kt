package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio
import java.math.BigDecimal
import java.math.RoundingMode

object Console {
    fun printDashboard(d: Portfolio) {
        println("=== Personal Finance Dashboard ===")
        println()
        println("-- Liquidity (Transactions) --")
        println("Total income: ${d.liquidity.totalIncome}")
        println("Total expense: ${d.liquidity.totalExpense}")
        println("Net: ${d.liquidity.net}")
        println("Avg monthly expense (12m): ${d.liquidity.avgMonthlyExpense12m}")
        println()

        println("-- Planned & Predictable Expenses --")
        println("Total estimated: ${d.planned.totalEstimated}")
        println("Total accrued: ${d.planned.totalAccrued}")
        println("Coverage: ${(d.planned.coverageRatio * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println()

        println("-- Emergency Fund --")
        println("Target capital: ${d.emergency.targetCapital}")
        println("Current capital: ${d.emergency.currentCapital}")
        println("Delta to target: ${d.emergency.deltaToTarget}")
        println("Status: ${d.emergency.status}")
        println()

        println("-- Investments (Long Term) --")
        println("Total invested: ${d.investments.totalInvested}")
        println("Total current: ${d.investments.totalCurrent}")
        if (d.investments.itemsWithWeights.isNotEmpty()) {
            println("Breakdown:")
            d.investments.itemsWithWeights.forEach { (inv, w) ->
                val pct = (w * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                println("  - ${inv.etf} (${inv.ticker}): current=${inv.currentValue}, pnl=${inv.pnl}, weight=${pct}%")
            }
        }
        println()

        println("-- Summary --")
        println("Total net worth: ${d.totalNetWorth}")
        println("% Invested: ${(d.percentInvested * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println("% Liquid: ${(d.percentLiquid * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println()

        println("Tip: You can pass CSV path as argument")
    }
}
package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio
import java.math.BigDecimal
import java.math.RoundingMode

public class ConsoleView: IView {

    override fun render(portfolio: Portfolio, port: Int) {
        println("=== Personal Finance Dashboard ===")
        println()
        println("-- Liquidity (Transactions) --")
        println("Total income: ${portfolio.liquidity.totalIncome}")
        println("Total expense: ${portfolio.liquidity.totalExpense}")
        println("Net: ${portfolio.liquidity.net}")
        println("Avg monthly expense (12m): ${portfolio.liquidity.avgMonthlyExpense12m}")
        println()

        println("-- Planned & Predictable Expenses --")
        println("Total estimated: ${portfolio.planned.totalEstimated}")
        println("Total accrued: ${portfolio.planned.totalAccrued}")
        println("  - Liquid: ${portfolio.planned.liquidAccrued}")
        println("  - Invested: ${portfolio.planned.investedAccrued}")
        println("Coverage: ${(portfolio.planned.coverageRatio * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println()

        println("-- Emergency Fund --")
        println("Target capital: ${portfolio.emergency.targetCapital}")
        println("Current capital: ${portfolio.emergency.currentCapital}")
        println("Delta to target: ${portfolio.emergency.deltaToTarget}")
        println("Status: ${portfolio.emergency.status}")
        println("Type: ${if (portfolio.emergency.isLiquid) "Liquid" else "Invested"}")
        println()

        println("-- Investments (Long Term) --")
        println("Total invested: ${portfolio.investments.totalInvested}")
        println("Total current: ${portfolio.investments.totalCurrent}")
        if (portfolio.investments.itemsWithWeights.isNotEmpty()) {
            println("Breakdown:")
            portfolio.investments.itemsWithWeights.forEach { (inv, w) ->
                val pct = (w * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                println("  - ${inv.etf} (${inv.ticker}): current=${inv.currentValue}, pnl=${inv.pnl}, weight=$pct%")
            }
        }
        println()

        println("-- Summary --")
        println("Total net worth: ${portfolio.totalNetWorth}")
        println("% Invested: ${(portfolio.percentInvested * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println("% Liquid: ${(portfolio.percentLiquid * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%")
        println()

        println("Tip: You can pass CSV path as argument")
    }
}

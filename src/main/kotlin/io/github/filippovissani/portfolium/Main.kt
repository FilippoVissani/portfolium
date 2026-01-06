package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.logic.Calculators
import io.github.filippovissani.portfolium.csv.Loaders
import io.github.filippovissani.portfolium.model.Dashboard
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

fun main(args: Array<String>) {
    val transactionsCsv = args.getOrNull(0) ?: "data/transactions.csv"
    val plannedCsv = args.getOrNull(1) ?: "data/planned_expenses.csv"
    val emergencyCsv = args.getOrNull(2) ?: "data/emergency_fund.csv"
    val investmentsCsv = args.getOrNull(3) ?: "data/investments.csv"
    val pricesCsv = args.getOrNull(4) ?: "data/current_prices.csv"

    val loaders = Loaders()

    try {
        val transactions = loaders.loadTransactions(File(transactionsCsv))
        val planned = loaders.loadPlannedExpenses(File(plannedCsv))
        val emergency = loaders.loadEmergencyFund(File(emergencyCsv))
        val investments = loaders.loadInvestmentTransactions(File(investmentsCsv))
        val currentPrices = loaders.loadCurrentPrices(File(pricesCsv))

        val liquiditySummary = Calculators.summarizeLiquidity(transactions)
        val plannedSummary = Calculators.summarizePlanned(planned)
        val emergencySummary = Calculators.summarizeEmergency(emergency, liquiditySummary.avgMonthlyExpense12m)
        val investmentSummary = Calculators.summarizeInvestmentsFromTransactions(investments, currentPrices)
        val dashboard =
            Calculators.buildDashboard(liquiditySummary, plannedSummary, emergencySummary, investmentSummary)

        printDashboard(dashboard)
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        System.err.println("Usage: <transactions.csv> <planned_expenses.csv> <emergency_fund.csv> <investments.csv>")
    }
}

private fun printDashboard(d: Dashboard) {
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

    println("Tip: You can pass CSV paths as arguments in this order:")
    println("  transactions.csv planned_expenses.csv emergency_fund.csv investments.csv current_prices.csv")
}
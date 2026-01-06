package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.csv.Loaders
import io.github.filippovissani.portfolium.model.Calculators
import io.github.filippovissani.portfolium.view.Console.printDashboard
import java.io.File

object Controller {
    fun computePortfolioSummary(dataPath: String) {
        val transactionsCsv = "${dataPath}/transactions.csv"
        val plannedCsv = "${dataPath}/planned_expenses.csv"
        val emergencyCsv = "${dataPath}/emergency_fund.csv"
        val investmentsCsv = "${dataPath}/investments.csv"
        val pricesCsv = "${dataPath}/current_prices.csv"
        val loaders = Loaders()
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
            Calculators.buildPortfolio(liquiditySummary, plannedSummary, emergencySummary, investmentSummary)
        printDashboard(dashboard)
    }
}
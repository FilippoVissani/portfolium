package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.csv.Loaders
import io.github.filippovissani.portfolium.controller.datasource.CachedPriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.YahooFinancePriceDataSource
import io.github.filippovissani.portfolium.model.Calculators
import io.github.filippovissani.portfolium.model.HistoricalPerformanceCalculator
import io.github.filippovissani.portfolium.view.Console.printDashboard
import io.github.filippovissani.portfolium.view.WebView
import org.slf4j.LoggerFactory
import java.io.File

object Controller {
    private val logger = LoggerFactory.getLogger(Controller::class.java)
    fun computePortfolioSummary(dataPath: String) {
        logger.info("Using Yahoo Finance price source with caching")
        logger.info("Historical performance enabled (all available data)")

        // Load data files
        val transactionsCsv = "${dataPath}/transactions.csv"
        val plannedCsv = "${dataPath}/planned_expenses.csv"
        val emergencyCsv = "${dataPath}/emergency_fund.csv"
        val investmentsCsv = "${dataPath}/investments.csv"

        val loaders = Loaders()
        val transactions = loaders.loadTransactions(File(transactionsCsv))
        val planned = loaders.loadPlannedExpenses(File(plannedCsv))
        val emergency = loaders.loadEmergencyFund(File(emergencyCsv))
        val investments = loaders.loadInvestmentTransactions(File(investmentsCsv))

        // Use YahooFinancePriceDataSource with caching by default
        val priceSource = CachedPriceDataSource(
            delegate = YahooFinancePriceDataSource(),
            cacheFile = File("data/price_cache.csv"),
            cacheDurationHours = 24
        )

        // Get current prices for all unique tickers
        val tickers = investments.map { it.ticker }.distinct()
        val currentPrices = priceSource.getCurrentPrices(tickers)

        // Calculate summaries
        val liquiditySummary = Calculators.summarizeLiquidity(transactions)
        val plannedSummary = Calculators.summarizePlanned(planned)
        val emergencySummary = Calculators.summarizeEmergency(emergency, liquiditySummary.avgMonthlyExpense12m)
        val investmentSummary = Calculators.summarizeInvestmentsFromTransactions(investments, currentPrices)

        // Calculate historical performance
        val historicalPerformance = if (investments.isNotEmpty()) {
            logger.info("Calculating historical performance...")
            try {
                // Calculate from the earliest transaction date to today to support all time period views
                val earliestDate = investments.minOfOrNull { it.date } ?: java.time.LocalDate.now()
                HistoricalPerformanceCalculator.calculateHistoricalPerformance(
                    transactions = investments,
                    priceSource = priceSource,
                    startDate = earliestDate,
                    endDate = java.time.LocalDate.now(),
                    intervalDays = 7 // Weekly data points for better resolution
                )
            } catch (e: Exception) {
                logger.warn("Could not calculate historical performance", e)
                null
            }
        } else {
            null
        }

        // Build portfolio with historical performance
        val portfolio = Calculators.buildPortfolio(
            liquiditySummary,
            plannedSummary,
            emergencySummary,
            investmentSummary,
            historicalPerformance
        )

        printDashboard(portfolio)
        WebView.startServer(portfolio, 8080)
    }
}
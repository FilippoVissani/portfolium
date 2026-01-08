package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.config.ConfigLoader
import io.github.filippovissani.portfolium.controller.csv.Loaders
import io.github.filippovissani.portfolium.controller.datasource.CachedPriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.YahooFinancePriceDataSource
import io.github.filippovissani.portfolium.model.Calculators
import io.github.filippovissani.portfolium.model.HistoricalPerformanceCalculator
import io.github.filippovissani.portfolium.view.Console.printDashboard
import io.github.filippovissani.portfolium.view.WebView
import org.slf4j.LoggerFactory

object Controller {
    private val logger = LoggerFactory.getLogger(Controller::class.java)
    fun computePortfolioSummary() {
        // Load configuration
        val config = ConfigLoader.loadConfig()
        logger.info("Configuration loaded: data path = ${config.dataPath}")

        logger.info("Using Yahoo Finance price source with caching")
        logger.info("Historical performance enabled (all available data)")

        // Load data files
        val loaders = Loaders()
        val transactions = loaders.loadTransactions(config.getTransactionsPath())
        val planned = loaders.loadPlannedExpenses(config.getPlannedExpensesPath())
        val emergency = loaders.loadEmergencyFund(config.getEmergencyFundPath())
        val investments = loaders.loadInvestmentTransactions(config.getInvestmentsPath())

        // Use YahooFinancePriceDataSource with caching by default
        val priceSource = CachedPriceDataSource(
            delegate = YahooFinancePriceDataSource(),
            cacheFile = config.getPriceCachePath(),
            cacheDurationHours = config.cacheDurationHours
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
                    intervalDays = config.historicalPerformanceIntervalDays
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
        WebView.startServer(portfolio, config.serverPort)
    }
}
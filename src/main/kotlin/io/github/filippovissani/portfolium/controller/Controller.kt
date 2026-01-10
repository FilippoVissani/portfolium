package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.config.ConfigLoader
import io.github.filippovissani.portfolium.controller.datasource.CachedPriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.YahooFinancePriceDataSource
import io.github.filippovissani.portfolium.controller.service.BankAccountLoaderService
import io.github.filippovissani.portfolium.controller.service.HistoricalPerformanceOrchestrator
import io.github.filippovissani.portfolium.model.service.EmergencyFundService
import io.github.filippovissani.portfolium.model.service.InvestmentService
import io.github.filippovissani.portfolium.model.service.LiquidityService
import io.github.filippovissani.portfolium.model.service.PlannedExpensesService
import io.github.filippovissani.portfolium.model.service.PortfolioService
import io.github.filippovissani.portfolium.view.Console.printDashboard
import io.github.filippovissani.portfolium.view.WebView
import org.slf4j.LoggerFactory

object Controller {
    private val logger = LoggerFactory.getLogger(Controller::class.java)

    fun computePortfolioSummary() {
        // Load configuration
        val config = ConfigLoader.loadConfig()
        logger.info("Configuration loaded: data path = ${config.dataPath}")

        // Load specialized bank accounts from YAML files
        val mainBankAccount = BankAccountLoaderService.loadMainBankAccount(config)
        val plannedExpensesBankAccount = BankAccountLoaderService.loadPlannedExpensesBankAccount(config)
        val emergencyFundBankAccount = BankAccountLoaderService.loadEmergencyFundBankAccount(config)
        val investmentBankAccount = BankAccountLoaderService.loadInvestmentBankAccount(config)

        // Use YahooFinancePriceDataSource with caching by default
        val priceSource =
            CachedPriceDataSource(
                delegate = YahooFinancePriceDataSource(),
                cacheFile = config.getPriceCachePath(),
                cacheDurationHours = config.cacheDurationHours,
            )

        // Get current prices for all unique tickers from investment account and planned expenses account
        val tickers = (investmentBankAccount.etfHoldings.keys + plannedExpensesBankAccount.etfHoldings.keys).distinct()
        val currentPrices =
            if (tickers.isNotEmpty()) {
                priceSource.getCurrentPrices(tickers)
            } else {
                emptyMap()
            }

        // Calculate summaries using new services
        val liquiditySummary = LiquidityService.calculateLiquiditySummary(mainBankAccount)
        var plannedSummary =
            PlannedExpensesService.calculatePlannedExpensesSummary(
                plannedExpensesBankAccount,
                currentPrices,
            )
        var emergencySummary =
            EmergencyFundService.calculateEmergencyFundSummary(
                emergencyFundBankAccount,
                liquiditySummary.avgMonthlyExpense12m,
            )
        val investmentSummary = InvestmentService.calculateInvestmentsSummary(investmentBankAccount, currentPrices)

        // Calculate historical performance from investment bank account
        val investmentHistoricalPerformance =
            if (investmentBankAccount.transactions.isNotEmpty()) {
                logger.info("Calculating investment historical performance...")
                try {
                    HistoricalPerformanceOrchestrator.calculateForAccount(investmentBankAccount, priceSource, config)
                } catch (e: Exception) {
                    logger.warn("Could not calculate investment historical performance", e)
                    null
                }
            } else {
                null
            }

        // Calculate historical performance for planned expenses if invested
        val plannedHistoricalPerformance =
            if (plannedSummary.isInvested && plannedExpensesBankAccount.transactions.isNotEmpty()) {
                logger.info("Calculating planned expenses historical performance...")
                try {
                    HistoricalPerformanceOrchestrator.calculateForAccount(
                        plannedExpensesBankAccount,
                        priceSource,
                        config,
                    )
                } catch (e: Exception) {
                    logger.warn("Could not calculate planned expenses historical performance", e)
                    null
                }
            } else {
                null
            }
        plannedSummary = plannedSummary.copy(historicalPerformance = plannedHistoricalPerformance)

        // Calculate historical performance for emergency fund if invested
        val emergencyHistoricalPerformance =
            if (!emergencySummary.isLiquid && emergencyFundBankAccount.transactions.isNotEmpty()) {
                logger.info("Calculating emergency fund historical performance...")
                try {
                    HistoricalPerformanceOrchestrator.calculateForAccount(emergencyFundBankAccount, priceSource, config)
                } catch (e: Exception) {
                    logger.warn("Could not calculate emergency fund historical performance", e)
                    null
                }
            } else {
                null
            }
        emergencySummary = emergencySummary.copy(historicalPerformance = emergencyHistoricalPerformance)

        // Calculate overall historical performance combining all invested accounts
        val overallHistoricalPerformance =
            try {
                logger.info("Calculating overall historical performance...")
                val allAccounts =
                    listOfNotNull(
                        if (investmentBankAccount.transactions.isNotEmpty()) investmentBankAccount else null,
                        if (plannedSummary.isInvested &&
                            plannedExpensesBankAccount.transactions.isNotEmpty()
                        ) {
                            plannedExpensesBankAccount
                        } else {
                            null
                        },
                        if (!emergencySummary.isLiquid &&
                            emergencyFundBankAccount.transactions.isNotEmpty()
                        ) {
                            emergencyFundBankAccount
                        } else {
                            null
                        },
                    )

                if (allAccounts.isNotEmpty()) {
                    HistoricalPerformanceOrchestrator.calculateCombined(allAccounts, priceSource, config)
                } else {
                    null
                }
            } catch (e: Exception) {
                logger.warn("Could not calculate overall historical performance", e)
                null
            }

        // Build portfolio with historical performance
        val portfolio =
            PortfolioService.buildPortfolio(
                liquiditySummary,
                plannedSummary,
                emergencySummary,
                investmentSummary,
                investmentHistoricalPerformance,
                overallHistoricalPerformance,
            )

        printDashboard(portfolio)
        WebView.startServer(portfolio, config.serverPort)
    }
}

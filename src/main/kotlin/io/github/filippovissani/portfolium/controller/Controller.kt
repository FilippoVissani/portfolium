package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.config.ConfigLoader
import io.github.filippovissani.portfolium.controller.datasource.CachedPriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.YahooFinancePriceDataSource
import io.github.filippovissani.portfolium.controller.yaml.SpecializedBankAccountLoaders
import io.github.filippovissani.portfolium.model.*
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
        val mainBankAccount = try {
            if (config.getMainBankAccountPath().exists()) {
                SpecializedBankAccountLoaders.loadMainBankAccount(config.getMainBankAccountPath()).also {
                    logger.info("Main bank account loaded: ${it.name}, ${it.transactions.size} transactions")
                }
            } else {
                logger.info("Main bank account file not found, using empty account")
                MainBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading main bank account", e)
            MainBankAccount()
        }

        val plannedExpensesBankAccount = try {
            if (config.getPlannedExpensesBankAccountPath().exists()) {
                SpecializedBankAccountLoaders.loadPlannedExpensesBankAccount(config.getPlannedExpensesBankAccountPath()).also {
                    logger.info("Planned expenses bank account loaded: ${it.name}, ${it.transactions.size} transactions, ${it.plannedExpenses.size} planned expenses")
                }
            } else {
                logger.info("Planned expenses bank account file not found, using empty account")
                PlannedExpensesBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading planned expenses bank account", e)
            PlannedExpensesBankAccount()
        }

        val emergencyFundBankAccount = try {
            if (config.getEmergencyFundBankAccountPath().exists()) {
                SpecializedBankAccountLoaders.loadEmergencyFundBankAccount(config.getEmergencyFundBankAccountPath()).also {
                    logger.info("Emergency fund bank account loaded: ${it.name}, ${it.transactions.size} transactions, target: ${it.targetMonthlyExpenses} months")
                }
            } else {
                logger.info("Emergency fund bank account file not found, using empty account")
                EmergencyFundBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading emergency fund bank account", e)
            EmergencyFundBankAccount()
        }

        val investmentBankAccount = try {
            if (config.getInvestmentBankAccountPath().exists()) {
                SpecializedBankAccountLoaders.loadInvestmentBankAccount(config.getInvestmentBankAccountPath()).also {
                    logger.info("Investment bank account loaded: ${it.name}, ${it.transactions.size} transactions")
                }
            } else {
                logger.info("Investment bank account file not found, using empty account")
                InvestmentBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading investment bank account", e)
            InvestmentBankAccount()
        }

        // Use YahooFinancePriceDataSource with caching by default
        val priceSource = CachedPriceDataSource(
            delegate = YahooFinancePriceDataSource(),
            cacheFile = config.getPriceCachePath(),
            cacheDurationHours = config.cacheDurationHours
        )

        // Get current prices for all unique tickers from investment account
        val tickers = investmentBankAccount.etfHoldings.keys.toList()
        val currentPrices = if (tickers.isNotEmpty()) {
            priceSource.getCurrentPrices(tickers)
        } else {
            emptyMap()
        }

        // Calculate summaries using new bank accounts
        val liquiditySummary = Calculators.summarizeLiquidity(mainBankAccount)
        val plannedSummary = Calculators.summarizePlanned(plannedExpensesBankAccount)
        val emergencySummary = Calculators.summarizeEmergency(emergencyFundBankAccount, liquiditySummary.avgMonthlyExpense12m)
        val investmentSummary = Calculators.summarizeInvestments(investmentBankAccount, currentPrices)

        // Calculate historical performance from investment bank account
        val historicalPerformance = if (investmentBankAccount.transactions.isNotEmpty()) {
            logger.info("Calculating historical performance...")
            try {
                // Extract ETF transactions for historical calculation
                val etfTransactions = investmentBankAccount.transactions.mapNotNull { tx ->
                    when (tx) {
                        is EtfBuyTransaction ->
                            InvestmentTransaction(
                                date = tx.date,
                                etf = tx.name,
                                ticker = tx.ticker,
                                area = tx.area,
                                quantity = tx.quantity,
                                price = tx.price,
                                fees = tx.fees
                            )
                        is EtfSellTransaction ->
                            InvestmentTransaction(
                                date = tx.date,
                                etf = tx.name,
                                ticker = tx.ticker,
                                area = tx.area,
                                quantity = -tx.quantity,
                                price = tx.price,
                                fees = tx.fees
                            )
                        else -> null
                    }
                }

                if (etfTransactions.isNotEmpty()) {
                    val earliestDate = etfTransactions.minOfOrNull { it.date } ?: java.time.LocalDate.now()
                    HistoricalPerformanceCalculator.calculateHistoricalPerformance(
                        transactions = etfTransactions,
                        priceSource = priceSource,
                        startDate = earliestDate,
                        endDate = java.time.LocalDate.now(),
                        intervalDays = config.historicalPerformanceIntervalDays
                    )
                } else {
                    null
                }
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
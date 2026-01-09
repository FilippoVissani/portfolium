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
        var plannedSummary = Calculators.summarizePlanned(plannedExpensesBankAccount)
        var emergencySummary = Calculators.summarizeEmergency(emergencyFundBankAccount, liquiditySummary.avgMonthlyExpense12m)
        val investmentSummary = Calculators.summarizeInvestments(investmentBankAccount, currentPrices)

        // Calculate historical performance from investment bank account
        val investmentHistoricalPerformance = if (investmentBankAccount.transactions.isNotEmpty()) {
            logger.info("Calculating investment historical performance...")
            try {
                calculateHistoricalPerformanceForAccount(investmentBankAccount, priceSource, config)
            } catch (e: Exception) {
                logger.warn("Could not calculate investment historical performance", e)
                null
            }
        } else {
            null
        }

        // Calculate historical performance for planned expenses if invested
        val plannedHistoricalPerformance = if (plannedSummary.isInvested && plannedExpensesBankAccount.transactions.isNotEmpty()) {
            logger.info("Calculating planned expenses historical performance...")
            try {
                calculateHistoricalPerformanceForAccount(plannedExpensesBankAccount, priceSource, config)
            } catch (e: Exception) {
                logger.warn("Could not calculate planned expenses historical performance", e)
                null
            }
        } else {
            null
        }
        plannedSummary = plannedSummary.copy(historicalPerformance = plannedHistoricalPerformance)

        // Calculate historical performance for emergency fund if invested
        val emergencyHistoricalPerformance = if (!emergencySummary.isLiquid && emergencyFundBankAccount.transactions.isNotEmpty()) {
            logger.info("Calculating emergency fund historical performance...")
            try {
                calculateHistoricalPerformanceForAccount(emergencyFundBankAccount, priceSource, config)
            } catch (e: Exception) {
                logger.warn("Could not calculate emergency fund historical performance", e)
                null
            }
        } else {
            null
        }
        emergencySummary = emergencySummary.copy(historicalPerformance = emergencyHistoricalPerformance)

        // Calculate overall historical performance combining all invested accounts
        val overallHistoricalPerformance = try {
            logger.info("Calculating overall historical performance...")
            val allAccounts = listOfNotNull(
                if (investmentBankAccount.transactions.isNotEmpty()) investmentBankAccount else null,
                if (plannedSummary.isInvested && plannedExpensesBankAccount.transactions.isNotEmpty()) plannedExpensesBankAccount else null,
                if (!emergencySummary.isLiquid && emergencyFundBankAccount.transactions.isNotEmpty()) emergencyFundBankAccount else null
            )

            if (allAccounts.isNotEmpty()) {
                calculateCombinedHistoricalPerformance(allAccounts, priceSource, config)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Could not calculate overall historical performance", e)
            null
        }

        // Build portfolio with historical performance
        val portfolio = Calculators.buildPortfolio(
            liquiditySummary,
            plannedSummary,
            emergencySummary,
            investmentSummary,
            investmentHistoricalPerformance,
            overallHistoricalPerformance
        )

        printDashboard(portfolio)
        WebView.startServer(portfolio, config.serverPort)
    }

    private fun calculateHistoricalPerformanceForAccount(
        account: Any,
        priceSource: io.github.filippovissani.portfolium.controller.datasource.PriceDataSource,
        config: io.github.filippovissani.portfolium.controller.config.Config
    ): HistoricalPerformance? {
        val transactions = when (account) {
            is InvestmentBankAccount -> account.transactions
            is PlannedExpensesBankAccount -> account.transactions
            is EmergencyFundBankAccount -> account.transactions
            else -> emptyList()
        }

        val etfTransactions = transactions.mapNotNull { tx ->
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

        return if (etfTransactions.isNotEmpty()) {
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
    }

    private fun calculateCombinedHistoricalPerformance(
        accounts: List<Any>,
        priceSource: io.github.filippovissani.portfolium.controller.datasource.PriceDataSource,
        config: io.github.filippovissani.portfolium.controller.config.Config
    ): HistoricalPerformance? {
        val allEtfTransactions = accounts.flatMap { account ->
            val transactions = when (account) {
                is InvestmentBankAccount -> account.transactions
                is PlannedExpensesBankAccount -> account.transactions
                is EmergencyFundBankAccount -> account.transactions
                else -> emptyList()
            }

            transactions.mapNotNull { tx ->
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
        }

        return if (allEtfTransactions.isNotEmpty()) {
            val earliestDate = allEtfTransactions.minOfOrNull { it.date } ?: java.time.LocalDate.now()
            HistoricalPerformanceCalculator.calculateHistoricalPerformance(
                transactions = allEtfTransactions,
                priceSource = priceSource,
                startDate = earliestDate,
                endDate = java.time.LocalDate.now(),
                intervalDays = config.historicalPerformanceIntervalDays
            )
        } else {
            null
        }
    }
}
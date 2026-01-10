package io.github.filippovissani.portfolium.controller.service

import io.github.filippovissani.portfolium.controller.config.Config
import io.github.filippovissani.portfolium.controller.yaml.BankAccountLoaders
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.github.filippovissani.portfolium.model.domain.MainBankAccount
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import org.slf4j.LoggerFactory

/**
 * Service for loading bank accounts from configuration
 */
object BankAccountLoaderService {
    private val logger = LoggerFactory.getLogger(BankAccountLoaderService::class.java)

    /**
     * Load main bank account from file
     */
    fun loadMainBankAccount(config: Config): MainBankAccount =
        try {
            if (config.getMainBankAccountPath().exists()) {
                BankAccountLoaders.loadMainBankAccount(config.getMainBankAccountPath()).also {
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

    /**
     * Load planned expenses bank account from file
     */
    fun loadPlannedExpensesBankAccount(config: Config): PlannedExpensesBankAccount =
        try {
            if (config.getPlannedExpensesBankAccountPath().exists()) {
                BankAccountLoaders.loadPlannedExpensesBankAccount(config.getPlannedExpensesBankAccountPath()).also {
                    logger.info(
                        "Planned expenses bank account loaded: ${it.name}, ${it.transactions.size} transactions, ${it.plannedExpenses.size} planned expenses",
                    )
                }
            } else {
                logger.info("Planned expenses bank account file not found, using empty account")
                PlannedExpensesBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading planned expenses bank account", e)
            PlannedExpensesBankAccount()
        }

    /**
     * Load emergency fund bank account from file
     */
    fun loadEmergencyFundBankAccount(config: Config): EmergencyFundBankAccount =
        try {
            if (config.getEmergencyFundBankAccountPath().exists()) {
                BankAccountLoaders.loadEmergencyFundBankAccount(config.getEmergencyFundBankAccountPath()).also {
                    logger.info(
                        "Emergency fund bank account loaded: ${it.name}, ${it.transactions.size} transactions, target: ${it.targetMonthlyExpenses} months",
                    )
                }
            } else {
                logger.info("Emergency fund bank account file not found, using empty account")
                EmergencyFundBankAccount()
            }
        } catch (e: Exception) {
            logger.warn("Error loading emergency fund bank account", e)
            EmergencyFundBankAccount()
        }

    /**
     * Load investment bank account from file
     */
    fun loadInvestmentBankAccount(config: Config): InvestmentBankAccount =
        try {
            if (config.getInvestmentBankAccountPath().exists()) {
                BankAccountLoaders.loadInvestmentBankAccount(config.getInvestmentBankAccountPath()).also {
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
}

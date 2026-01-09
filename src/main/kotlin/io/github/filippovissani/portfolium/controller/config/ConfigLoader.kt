package io.github.filippovissani.portfolium.controller.config

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Loads configuration from application.properties file in resources
 */
object ConfigLoader {
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)

    /**
     * Load configuration from application.properties file
     * @return Config object with loaded properties
     */
    fun loadConfig(): Config {
        val properties = Properties()

        val resourceStream = ConfigLoader::class.java.classLoader.getResourceAsStream("application.properties")

        if (resourceStream == null) {
            logger.warn("application.properties not found in resources, using defaults")
            return getDefaultConfig()
        }

        try {
            resourceStream.use { stream ->
                properties.load(stream)
            }
            logger.info("Configuration loaded from application.properties")
        } catch (e: Exception) {
            logger.error("Error loading application.properties, using defaults", e)
            return getDefaultConfig()
        }

        return Config(
            dataPath = properties.getProperty("data.path", "data"),
            mainBankAccountFile = properties.getProperty("data.main.bank.account", "main_bank_account.yaml"),
            plannedExpensesBankAccountFile = properties.getProperty(
                "data.planned.expenses.bank.account",
                "planned_expenses_bank_account.yaml"
            ),
            emergencyFundBankAccountFile = properties.getProperty(
                "data.emergency.fund.bank.account",
                "emergency_fund_bank_account.yaml"
            ),
            investmentBankAccountFile = properties.getProperty(
                "data.investment.bank.account",
                "investment_bank_account.yaml"
            ),
            priceCacheFile = properties.getProperty("data.price.cache", "price_cache.csv"),
            cacheDurationHours = properties.getProperty("cache.duration.hours", "24").toLongOrNull() ?: 24L,
            historicalPerformanceIntervalDays = properties.getProperty("historical.performance.interval.days", "7")
                .toLongOrNull() ?: 7L,
            serverPort = properties.getProperty("server.port", "8080").toIntOrNull() ?: 8080
        )
    }

    /**
     * Get default configuration
     */
    private fun getDefaultConfig(): Config {
        return Config(
            dataPath = "data",
            mainBankAccountFile = "main_bank_account.yaml",
            plannedExpensesBankAccountFile = "planned_expenses_bank_account.yaml",
            emergencyFundBankAccountFile = "emergency_fund_bank_account.yaml",
            investmentBankAccountFile = "investment_bank_account.yaml",
            priceCacheFile = "price_cache.csv",
            cacheDurationHours = 24L,
            historicalPerformanceIntervalDays = 7L,
            serverPort = 8080
        )
    }
}


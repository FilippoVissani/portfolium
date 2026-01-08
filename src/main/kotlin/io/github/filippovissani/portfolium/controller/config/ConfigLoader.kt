package io.github.filippovissani.portfolium.controller.config

import org.slf4j.LoggerFactory
import java.util.Properties

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
            transactionsFile = properties.getProperty("data.transactions", "transactions.csv"),
            plannedExpensesFile = properties.getProperty("data.planned.expenses", "planned_expenses.csv"),
            emergencyFundFile = properties.getProperty("data.emergency.fund", "emergency_fund.csv"),
            investmentsFile = properties.getProperty("data.investments", "investments.csv"),
            priceCacheFile = properties.getProperty("data.price.cache", "price_cache.csv"),
            cacheDurationHours = properties.getProperty("cache.duration.hours", "24").toLongOrNull() ?: 24L,
            historicalPerformanceIntervalDays = properties.getProperty("historical.performance.interval.days", "7").toLongOrNull() ?: 7L,
            serverPort = properties.getProperty("server.port", "8080").toIntOrNull() ?: 8080
        )
    }

    /**
     * Get default configuration
     */
    private fun getDefaultConfig(): Config {
        return Config(
            dataPath = "data",
            transactionsFile = "transactions.csv",
            plannedExpensesFile = "planned_expenses.csv",
            emergencyFundFile = "emergency_fund.csv",
            investmentsFile = "investments.csv",
            priceCacheFile = "price_cache.csv",
            cacheDurationHours = 24L,
            historicalPerformanceIntervalDays = 7L,
            serverPort = 8080
        )
    }
}


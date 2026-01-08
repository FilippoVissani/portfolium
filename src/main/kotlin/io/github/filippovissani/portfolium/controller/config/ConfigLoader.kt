package io.github.filippovissani.portfolium.controller.config

import io.github.filippovissani.portfolium.model.EmergencyFundGoal
import io.github.filippovissani.portfolium.model.PlannedExpenseGoal
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

/**
 * Loads configuration from application.properties file in resources
 */
object ConfigLoader {
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

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

        // Parse emergency fund goal
        val emergencyFundGoal = EmergencyFundGoal(
            targetMonths = properties.getProperty("emergency.fund.target.months", "6").toIntOrNull() ?: 6,
            instrument = properties.getProperty("emergency.fund.instrument")
        )

        // Parse planned expense goals
        val plannedExpenseGoals = mutableListOf<PlannedExpenseGoal>()
        var index = 0
        while (true) {
            val name = properties.getProperty("planned.expense.$index.name") ?: break
            val estimatedAmount = properties.getProperty("planned.expense.$index.estimated.amount")
                ?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val horizon = properties.getProperty("planned.expense.$index.horizon")
            val dueDateStr = properties.getProperty("planned.expense.$index.due.date")
            val dueDate = dueDateStr?.let {
                try {
                    LocalDate.parse(it, dateFormatter)
                } catch (e: Exception) {
                    logger.warn("Invalid date format for planned.expense.$index.due.date: $it")
                    null
                }
            }
            val instrument = properties.getProperty("planned.expense.$index.instrument")

            plannedExpenseGoals.add(
                PlannedExpenseGoal(
                    name = name,
                    estimatedAmount = estimatedAmount,
                    horizon = horizon,
                    dueDate = dueDate,
                    instrument = instrument
                )
            )
            index++
        }

        return Config(
            dataPath = properties.getProperty("data.path", "data"),
            transactionsFile = properties.getProperty("data.transactions", "transactions.csv"),
            plannedExpensesTransactionsFile = properties.getProperty("data.planned.expenses.transactions", "planned_expenses_transactions.csv"),
            emergencyFundTransactionsFile = properties.getProperty("data.emergency.fund.transactions", "emergency_fund_transactions.csv"),
            investmentsFile = properties.getProperty("data.investments", "investments.csv"),
            priceCacheFile = properties.getProperty("data.price.cache", "price_cache.csv"),
            cacheDurationHours = properties.getProperty("cache.duration.hours", "24").toLongOrNull() ?: 24L,
            historicalPerformanceIntervalDays = properties.getProperty("historical.performance.interval.days", "7").toLongOrNull() ?: 7L,
            serverPort = properties.getProperty("server.port", "8080").toIntOrNull() ?: 8080,
            emergencyFundGoal = emergencyFundGoal,
            plannedExpenseGoals = plannedExpenseGoals
        )
    }

    /**
     * Get default configuration
     */
    private fun getDefaultConfig(): Config {
        return Config(
            dataPath = "data",
            transactionsFile = "transactions.csv",
            plannedExpensesTransactionsFile = "planned_expenses_transactions.csv",
            emergencyFundTransactionsFile = "emergency_fund_transactions.csv",
            investmentsFile = "investments.csv",
            priceCacheFile = "price_cache.csv",
            cacheDurationHours = 24L,
            historicalPerformanceIntervalDays = 7L,
            serverPort = 8080,
            emergencyFundGoal = EmergencyFundGoal(targetMonths = 6, instrument = null),
            plannedExpenseGoals = emptyList()
        )
    }
}


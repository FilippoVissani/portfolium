package io.github.filippovissani.portfolium.controller.config

import io.github.filippovissani.portfolium.model.EmergencyFundGoal
import io.github.filippovissani.portfolium.model.PlannedExpenseGoal
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Configuration data class for Portfolium application
 */
data class Config(
    val dataPath: String,
    val transactionsFile: String,
    val plannedExpensesTransactionsFile: String,
    val emergencyFundTransactionsFile: String,
    val investmentsFile: String,
    val priceCacheFile: String,
    val cacheDurationHours: Long,
    val historicalPerformanceIntervalDays: Long,
    val serverPort: Int,
    val emergencyFundGoal: EmergencyFundGoal,
    val plannedExpenseGoals: List<PlannedExpenseGoal>
) {
    /**
     * Get the full path for the transactions CSV file
     */
    fun getTransactionsPath(): File = File("$dataPath/$transactionsFile")

    /**
     * Get the full path for the planned expenses transactions CSV file
     */
    fun getPlannedExpensesTransactionsPath(): File = File("$dataPath/$plannedExpensesTransactionsFile")

    /**
     * Get the full path for the emergency fund transactions CSV file
     */
    fun getEmergencyFundTransactionsPath(): File = File("$dataPath/$emergencyFundTransactionsFile")

    /**
     * Get the full path for the investments CSV file
     */
    fun getInvestmentsPath(): File = File("$dataPath/$investmentsFile")

    /**
     * Get the full path for the price cache CSV file
     */
    fun getPriceCachePath(): File = File("$dataPath/$priceCacheFile")
}


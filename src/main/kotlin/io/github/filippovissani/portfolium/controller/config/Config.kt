package io.github.filippovissani.portfolium.controller.config

import java.io.File

/**
 * Configuration data class for Portfolium application
 */
data class Config(
    val dataPath: String,
    val transactionsFile: String,
    val plannedExpensesFile: String,
    val emergencyFundFile: String,
    val investmentsFile: String,
    val bankAccountFile: String,
    val priceCacheFile: String,
    val cacheDurationHours: Long,
    val historicalPerformanceIntervalDays: Long,
    val serverPort: Int
) {
    /**
     * Get the full path for the transactions CSV file
     */
    fun getTransactionsPath(): File = File("$dataPath/$transactionsFile")

    /**
     * Get the full path for the planned expenses CSV file
     */
    fun getPlannedExpensesPath(): File = File("$dataPath/$plannedExpensesFile")

    /**
     * Get the full path for the emergency fund CSV file
     */
    fun getEmergencyFundPath(): File = File("$dataPath/$emergencyFundFile")

    /**
     * Get the full path for the investments CSV file
     */
    fun getInvestmentsPath(): File = File("$dataPath/$investmentsFile")

    /**
     * Get the full path for the price cache CSV file
     */
    fun getPriceCachePath(): File = File("$dataPath/$priceCacheFile")

    /**
     * Get the full path for the bank account YAML file
     */
    fun getBankAccountPath(): File = File("$dataPath/$bankAccountFile")
}


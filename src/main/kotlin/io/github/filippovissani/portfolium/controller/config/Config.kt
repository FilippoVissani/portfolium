package io.github.filippovissani.portfolium.controller.config

import java.io.File

/**
 * Configuration data class for Portfolium application
 */
data class Config(
    val dataPath: String,
    val mainBankAccountFile: String,
    val plannedExpensesBankAccountFile: String,
    val emergencyFundBankAccountFile: String,
    val investmentBankAccountFile: String,
    val priceCacheFile: String,
    val cacheDurationHours: Long,
    val historicalPerformanceIntervalDays: Long,
    val serverPort: Int,
) {
    /**
     * Get the full path for the price cache CSV file
     */
    fun getPriceCachePath(): File = File("$dataPath/$priceCacheFile")

    /**
     * Get the full path for the main bank account YAML file
     */
    fun getMainBankAccountPath(): File = File("$dataPath/$mainBankAccountFile")

    /**
     * Get the full path for the planned expenses bank account YAML file
     */
    fun getPlannedExpensesBankAccountPath(): File = File("$dataPath/$plannedExpensesBankAccountFile")

    /**
     * Get the full path for the emergency fund bank account YAML file
     */
    fun getEmergencyFundBankAccountPath(): File = File("$dataPath/$emergencyFundBankAccountFile")

    /**
     * Get the full path for the investment bank account YAML file
     */
    fun getInvestmentBankAccountPath(): File = File("$dataPath/$investmentBankAccountFile")
}

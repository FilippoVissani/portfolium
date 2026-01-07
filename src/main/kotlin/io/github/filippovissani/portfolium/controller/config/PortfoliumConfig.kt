package io.github.filippovissani.portfolium.controller.config

import io.github.filippovissani.portfolium.controller.datasource.CsvPriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.controller.datasource.YahooFinancePriceDataSource
import java.io.File
import java.util.Properties

/**
 * Configuration for the portfolio application
 */
data class PortfoliumConfig(
    val priceDataSourceType: PriceDataSourceType = PriceDataSourceType.CSV,
    val csvPricesPath: String = "data/current_prices.csv",
    val enableHistoricalPerformance: Boolean = false,
    val historicalPerformanceMonths: Long = 12
) {
    companion object {
        /**
         * Load configuration from a properties file
         * If the file doesn't exist, returns default configuration
         */
        fun fromPropertiesFile(file: File): PortfoliumConfig {
            if (!file.exists()) {
                return PortfoliumConfig()
            }

            val properties = Properties()
            file.inputStream().use { properties.load(it) }

            return PortfoliumConfig(
                priceDataSourceType = properties.getProperty("price.source.type", "CSV")
                    .let { PriceDataSourceType.valueOf(it.uppercase()) },
                csvPricesPath = properties.getProperty("price.csv.path", "data/current_prices.csv"),
                enableHistoricalPerformance = properties.getProperty("performance.historical.enabled", "false")
                    .toBoolean(),
                historicalPerformanceMonths = properties.getProperty("performance.historical.months", "12")
                    .toLong()
            )
        }

        /**
         * Load configuration from environment variables and system properties
         */
        fun fromEnvironment(): PortfoliumConfig {
            val priceSourceType = System.getenv("PORTFOLIUM_PRICE_SOURCE")
                ?: System.getProperty("portfolium.price.source", "YAHOO_FINANCE")
            val csvPath = System.getenv("PORTFOLIUM_CSV_PRICES_PATH")
                ?: System.getProperty("portfolium.csv.prices.path", "data/current_prices.csv")
            val enableHistorical = (System.getenv("PORTFOLIUM_ENABLE_HISTORICAL")
                ?: System.getProperty("portfolium.enable.historical", "true")).toBoolean()
            val historicalMonths = (System.getenv("PORTFOLIUM_HISTORICAL_MONTHS")
                ?: System.getProperty("portfolium.historical.months", "12")).toLong()

            return PortfoliumConfig(
                priceDataSourceType = PriceDataSourceType.valueOf(priceSourceType.uppercase()),
                csvPricesPath = csvPath,
                enableHistoricalPerformance = enableHistorical,
                historicalPerformanceMonths = historicalMonths
            )
        }
    }

    /**
     * Create the appropriate price data source based on configuration
     */
    fun createPriceDataSource(): PriceDataSource {
        return when (priceDataSourceType) {
            PriceDataSourceType.CSV -> CsvPriceDataSource(File(csvPricesPath))
            PriceDataSourceType.YAHOO_FINANCE -> YahooFinancePriceDataSource()
        }
    }
}

enum class PriceDataSourceType {
    CSV,
    YAHOO_FINANCE
}


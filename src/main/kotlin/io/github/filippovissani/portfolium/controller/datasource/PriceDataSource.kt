package io.github.filippovissani.portfolium.controller.datasource

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Interface for price data sources - can be CSV or API-based
 */
interface PriceDataSource {
    /**
     * Get current price for a ticker
     */
    fun getCurrentPrice(ticker: String): BigDecimal?

    /**
     * Get historical price for a ticker at a specific date
     */
    fun getHistoricalPrice(
        ticker: String,
        date: LocalDate,
    ): BigDecimal?

    /**
     * Get historical prices for a ticker over a date range
     */
    fun getHistoricalPrices(
        ticker: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, BigDecimal>

    /**
     * Get current prices for multiple tickers (batch operation)
     */
    fun getCurrentPrices(tickers: List<String>): Map<String, BigDecimal> =
        tickers
            .mapNotNull { ticker ->
                getCurrentPrice(ticker)?.let { ticker to it }
            }.toMap()
}

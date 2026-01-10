package io.github.filippovissani.portfolium.controller.datasource

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.ensureExists
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.parseBigDecimal
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

/**
 * CSV-based price data source - uses the existing current_prices.csv file
 */
class CsvPriceDataSource(
    private val csvFile: File,
) : PriceDataSource {
    private val reader = csvReader { skipEmptyLine = true }
    private val pricesCache: Map<String, BigDecimal> by lazy { loadPrices() }

    private fun loadPrices(): Map<String, BigDecimal> {
        csvFile.ensureExists()
        val rows = reader.readAllWithHeader(csvFile)
        return rows
            .mapNotNull { r ->
                val ticker = r["ticker"]?.trim()
                val price = r["price"]?.trim()?.let { parseBigDecimal(it) }
                if (ticker != null && price != null) {
                    ticker to price
                } else {
                    null
                }
            }.toMap()
    }

    override fun getCurrentPrice(ticker: String): BigDecimal? = pricesCache[ticker]

    override fun getHistoricalPrice(
        ticker: String,
        date: LocalDate,
    ): BigDecimal? {
        // CSV doesn't support historical data, return current price as fallback
        return getCurrentPrice(ticker)
    }

    override fun getHistoricalPrices(
        ticker: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, BigDecimal> {
        // CSV doesn't support historical data, return empty map
        return emptyMap()
    }

    override fun getCurrentPrices(tickers: List<String>): Map<String, BigDecimal> =
        tickers
            .mapNotNull { ticker ->
                pricesCache[ticker]?.let { ticker to it }
            }.toMap()
}

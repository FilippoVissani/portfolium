package io.github.filippovissani.portfolium.controller.datasource

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Cached price data source that wraps another PriceDataSource implementation
 * Stores prices persistently in a CSV file to reduce API calls
 *
 * @param delegate The underlying price data source (e.g., YahooFinancePriceDataSource)
 * @param cacheFile The file where cached prices are stored
 * @param cacheDurationHours How long current prices are considered fresh (default: 24 hours)
 */
class CachedPriceDataSource(
    private val delegate: PriceDataSource,
    private val cacheFile: File = File("data/price_cache.csv"),
    private val cacheDurationHours: Long = 24,
) : PriceDataSource {
    companion object {
        private val logger = LoggerFactory.getLogger(CachedPriceDataSource::class.java)
        private const val CURRENT_PRICE_TYPE = "CURRENT"
        private const val HISTORICAL_PRICE_TYPE = "HISTORICAL"
    }

    private data class CacheEntry(
        val ticker: String,
        val type: String,
        val date: LocalDate?,
        val price: BigDecimal,
        val lastUpdated: LocalDateTime,
    )

    private val cache = mutableMapOf<String, CacheEntry>()
    private val lock = ReentrantReadWriteLock()

    init {
        ensureCacheDirectoryExists()
        loadCache()
    }

    override fun getCurrentPrice(ticker: String): BigDecimal? {
        // Check cache first
        val cachedPrice =
            lock.read {
                val cacheKey = currentPriceCacheKey(ticker)
                val entry = cache[cacheKey]

                // Check if we have a fresh cached price
                if (entry != null && isFresh(entry)) {
                    logger.debug("Cache hit for current price of {}", ticker)
                    entry.price
                } else {
                    null
                }
            }

        if (cachedPrice != null) {
            return cachedPrice
        }

        // Cache miss or stale - fetch from delegate
        logger.debug("Cache miss or stale for current price of {}, fetching from delegate", ticker)
        val price = delegate.getCurrentPrice(ticker)

        if (price != null) {
            lock.write {
                val entry =
                    CacheEntry(
                        ticker = ticker,
                        type = CURRENT_PRICE_TYPE,
                        date = null,
                        price = price,
                        lastUpdated = LocalDateTime.now(),
                    )
                cache[currentPriceCacheKey(ticker)] = entry
                saveCache()
            }
        }

        return price
    }

    override fun getHistoricalPrice(
        ticker: String,
        date: LocalDate,
    ): BigDecimal? {
        // Check cache first
        val cachedPrice =
            lock.read {
                val cacheKey = historicalPriceCacheKey(ticker, date)
                cache[cacheKey]?.price
            }

        if (cachedPrice != null) {
            logger.debug("Cache hit for historical price of {} on {}", ticker, date)
            return cachedPrice
        }

        // Cache miss - fetch from delegate
        logger.debug("Cache miss for historical price of {} on {}, fetching from delegate", ticker, date)
        val price = delegate.getHistoricalPrice(ticker, date)

        if (price != null) {
            lock.write {
                val entry =
                    CacheEntry(
                        ticker = ticker,
                        type = HISTORICAL_PRICE_TYPE,
                        date = date,
                        price = price,
                        lastUpdated = LocalDateTime.now(),
                    )
                cache[historicalPriceCacheKey(ticker, date)] = entry
                saveCache()
            }
        }

        return price
    }

    override fun getHistoricalPrices(
        ticker: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, BigDecimal> {
        val result = mutableMapOf<LocalDate, BigDecimal>()
        val missingDates = mutableListOf<LocalDate>()

        // Check cache for each date in range
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            lock.read {
                val cacheKey = historicalPriceCacheKey(ticker, currentDate)
                val entry = cache[cacheKey]

                if (entry != null) {
                    result[currentDate] = entry.price
                } else {
                    missingDates.add(currentDate)
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        // If we have all dates cached, return them
        if (missingDates.isEmpty()) {
            logger.debug("All historical prices for {} from {} to {} found in cache", ticker, startDate, endDate)
            return result
        }

        // Fetch missing dates from delegate
        logger.debug("Fetching {} missing historical prices for {} from delegate", missingDates.size, ticker)
        val fetchedPrices = delegate.getHistoricalPrices(ticker, startDate, endDate)

        // Update cache with fetched prices
        if (fetchedPrices.isNotEmpty()) {
            lock.write {
                fetchedPrices.forEach { (date, price) ->
                    val entry =
                        CacheEntry(
                            ticker = ticker,
                            type = HISTORICAL_PRICE_TYPE,
                            date = date,
                            price = price,
                            lastUpdated = LocalDateTime.now(),
                        )
                    cache[historicalPriceCacheKey(ticker, date)] = entry
                    result[date] = price
                }
                saveCache()
            }
        }

        return result
    }

    /**
     * Clear all cached data
     */
    fun clearCache() {
        lock.write {
            cache.clear()
            saveCache()
            logger.info("Cache cleared")
        }
    }

    /**
     * Clear cached data for a specific ticker
     */
    fun clearCache(ticker: String) {
        lock.write {
            cache.keys.removeIf { it.startsWith("$ticker|") }
            saveCache()
            logger.info("Cache cleared for ticker {}", ticker)
        }
    }

    /**
     * Get cache statistics
     */
    fun getCacheStats(): Map<String, Any> =
        lock.read {
            val currentPrices = cache.values.count { it.type == CURRENT_PRICE_TYPE }
            val historicalPrices = cache.values.count { it.type == HISTORICAL_PRICE_TYPE }
            val freshCurrentPrices = cache.values.count { it.type == CURRENT_PRICE_TYPE && isFresh(it) }

            mapOf(
                "totalEntries" to cache.size,
                "currentPrices" to currentPrices,
                "historicalPrices" to historicalPrices,
                "freshCurrentPrices" to freshCurrentPrices,
                "stalCurrentPrices" to (currentPrices - freshCurrentPrices),
            )
        }

    private fun ensureCacheDirectoryExists() {
        val directory = cacheFile.parentFile
        if (!directory.exists()) {
            directory.mkdirs()
            logger.info("Created cache directory: {}", directory.absolutePath)
        }
    }

    private fun loadCache() {
        if (!cacheFile.exists()) {
            logger.info("Cache file does not exist, starting with empty cache")
            return
        }

        try {
            val rows = csvReader().readAll(cacheFile)
            if (rows.isEmpty()) {
                logger.info("Cache file is empty")
                return
            }

            // Skip header row
            rows.drop(1).forEach { row ->
                try {
                    if (row.size >= 5) {
                        val ticker = row[0]
                        val type = row[1]
                        val dateStr = row[2]
                        val price = BigDecimal(row[3])
                        val lastUpdated = LocalDateTime.parse(row[4])

                        val date = if (dateStr.isNotEmpty()) LocalDate.parse(dateStr) else null
                        val entry = CacheEntry(ticker, type, date, price, lastUpdated)

                        val cacheKey =
                            if (type == CURRENT_PRICE_TYPE) {
                                currentPriceCacheKey(ticker)
                            } else {
                                historicalPriceCacheKey(ticker, date!!)
                            }

                        cache[cacheKey] = entry
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to parse cache row: {}", row, e)
                }
            }

            logger.info("Loaded {} entries from cache", cache.size)
        } catch (e: Exception) {
            logger.error("Failed to load cache from file", e)
        }
    }

    private fun saveCache() {
        try {
            csvWriter().open(cacheFile) {
                // Write header
                writeRow(listOf("ticker", "type", "date", "price", "lastUpdated"))

                // Write cache entries
                cache.values.forEach { entry ->
                    writeRow(
                        listOf(
                            entry.ticker,
                            entry.type,
                            entry.date?.toString() ?: "",
                            entry.price.toString(),
                            entry.lastUpdated.toString(),
                        ),
                    )
                }
            }
            logger.debug("Cache saved to file with {} entries", cache.size)
        } catch (e: Exception) {
            logger.error("Failed to save cache to file", e)
        }
    }

    private fun isFresh(entry: CacheEntry): Boolean {
        // Historical prices never expire
        if (entry.type == HISTORICAL_PRICE_TYPE) {
            return true
        }

        // Current prices expire after cacheDurationHours
        val age = java.time.Duration.between(entry.lastUpdated, LocalDateTime.now())
        return age.toHours() < cacheDurationHours
    }

    private fun currentPriceCacheKey(ticker: String): String = "$ticker|$CURRENT_PRICE_TYPE"

    private fun historicalPriceCacheKey(
        ticker: String,
        date: LocalDate,
    ): String = "$ticker|$HISTORICAL_PRICE_TYPE|$date"
}

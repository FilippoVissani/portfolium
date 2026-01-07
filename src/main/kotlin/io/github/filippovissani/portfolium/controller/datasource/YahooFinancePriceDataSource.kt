package io.github.filippovissani.portfolium.controller.datasource

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import com.google.gson.JsonParser

/**
 * Yahoo Finance API-based price data source
 * Uses the Yahoo Finance v8 API to fetch historical and current prices
 */
class YahooFinancePriceDataSource(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : PriceDataSource {

    companion object {
        private const val BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart"
        private val logger = LoggerFactory.getLogger(YahooFinancePriceDataSource::class.java)
    }

    override fun getCurrentPrice(ticker: String): BigDecimal? {
        return try {
            val url = "$BASE_URL/$ticker?interval=1d&range=1d"
            val response = makeRequest(url)
            parseCurrentPrice(response)
        } catch (e: Exception) {
            logger.error("Error fetching current price for ticker: {}", ticker, e)
            null
        }
    }

    override fun getHistoricalPrice(ticker: String, date: LocalDate): BigDecimal? {
        return try {
            // Get prices for the day and a few days before/after to handle weekends/holidays
            val startDate = date.minusDays(5)
            val endDate = date.plusDays(5)
            val prices = getHistoricalPrices(ticker, startDate, endDate)

            // Try to find the exact date first
            prices[date] ?:
            // If not found, get the closest date before the target date
            prices.filter { it.key <= date }
                .maxByOrNull { it.key }?.value
        } catch (e: Exception) {
            logger.error("Error fetching historical price for ticker: {} on date: {}", ticker, date, e)
            null
        }
    }

    override fun getHistoricalPrices(ticker: String, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, BigDecimal> {
        return try {
            val period1 = startDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond()
            val period2 = endDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond()
            val url = "$BASE_URL/$ticker?period1=$period1&period2=$period2&interval=1d"

            val response = makeRequest(url)
            parseHistoricalPrices(response)
        } catch (e: Exception) {
            logger.error("Error fetching historical prices for ticker: {}", ticker, e)
            emptyMap()
        }
    }

    private fun makeRequest(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException("HTTP request failed with status ${response.statusCode()}")
        }
        return response.body()
    }

    private fun parseCurrentPrice(jsonResponse: String): BigDecimal? {
        try {
            val root = JsonParser.parseString(jsonResponse).asJsonObject
            val chart = root.getAsJsonObject("chart")
            val result = chart.getAsJsonArray("result")?.get(0)?.asJsonObject ?: return null
            val meta = result.getAsJsonObject("meta")
            val regularPrice = meta.get("regularMarketPrice")?.asDouble ?: return null
            return BigDecimal.valueOf(regularPrice)
        } catch (e: Exception) {
            logger.error("Error parsing current price from JSON response", e)
            return null
        }
    }

    private fun parseHistoricalPrices(jsonResponse: String): Map<LocalDate, BigDecimal> {
        try {
            val root = JsonParser.parseString(jsonResponse).asJsonObject
            val chart = root.getAsJsonObject("chart")
            val result = chart.getAsJsonArray("result")?.get(0)?.asJsonObject ?: return emptyMap()

            val timestamps = result.getAsJsonArray("timestamp") ?: return emptyMap()
            val indicators = result.getAsJsonObject("indicators")
            val quote = indicators.getAsJsonArray("quote")?.get(0)?.asJsonObject ?: return emptyMap()
            val closes = quote.getAsJsonArray("close") ?: return emptyMap()

            val prices = mutableMapOf<LocalDate, BigDecimal>()
            for (i in 0 until timestamps.size()) {
                val timestamp = timestamps.get(i).asLong
                val closeElement = closes.get(i)

                if (!closeElement.isJsonNull) {
                    val close = closeElement.asDouble
                    val date = LocalDate.ofEpochDay(timestamp / 86400)
                    prices[date] = BigDecimal.valueOf(close)
                }
            }
            return prices
        } catch (e: Exception) {
            logger.error("Error parsing historical prices from JSON response", e)
            return emptyMap()
        }
    }
}


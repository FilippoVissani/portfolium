package io.github.filippovissani.portfolium.controller.datasource

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

class CachedPriceDataSourceTest :
    FunSpec({

        test("CachedPriceDataSource should cache current prices") {
            val cacheFile = File.createTempFile("price_cache_test", ".csv")
            cacheFile.deleteOnExit()

            var callCount = 0
            val mockDelegate =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal {
                        callCount++
                        return BigDecimal("100.50")
                    }

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal? = null

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val cachedSource = CachedPriceDataSource(mockDelegate, cacheFile, cacheDurationHours = 24)

            // First call should hit delegate
            val price1 = cachedSource.getCurrentPrice("AAPL")
            price1.shouldNotBeNull()
            price1 shouldBe BigDecimal("100.50")
            callCount shouldBe 1

            // Second call should use cache
            val price2 = cachedSource.getCurrentPrice("AAPL")
            price2.shouldNotBeNull()
            price2 shouldBe BigDecimal("100.50")
            callCount shouldBe 1 // Should still be 1 (not incremented)
        }

        test("CachedPriceDataSource should cache historical prices") {
            val cacheFile = File.createTempFile("price_cache_test", ".csv")
            cacheFile.deleteOnExit()

            var callCount = 0
            val testDate = LocalDate.of(2024, 1, 15)
            val mockDelegate =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal {
                        callCount++
                        return BigDecimal("150.25")
                    }

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val cachedSource = CachedPriceDataSource(mockDelegate, cacheFile)

            // First call should hit delegate
            val price1 = cachedSource.getHistoricalPrice("MSFT", testDate)
            price1.shouldNotBeNull()
            price1 shouldBe BigDecimal("150.25")
            callCount shouldBe 1

            // Second call should use cache
            val price2 = cachedSource.getHistoricalPrice("MSFT", testDate)
            price2.shouldNotBeNull()
            price2 shouldBe BigDecimal("150.25")
            callCount shouldBe 1 // Should still be 1 (not incremented)
        }

        test("CachedPriceDataSource should persist cache to file") {
            val cacheFile = File.createTempFile("price_cache_test", ".csv")
            cacheFile.deleteOnExit()

            val mockDelegate =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal = BigDecimal("200.00")

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal? = null

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            // Create cached source and fetch a price
            val cachedSource1 = CachedPriceDataSource(mockDelegate, cacheFile)
            cachedSource1.getCurrentPrice("GOOGL")

            // Create a new instance with same cache file
            var delegateCallCount = 0
            val mockDelegate2 =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal {
                        delegateCallCount++
                        return BigDecimal("200.00")
                    }

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal? = null

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> = emptyMap()
                }

            val cachedSource2 = CachedPriceDataSource(mockDelegate2, cacheFile)
            val price = cachedSource2.getCurrentPrice("GOOGL")

            // Should load from cache file, not call delegate
            price shouldBe BigDecimal("200.00")
            delegateCallCount shouldBe 0
        }

        test("CachedPriceDataSource should fetch multiple historical prices efficiently") {
            val cacheFile = File.createTempFile("price_cache_test", ".csv")
            cacheFile.deleteOnExit()

            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 1, 5)

            val mockPrices =
                mapOf(
                    LocalDate.of(2024, 1, 1) to BigDecimal("100.00"),
                    LocalDate.of(2024, 1, 2) to BigDecimal("101.00"),
                    LocalDate.of(2024, 1, 3) to BigDecimal("102.00"),
                    LocalDate.of(2024, 1, 4) to BigDecimal("103.00"),
                    LocalDate.of(2024, 1, 5) to BigDecimal("104.00"),
                )

            var callCount = 0
            val mockDelegate =
                object : PriceDataSource {
                    override fun getCurrentPrice(ticker: String): BigDecimal? = null

                    override fun getHistoricalPrice(
                        ticker: String,
                        date: LocalDate,
                    ): BigDecimal? = null

                    override fun getHistoricalPrices(
                        ticker: String,
                        startDate: LocalDate,
                        endDate: LocalDate,
                    ): Map<LocalDate, BigDecimal> {
                        callCount++
                        return mockPrices
                    }
                }

            val cachedSource = CachedPriceDataSource(mockDelegate, cacheFile)

            // First call should fetch from delegate
            val prices1 = cachedSource.getHistoricalPrices("TSLA", startDate, endDate)
            prices1.size shouldBe 5
            callCount shouldBe 1

            // Second call should use cache
            val prices2 = cachedSource.getHistoricalPrices("TSLA", startDate, endDate)
            prices2.size shouldBe 5
            callCount shouldBe 1 // Should not increment
        }
    })

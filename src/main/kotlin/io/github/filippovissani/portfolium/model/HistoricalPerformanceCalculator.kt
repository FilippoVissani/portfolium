package io.github.filippovissani.portfolium.model

import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.model.service.HistoricalPerformanceService
import java.time.LocalDate

/**
 * Backward compatibility wrapper - delegates to new service class
 * @deprecated Use HistoricalPerformanceService instead
 */
object HistoricalPerformanceCalculator {
    /**
     * Calculate historical performance of investments over a date range
     * @param transactions All investment transactions
     * @param priceSource Source for historical prices
     * @param startDate Start date for performance calculation
     * @param endDate End date for performance calculation (defaults to today)
     * @param intervalDays Interval in days between data points (defaults to 30 for monthly)
     */
    fun calculateHistoricalPerformance(
        transactions: List<InvestmentTransaction>,
        priceSource: PriceDataSource,
        startDate: LocalDate,
        endDate: LocalDate = LocalDate.now(),
        intervalDays: Long = 30,
    ): HistoricalPerformance =
        HistoricalPerformanceService.calculateHistoricalPerformance(
            transactions,
            priceSource,
            startDate,
            endDate,
            intervalDays,
        )
}

package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.model.domain.HistoricalPerformance
import io.github.filippovissani.portfolium.model.domain.InvestmentTransaction
import io.github.filippovissani.portfolium.model.domain.PerformanceDataPoint
import io.github.filippovissani.portfolium.model.util.times
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

/**
 * Service for calculating historical performance
 */
object HistoricalPerformanceService {
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
    ): HistoricalPerformance {
        val dataPoints = mutableListOf<PerformanceDataPoint>()

        // Generate dates at regular intervals
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val portfolioValue = calculatePortfolioValueAtDate(transactions, priceSource, currentDate)
            dataPoints.add(PerformanceDataPoint(currentDate, portfolioValue))
            currentDate = currentDate.plusDays(intervalDays)
        }

        // Add final date if not already included
        if (dataPoints.isEmpty() || dataPoints.last().date != endDate) {
            val finalValue = calculatePortfolioValueAtDate(transactions, priceSource, endDate)
            dataPoints.add(PerformanceDataPoint(endDate, finalValue))
        }

        // Calculate returns
        val initialValue = dataPoints.firstOrNull()?.value ?: BigDecimal.ZERO
        val finalValue = dataPoints.lastOrNull()?.value ?: BigDecimal.ZERO

        val totalReturn =
            if (initialValue.signum() == 0) {
                BigDecimal.ZERO
            } else {
                ((finalValue - initialValue) / initialValue * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
            }

        // Calculate annualized return
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
        val annualizedReturn =
            if (daysBetween > 0 && initialValue.signum() != 0) {
                val years = daysBetween.toBigDecimal().divide(BigDecimal(365.25), 6, RoundingMode.HALF_UP)
                if (years > BigDecimal.ZERO) {
                    val ratio = finalValue.divide(initialValue, 6, RoundingMode.HALF_UP)
                    // Convert to double for power calculation, then back to BigDecimal
                    val ratioDouble = ratio.toDouble()
                    val yearsDouble = years.toDouble()
                    val annualized = (ratioDouble.pow(1.0 / yearsDouble) - 1.0) * 100.0
                    BigDecimal.valueOf(annualized).setScale(2, RoundingMode.HALF_UP)
                } else {
                    null
                }
            } else {
                null
            }

        return HistoricalPerformance(
            dataPoints = dataPoints,
            totalReturn = totalReturn,
            annualizedReturn = annualizedReturn,
        )
    }

    /**
     * Calculate portfolio value at a specific date
     */
    private fun calculatePortfolioValueAtDate(
        transactions: List<InvestmentTransaction>,
        priceSource: PriceDataSource,
        date: LocalDate,
    ): BigDecimal {
        // Group transactions by ticker up to the given date
        data class Position(
            var quantity: BigDecimal,
        )

        val positions = mutableMapOf<String, Position>()

        transactions
            .filter { !it.date.isAfter(date) }
            .forEach { tx ->
                val position = positions.getOrPut(tx.ticker) { Position(BigDecimal.ZERO) }
                position.quantity += tx.quantity
            }

        // Calculate total value using historical prices
        var totalValue = BigDecimal.ZERO
        positions.forEach { (ticker, position) ->
            if (position.quantity.signum() != 0) {
                val price = priceSource.getHistoricalPrice(ticker, date) ?: BigDecimal.ZERO
                totalValue += position.quantity * price
            }
        }

        return totalValue.toMoney()
    }
}

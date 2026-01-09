package io.github.filippovissani.portfolium.model.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Historical performance data
 */
data class HistoricalPerformance(
    val dataPoints: List<PerformanceDataPoint>,
    val totalReturn: BigDecimal, // percentage
    val annualizedReturn: BigDecimal? = null, // percentage
)

/**
 * Performance data point at a specific date
 */
data class PerformanceDataPoint(val date: LocalDate, val value: BigDecimal)

package io.github.filippovissani.portfolium.view.utils

import io.github.filippovissani.portfolium.model.HistoricalPerformance
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility functions for formatting values and calculating display metrics
 */
object FormattingUtils {
    fun formatPercentage(value: BigDecimal, scale: Int = 1): String =
        "${(value * BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)}%"

    fun formatCurrency(value: BigDecimal): String = "€$value"

    fun getValueClass(value: BigDecimal, zeroIsPositive: Boolean = true): String = when {
        value > BigDecimal.ZERO -> "positive"
        value < BigDecimal.ZERO -> "negative"
        zeroIsPositive -> "positive"
        else -> "negative"
    }

    fun getReturnBadge(hp: HistoricalPerformance?): Pair<String, String>? {
        if (hp == null || hp.totalReturn == BigDecimal.ZERO) return null
        val returnClass = if (hp.totalReturn >= BigDecimal.ZERO) "positive" else "negative"
        return Pair("${hp.totalReturn}%", returnClass)
    }

    fun calculatePercentage(numerator: BigDecimal, denominator: BigDecimal, scale: Int = 1): String {
        if (denominator <= BigDecimal.ZERO) return "0%"
        return formatPercentage(numerator / denominator, scale)
    }
}

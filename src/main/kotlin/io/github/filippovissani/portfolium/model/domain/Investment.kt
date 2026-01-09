package io.github.filippovissani.portfolium.model.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Represents an investment in an ETF
 */
data class Investment(
    val etf: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal,
) {
    val investedValue: BigDecimal get() = quantity * averagePrice
    val currentValue: BigDecimal get() = quantity * currentPrice
    val pnl: BigDecimal get() = currentValue - investedValue
}

/**
 * Investment transaction for historical performance calculations
 */
data class InvestmentTransaction(
    val date: LocalDate,
    val etf: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?,
)

/**
 * Represents ETF holdings in an account
 */
data class EtfHolding(
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
)

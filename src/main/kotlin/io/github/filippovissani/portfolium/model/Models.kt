package io.github.filippovissani.portfolium.model

import io.github.filippovissani.portfolium.model.util.minus
import io.github.filippovissani.portfolium.model.util.times
import java.math.BigDecimal
import java.time.LocalDate

data class Transaction(
    val date: LocalDate,
    val description: String,
    val type: TransactionType,
    val category: String,
    val method: String,
    val amount: BigDecimal,
    val note: String?
)

enum class TransactionType { Income, Expense }


data class PlannedExpense(
    val name: String,
    val estimatedAmount: BigDecimal,
    val horizon: String?,
    val dueDate: LocalDate?,
    val accrued: BigDecimal,
) {
    val delta: BigDecimal get() = estimatedAmount - accrued
}


data class EmergencyFundConfig(
    val targetMonths: Int,
    val currentCapital: BigDecimal
)


// individual investment transaction (e.g., buy/sell)
// quantity can be negative for sells; price is per unit; fees are optional per transaction cost
// instrument identifiers: etf/ticker plus optional area
data class InvestmentTransaction(
    val date: LocalDate,
    val etf: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?
)


data class Investment(
    val etf: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal
) {
    val investedValue: BigDecimal get() = quantity * averagePrice
    val currentValue: BigDecimal get() = quantity * currentPrice
    val pnl: BigDecimal get() = currentValue - investedValue
}

// Dashboard aggregates

data class LiquiditySummary(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val net: BigDecimal,
    val avgMonthlyExpense12m: BigDecimal,
)


data class PlannedExpensesSummary(
    val totalEstimated: BigDecimal,
    val totalAccrued: BigDecimal,
    val coverageRatio: BigDecimal
)


data class EmergencyFundSummary(
    val targetCapital: BigDecimal,
    val currentCapital: BigDecimal,
    val deltaToTarget: BigDecimal,
    val status: String
)


data class InvestmentsSummary(
    val totalInvested: BigDecimal,
    val totalCurrent: BigDecimal,
    val itemsWithWeights: List<Pair<Investment, BigDecimal>> // weight 0..1
)


data class Portfolio(
    val liquidity: LiquiditySummary,
    val planned: PlannedExpensesSummary,
    val emergency: EmergencyFundSummary,
    val investments: InvestmentsSummary,
    val totalNetWorth: BigDecimal,
    val percentInvested: BigDecimal,
    val percentLiquid: BigDecimal
)

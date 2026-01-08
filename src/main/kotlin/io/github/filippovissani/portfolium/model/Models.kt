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


data class PlannedExpenseGoal(
    val name: String,
    val estimatedAmount: BigDecimal,
    val horizon: String?,
    val dueDate: LocalDate?,
    val instrument: String? = null // "liquid", "etf", "bond", etc. - null/empty means liquid
) {
    val isLiquid: Boolean get() = instrument.isNullOrBlank() || instrument.equals("liquid", ignoreCase = true)
}

data class PlannedExpense(
    val goal: PlannedExpenseGoal,
    val accrued: BigDecimal
) {
    val name: String get() = goal.name
    val estimatedAmount: BigDecimal get() = goal.estimatedAmount
    val horizon: String? get() = goal.horizon
    val dueDate: LocalDate? get() = goal.dueDate
    val instrument: String? get() = goal.instrument
    val delta: BigDecimal get() = estimatedAmount - accrued
    val isLiquid: Boolean get() = goal.isLiquid
}

data class PlannedExpenseTransaction(
    val date: LocalDate,
    val expenseName: String,
    val description: String,
    val amount: BigDecimal, // positive for deposits, negative for withdrawals
    val note: String?
)

data class EmergencyFundGoal(
    val targetMonths: Int,
    val instrument: String? = null // "liquid", "etf", "bond", etc. - null/empty means liquid
) {
    val isLiquid: Boolean get() = instrument.isNullOrBlank() || instrument.equals("liquid", ignoreCase = true)
}

data class EmergencyFundConfig(
    val goal: EmergencyFundGoal,
    val currentCapital: BigDecimal
) {
    val targetMonths: Int get() = goal.targetMonths
    val instrument: String? get() = goal.instrument
    val isLiquid: Boolean get() = goal.isLiquid
}

data class EmergencyFundTransaction(
    val date: LocalDate,
    val description: String,
    val amount: BigDecimal, // positive for deposits, negative for withdrawals
    val note: String?
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
    val coverageRatio: BigDecimal,
    val liquidAccrued: BigDecimal,
    val investedAccrued: BigDecimal
)


data class EmergencyFundSummary(
    val targetCapital: BigDecimal,
    val currentCapital: BigDecimal,
    val deltaToTarget: BigDecimal,
    val status: String,
    val isLiquid: Boolean
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
    val percentLiquid: BigDecimal,
    val historicalPerformance: HistoricalPerformance? = null
)

// Historical performance data
data class HistoricalPerformance(
    val dataPoints: List<PerformanceDataPoint>,
    val totalReturn: BigDecimal, // percentage
    val annualizedReturn: BigDecimal? = null // percentage
)

data class PerformanceDataPoint(
    val date: LocalDate,
    val value: BigDecimal
)

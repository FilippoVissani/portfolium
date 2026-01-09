package io.github.filippovissani.portfolium.model.domain

import java.math.BigDecimal

/**
 * Transaction statistics for liquidity summary
 */
data class TransactionStatistics(
    val totalByCategory: Map<String, BigDecimal>,
    val monthlyTrend: List<MonthlyDataPoint>,
    val topExpenseCategories: List<Pair<String, BigDecimal>>,
    val topIncomeCategories: List<Pair<String, BigDecimal>>,
)

/**
 * Monthly data point for trends
 */
data class MonthlyDataPoint(
    val yearMonth: String, // Format: YYYY-MM
    val income: BigDecimal,
    val expense: BigDecimal,
    val net: BigDecimal,
)

/**
 * Liquidity summary
 */
data class LiquiditySummary(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val net: BigDecimal,
    val avgMonthlyExpense12m: BigDecimal,
    val statistics: TransactionStatistics? = null,
)

/**
 * Planned expenses summary
 */
data class PlannedExpensesSummary(
    val totalEstimated: BigDecimal,
    val totalAccrued: BigDecimal,
    val coverageRatio: BigDecimal,
    val liquidAccrued: BigDecimal,
    val investedAccrued: BigDecimal,
    val isInvested: Boolean,
    val historicalPerformance: HistoricalPerformance? = null,
)

/**
 * Emergency fund summary
 */
data class EmergencyFundSummary(
    val targetCapital: BigDecimal,
    val currentCapital: BigDecimal,
    val deltaToTarget: BigDecimal,
    val status: String,
    val isLiquid: Boolean,
    val historicalPerformance: HistoricalPerformance? = null,
)

/**
 * Investments summary
 */
data class InvestmentsSummary(
    val totalInvested: BigDecimal,
    val totalCurrent: BigDecimal,
    val itemsWithWeights: List<Pair<Investment, BigDecimal>>, // weight 0..1
)

/**
 * Complete portfolio
 */
data class Portfolio(
    val liquidity: LiquiditySummary,
    val planned: PlannedExpensesSummary,
    val emergency: EmergencyFundSummary,
    val investments: InvestmentsSummary,
    val totalNetWorth: BigDecimal,
    val percentInvested: BigDecimal,
    val percentLiquid: BigDecimal,
    val historicalPerformance: HistoricalPerformance? = null,
    val overallHistoricalPerformance: HistoricalPerformance? = null,
)

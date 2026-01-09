package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.github.filippovissani.portfolium.model.domain.EtfSellTransaction
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesSummary
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for planned expenses calculations
 */
object PlannedExpensesService {
    /**
     * Calculate planned expenses summary
     */
    fun calculatePlannedExpensesSummary(
        account: PlannedExpensesBankAccount,
        currentPrices: Map<String, BigDecimal> = emptyMap(),
    ): PlannedExpensesSummary {
        val totalEstimated = account.plannedExpenses.sumOf { it.estimatedAmount }

        // Calculate the current value of ETF holdings
        val investedAccrued =
            account.etfHoldings.entries.sumOf { (ticker, holding) ->
                val currentPrice = currentPrices[ticker] ?: holding.averagePrice
                holding.quantity * currentPrice
            }

        // Liquid is the cash balance
        val liquidAccrued = account.currentBalance

        // Total accrued is the sum of invested and liquid
        val totalAccrued = investedAccrued + liquidAccrued

        // Check if account has ETF transactions (invested)
        val hasEtfTransactions = account.transactions.any { it is EtfBuyTransaction || it is EtfSellTransaction }

        val coverage =
            if (totalEstimated.signum() == 0) {
                BigDecimal.ZERO
            } else {
                totalAccrued.divide(
                    totalEstimated,
                    4,
                    RoundingMode.HALF_UP,
                )
            }
        return PlannedExpensesSummary(
            totalEstimated = totalEstimated.toMoney(),
            totalAccrued = totalAccrued.toMoney(),
            coverageRatio = coverage,
            liquidAccrued = liquidAccrued.toMoney(),
            investedAccrued = investedAccrued.toMoney(),
            isInvested = hasEtfTransactions,
            historicalPerformance = null, // Will be set by Controller if needed
        )
    }
}

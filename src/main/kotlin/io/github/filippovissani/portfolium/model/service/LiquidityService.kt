package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.LiquiditySummary
import io.github.filippovissani.portfolium.model.domain.MainBankAccount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Service for liquidity calculations
 */
object LiquidityService {
    /**
     * Calculate liquidity summary from main bank account
     */
    fun calculateLiquiditySummary(
        account: MainBankAccount,
        today: LocalDate = LocalDate.now(),
    ): LiquiditySummary {
        val totalIncome = account.totalIncome
        val totalExpense = account.totalExpenses
        val net = account.currentBalance

        // Average monthly expense over last 12 months (absolute)
        val start = today.minusMonths(12)
        val last12 = account.transactions.filter { it.amount < BigDecimal.ZERO && it.date.isAfter(start.minusDays(1)) }
        val spent12 = last12.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount.abs() }
        val avgMonthly12 =
            if (spent12 == BigDecimal.ZERO) BigDecimal.ZERO else spent12.divide(BigDecimal(12), 2, RoundingMode.HALF_UP)

        // Calculate transaction statistics
        val statistics = StatisticsService.calculateTransactionStatistics(account.transactions)

        return LiquiditySummary(
            totalIncome = totalIncome.toMoney(),
            totalExpense = totalExpense.toMoney(),
            net = net.toMoney(),
            avgMonthlyExpense12m = avgMonthly12.toMoney(),
            statistics = statistics,
        )
    }
}

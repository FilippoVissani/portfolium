package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.LiquidTransaction
import io.github.filippovissani.portfolium.model.domain.MonthlyDataPoint
import io.github.filippovissani.portfolium.model.domain.TransactionStatistics
import java.math.BigDecimal

/**
 * Service for calculating transaction statistics
 */
object StatisticsService {
    /**
     * Calculate detailed transaction statistics
     */
    fun calculateTransactionStatistics(transactions: List<LiquidTransaction>): TransactionStatistics {
        // Total by category
        val totalByCategory =
            transactions
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount.abs() }.toMoney() }

        // Monthly trend
        val monthlyTrend =
            transactions
                .groupBy { "${it.date.year}-${String.format("%02d", it.date.monthValue)}" }
                .map { (yearMonth, txs) ->
                    val income = txs.filter { it.amount > BigDecimal.ZERO }.sumOf { it.amount }
                    val expense = txs.filter { it.amount < BigDecimal.ZERO }.sumOf { it.amount.abs() }
                    MonthlyDataPoint(
                        yearMonth = yearMonth,
                        income = income.toMoney(),
                        expense = expense.toMoney(),
                        net = (income - expense).toMoney(),
                    )
                }.sortedBy { it.yearMonth }

        // Top expense categories (excluding income)
        val topExpenseCategories =
            transactions
                .filter { it.amount < BigDecimal.ZERO }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount.abs() }.toMoney() }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

        // Top income categories
        val topIncomeCategories =
            transactions
                .filter { it.amount > BigDecimal.ZERO }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toMoney() }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

        return TransactionStatistics(
            totalByCategory = totalByCategory,
            monthlyTrend = monthlyTrend,
            topExpenseCategories = topExpenseCategories,
            topIncomeCategories = topIncomeCategories,
        )
    }
}

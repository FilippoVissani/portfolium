package io.github.filippovissani.portfolium.model

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.util.minus
import io.github.filippovissani.portfolium.model.util.plus
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

object Calculators {
    // Liquidity summary from MainBankAccount
    fun summarizeLiquidity(
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
        val statistics = calculateTransactionStatistics(account.transactions)

        return LiquiditySummary(
            totalIncome = totalIncome.toMoney(),
            totalExpense = totalExpense.toMoney(),
            net = net.toMoney(),
            avgMonthlyExpense12m = avgMonthly12.toMoney(),
            statistics = statistics,
        )
    }

    private fun calculateTransactionStatistics(transactions: List<LiquidTransaction>): TransactionStatistics {
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

    // Planned expenses summary from PlannedExpensesBankAccount
    fun summarizePlanned(
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

    // Emergency fund summary from EmergencyFundBankAccount
    fun summarizeEmergency(
        account: EmergencyFundBankAccount,
        avgMonthlyExpense: BigDecimal,
    ): EmergencyFundSummary {
        val targetCapital = avgMonthlyExpense.multiply(BigDecimal(account.targetMonthlyExpenses))
        val currentCapital = account.currentBalance
        val delta = targetCapital - currentCapital
        val status = if (currentCapital >= targetCapital) "OK" else "BELOW TARGET"

        // Check if account has ETF transactions (invested)
        val hasEtfTransactions = account.transactions.any { it is EtfBuyTransaction || it is EtfSellTransaction }
        val isLiquid = !hasEtfTransactions

        return EmergencyFundSummary(
            targetCapital = targetCapital.toMoney(),
            currentCapital = currentCapital.toMoney(),
            deltaToTarget = delta.toMoney(),
            status = status,
            isLiquid = isLiquid,
            historicalPerformance = null, // Will be set by Controller if needed
        )
    }

    // Investment summary from list of Investment objects
    fun summarizeInvestments(items: List<Investment>): InvestmentsSummary {
        val totalCurrent = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.currentValue }
        val weights =
            items.map { i ->
                val w =
                    if (totalCurrent.signum() == 0) {
                        BigDecimal.ZERO
                    } else {
                        i.currentValue.divide(
                            totalCurrent,
                            6,
                            RoundingMode.HALF_UP,
                        )
                    }
                i to w
            }
        val totalInvested = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.investedValue }
        return InvestmentsSummary(
            totalInvested = totalInvested.toMoney(),
            totalCurrent = totalCurrent.toMoney(),
            itemsWithWeights = weights,
        )
    }

    // Investment summary from InvestmentBankAccount
    fun summarizeInvestments(
        account: InvestmentBankAccount,
        currentPricesByTicker: Map<String, BigDecimal>,
    ): InvestmentsSummary {
        val items =
            account.etfHoldings.values.map { holding ->
                val currentPrice = currentPricesByTicker[holding.ticker] ?: BigDecimal.ZERO
                Investment(
                    etf = holding.name,
                    ticker = holding.ticker,
                    area = holding.area,
                    quantity = holding.quantity,
                    averagePrice = holding.averagePrice,
                    currentPrice = currentPrice,
                )
            }
        return summarizeInvestments(items)
    }

    // Build complete portfolio from all summaries
    fun buildPortfolio(
        liquidity: LiquiditySummary,
        planned: PlannedExpensesSummary,
        emergency: EmergencyFundSummary,
        investments: InvestmentsSummary,
        historicalPerformance: HistoricalPerformance? = null,
        overallHistoricalPerformance: HistoricalPerformance? = null,
    ): Portfolio {
        // Liquid capital includes: net liquidity, liquid planned accrued, and emergency fund if liquid
        val liquidCapital =
            liquidity.net + planned.liquidAccrued +
                if (emergency.isLiquid) emergency.currentCapital else BigDecimal.ZERO

        // Invested capital includes: investments, invested planned accrued, and emergency fund if invested
        val investedCapital =
            investments.totalCurrent + planned.investedAccrued +
                if (!emergency.isLiquid) emergency.currentCapital else BigDecimal.ZERO

        val totalNetWorth = (liquidCapital + investedCapital).toMoney()
        val percentInvested =
            if (totalNetWorth.signum() == 0) {
                BigDecimal.ZERO
            } else {
                investedCapital.divide(
                    totalNetWorth,
                    4,
                    RoundingMode.HALF_UP,
                )
            }
        val percentLiquid = BigDecimal.ONE - percentInvested

        return Portfolio(
            liquidity = liquidity,
            planned = planned,
            emergency = emergency,
            investments = investments,
            totalNetWorth = totalNetWorth,
            percentInvested = percentInvested,
            percentLiquid = percentLiquid,
            historicalPerformance = historicalPerformance,
            overallHistoricalPerformance = overallHistoricalPerformance,
        )
    }
}

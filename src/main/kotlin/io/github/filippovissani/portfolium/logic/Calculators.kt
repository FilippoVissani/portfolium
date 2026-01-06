package io.github.filippovissani.portfolium.logic

import io.github.filippovissani.portfolium.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.Dashboard
import io.github.filippovissani.portfolium.model.EmergencyFundConfig
import io.github.filippovissani.portfolium.model.EmergencyFundSummary
import io.github.filippovissani.portfolium.model.Investment
import io.github.filippovissani.portfolium.model.InvestmentTransaction
import io.github.filippovissani.portfolium.model.InvestmentsSummary
import io.github.filippovissani.portfolium.model.LiquiditySummary
import io.github.filippovissani.portfolium.model.PlannedExpense
import io.github.filippovissani.portfolium.model.PlannedExpensesSummary
import io.github.filippovissani.portfolium.model.Transaction
import io.github.filippovissani.portfolium.model.TransactionType
import io.github.filippovissani.portfolium.util.minus
import io.github.filippovissani.portfolium.util.plus
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

object Calculators {
    fun summarizeLiquidity(transactions: List<Transaction>, today: LocalDate = LocalDate.now()): LiquiditySummary {
        val totalIncome = transactions.filter { it.type == TransactionType.Income }
            .fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }
        val totalExpenseAbs = transactions.filter { it.type == TransactionType.Expense }
            .fold(BigDecimal.ZERO) { acc, t -> acc + t.amount.abs() }
        val net = transactions.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }

        // Average monthly expense over last 12 months (absolute)
        val start = today.minusMonths(12)
        val last12 = transactions.filter { it.type == TransactionType.Expense && it.date.isAfter(start.minusDays(1)) }
        val spent12 = last12.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount.abs() }
        val avgMonthly12 =
            if (spent12 == BigDecimal.ZERO) BigDecimal.ZERO else spent12.divide(BigDecimal(12), 2, RoundingMode.HALF_UP)

        return LiquiditySummary(
            totalIncome = totalIncome.toMoney(),
            totalExpense = totalExpenseAbs.toMoney(),
            net = net.toMoney(),
            avgMonthlyExpense12m = avgMonthly12.toMoney()
        )
    }

    fun summarizePlanned(items: List<PlannedExpense>): PlannedExpensesSummary {
        val totalEstimated = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.estimatedAmount }
        val totalAccrued = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.accrued }
        val coverage = if (totalEstimated.signum() == 0) BigDecimal.ZERO else totalAccrued.divide(
            totalEstimated,
            4,
            RoundingMode.HALF_UP
        )
        return PlannedExpensesSummary(
            totalEstimated = totalEstimated.toMoney(),
            totalAccrued = totalAccrued.toMoney(),
            coverageRatio = coverage
        )
    }

    fun summarizeEmergency(config: EmergencyFundConfig, avgMonthlyExpense: BigDecimal): EmergencyFundSummary {
        val targetCapital = avgMonthlyExpense.multiply(BigDecimal(config.targetMonths))
        val delta = targetCapital - config.currentCapital
        val status = if (config.currentCapital >= targetCapital) "OK" else "BELOW TARGET"
        return EmergencyFundSummary(
            targetCapital = targetCapital.toMoney(),
            currentCapital = config.currentCapital.toMoney(),
            deltaToTarget = delta.toMoney(),
            status = status
        )
    }

    fun summarizeInvestments(items: List<Investment>): InvestmentsSummary {
        val totalCurrent = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.currentValue }
        val weights = items.map { i ->
            val w = if (totalCurrent.signum() == 0) BigDecimal.ZERO else i.currentValue.divide(
                totalCurrent,
                6,
                RoundingMode.HALF_UP
            )
            i to w
        }
        val totalInvested = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.investedValue }
        return InvestmentsSummary(
            totalInvested = totalInvested.toMoney(),
            totalCurrent = totalCurrent.toMoney(),
            itemsWithWeights = weights
        )
    }

    // New: summarize investments from individual transactions and a map of current prices per ticker
    fun summarizeInvestmentsFromTransactions(
        txs: List<InvestmentTransaction>,
        currentPricesByTicker: Map<String, BigDecimal>
    ): InvestmentsSummary {
        // Aggregate by (ticker) while preserving etf and area from latest or first occurrence
        data class Agg(var etf: String, var area: String?, var qty: BigDecimal, var cost: BigDecimal)
        val byTicker = mutableMapOf<String, Agg>()
        txs.forEach { t ->
            val fees = t.fees ?: BigDecimal.ZERO
            val cost = t.price.multiply(t.quantity).plus(fees) // cost increases with buys; if quantity negative, reduces
            val agg = byTicker.getOrPut(t.ticker) { Agg(t.etf, t.area, BigDecimal.ZERO, BigDecimal.ZERO) }
            agg.qty += t.quantity
            agg.cost += cost
            // prefer last seen metadata
            agg.etf = t.etf
            agg.area = t.area
        }
        val items = byTicker.mapNotNull { (ticker, a) ->
            if (a.qty.signum() == 0) {
                // fully sold position; skip
                null
            } else {
                val avgPrice = if (a.qty.signum() == 0) BigDecimal.ZERO else a.cost.divide(a.qty, 6, RoundingMode.HALF_UP)
                val currentPrice = currentPricesByTicker[ticker] ?: BigDecimal.ZERO
                Investment(etf = a.etf, ticker = ticker, area = a.area, quantity = a.qty, averagePrice = avgPrice, currentPrice = currentPrice)
            }
        }
        return summarizeInvestments(items)
    }

    fun buildDashboard(
        liquidity: LiquiditySummary,
        planned: PlannedExpensesSummary,
        emergency: EmergencyFundSummary,
        investments: InvestmentsSummary
    ): Dashboard {
        val liquidCapital = liquidity.net + planned.totalAccrued + emergency.currentCapital
        val totalNetWorth = (liquidCapital + investments.totalCurrent).toMoney()
        val percentInvested = if (totalNetWorth.signum() == 0) BigDecimal.ZERO else investments.totalCurrent.divide(
            totalNetWorth,
            4,
            RoundingMode.HALF_UP
        )
        val percentLiquid = BigDecimal.ONE - percentInvested

        return Dashboard(
            liquidity = liquidity,
            planned = planned,
            emergency = emergency,
            investments = investments,
            totalNetWorth = totalNetWorth,
            percentInvested = percentInvested,
            percentLiquid = percentLiquid
        )
    }
}

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
    val instrument: String? = null // "liquid", "etf", "bond", etc. - null/empty means liquid
) {
    val delta: BigDecimal get() = estimatedAmount - accrued
    val isLiquid: Boolean get() = instrument.isNullOrBlank() || instrument.equals("liquid", ignoreCase = true)
}


data class EmergencyFundConfig(
    val targetMonths: Int,
    val currentCapital: BigDecimal,
    val instrument: String? = null // "liquid", "etf", "bond", etc. - null/empty means liquid
) {
    val isLiquid: Boolean get() = instrument.isNullOrBlank() || instrument.equals("liquid", ignoreCase = true)
}


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

// Bank account models

sealed class BankAccountTransaction {
    abstract val date: LocalDate
}

data class DepositTransaction(
    override val date: LocalDate,
    val amount: BigDecimal,
    val description: String? = null
) : BankAccountTransaction()

data class WithdrawalTransaction(
    override val date: LocalDate,
    val amount: BigDecimal,
    val description: String? = null
) : BankAccountTransaction()

data class EtfBuyTransaction(
    override val date: LocalDate,
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?
) : BankAccountTransaction()

data class EtfSellTransaction(
    override val date: LocalDate,
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?
) : BankAccountTransaction()

data class BankAccount(
    val name: String,
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val transactions: List<BankAccountTransaction> = emptyList()
) {
    val currentBalance: BigDecimal
        get() {
            var balance = initialBalance
            transactions.forEach { transaction ->
                when (transaction) {
                    is DepositTransaction -> balance += transaction.amount
                    is WithdrawalTransaction -> balance -= transaction.amount
                    is EtfBuyTransaction -> {
                        val totalCost = (transaction.price * transaction.quantity) + (transaction.fees ?: BigDecimal.ZERO)
                        balance -= totalCost
                    }
                    is EtfSellTransaction -> {
                        val totalProceeds = (transaction.price * transaction.quantity) - (transaction.fees ?: BigDecimal.ZERO)
                        balance += totalProceeds
                    }
                }
            }
            return balance
        }

    val etfHoldings: Map<String, EtfHolding>
        get() {
            val holdings = mutableMapOf<String, MutableList<EtfTransaction>>()

            transactions.forEach { transaction ->
                when (transaction) {
                    is EtfBuyTransaction -> {
                        val key = transaction.ticker
                        holdings.getOrPut(key) { mutableListOf() }
                            .add(EtfTransaction(transaction.date, transaction.quantity, transaction.price, transaction.fees, transaction.name, transaction.area))
                    }
                    is EtfSellTransaction -> {
                        val key = transaction.ticker
                        holdings.getOrPut(key) { mutableListOf() }
                            .add(EtfTransaction(transaction.date, -transaction.quantity, transaction.price, transaction.fees, transaction.name, transaction.area))
                    }
                    else -> {}
                }
            }

            return holdings.mapValues { (ticker, txs) ->
                val totalQuantity = txs.sumOf { it.quantity }
                val totalCost = txs.filter { it.quantity > BigDecimal.ZERO }
                    .sumOf { (it.quantity * it.price) + (it.fees ?: BigDecimal.ZERO) }
                val averagePrice = if (totalQuantity > BigDecimal.ZERO) {
                    totalCost / totalQuantity
                } else {
                    BigDecimal.ZERO
                }
                val name = txs.firstOrNull()?.name ?: ticker
                val area = txs.firstOrNull()?.area

                EtfHolding(
                    name = name,
                    ticker = ticker,
                    area = area,
                    quantity = totalQuantity,
                    averagePrice = averagePrice
                )
            }.filterValues { it.quantity > BigDecimal.ZERO }
        }

    private data class EtfTransaction(
        val date: LocalDate,
        val quantity: BigDecimal,
        val price: BigDecimal,
        val fees: BigDecimal?,
        val name: String,
        val area: String?
    )
}

data class EtfHolding(
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal
)


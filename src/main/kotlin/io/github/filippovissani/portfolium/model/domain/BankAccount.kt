package io.github.filippovissani.portfolium.model.domain

import io.github.filippovissani.portfolium.model.util.minus
import io.github.filippovissani.portfolium.model.util.plus
import io.github.filippovissani.portfolium.model.util.times
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Main bank account for day-to-day transactions
 */
data class MainBankAccount(
    val name: String = "Main Account",
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val transactions: List<LiquidTransaction> = emptyList(),
) {
    val currentBalance: BigDecimal
        get() = initialBalance + transactions.sumOf { it.amount }

    val totalIncome: BigDecimal
        get() = transactions.filter { it.amount > BigDecimal.ZERO }.sumOf { it.amount }

    val totalExpenses: BigDecimal
        get() = transactions.filter { it.amount < BigDecimal.ZERO }.sumOf { it.amount.abs() }
}

/**
 * Planned expense entry
 */
data class PlannedExpenseEntry(
    val name: String,
    val expirationDate: LocalDate?,
    val estimatedAmount: BigDecimal,
)

/**
 * Bank account for planned expenses
 */
data class PlannedExpensesBankAccount(
    val name: String = "Planned Expenses",
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val transactions: List<BankAccountTransaction> = emptyList(),
    val plannedExpenses: List<PlannedExpenseEntry> = emptyList(),
) {
    val currentBalance: BigDecimal
        get() = BankAccountHelper.calculateBalance(initialBalance, transactions)

    val etfHoldings: Map<String, EtfHolding>
        get() = BankAccountHelper.calculateEtfHoldings(transactions)
}

/**
 * Bank account for emergency fund
 */
data class EmergencyFundBankAccount(
    val name: String = "Emergency Fund",
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val transactions: List<BankAccountTransaction> = emptyList(),
    val targetMonthlyExpenses: Int = 6,
) {
    val currentBalance: BigDecimal
        get() {
            var balance = initialBalance
            transactions.forEach { transaction ->
                when (transaction) {
                    is DepositTransaction -> balance += transaction.amount
                    is WithdrawalTransaction -> balance -= transaction.amount
                    else -> {}
                }
            }
            return balance
        }
}

/**
 * Bank account for investments
 */
data class InvestmentBankAccount(
    val name: String = "Investments",
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val transactions: List<BankAccountTransaction> = emptyList(),
) {
    val currentBalance: BigDecimal
        get() = BankAccountHelper.calculateBalance(initialBalance, transactions)

    val etfHoldings: Map<String, EtfHolding>
        get() = BankAccountHelper.calculateEtfHoldings(transactions)
}

/**
 * Helper class for ETF transaction processing
 */
private data class EtfTransaction(
    val date: LocalDate,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?,
    val name: String,
    val area: String?,
)

/**
 * Helper object for bank account operations
 */
object BankAccountHelper {
    /**
     * Calculate balance from transactions
     */
    fun calculateBalance(
        initialBalance: BigDecimal,
        transactions: List<BankAccountTransaction>,
    ): BigDecimal {
        var balance = initialBalance
        transactions.forEach { transaction ->
            when (transaction) {
                is DepositTransaction -> balance += transaction.amount
                is WithdrawalTransaction -> balance -= transaction.amount
                is EtfBuyTransaction -> {
                    val totalCost =
                        (transaction.price * transaction.quantity) + (transaction.fees ?: BigDecimal.ZERO)
                    balance -= totalCost
                }
                is EtfSellTransaction -> {
                    val totalProceeds =
                        (transaction.price * transaction.quantity) - (transaction.fees ?: BigDecimal.ZERO)
                    balance += totalProceeds
                }
                else -> {}
            }
        }
        return balance
    }

    /**
     * Calculate ETF holdings from transactions
     */
    fun calculateEtfHoldings(transactions: List<BankAccountTransaction>): Map<String, EtfHolding> {
        val holdings = mutableMapOf<String, MutableList<EtfTransaction>>()

        transactions.forEach { transaction ->
            when (transaction) {
                is EtfBuyTransaction -> {
                    val key = transaction.ticker
                    holdings
                        .getOrPut(key) { mutableListOf() }
                        .add(
                            EtfTransaction(
                                transaction.date,
                                transaction.quantity,
                                transaction.price,
                                transaction.fees,
                                transaction.name,
                                transaction.area,
                            ),
                        )
                }
                is EtfSellTransaction -> {
                    val key = transaction.ticker
                    holdings
                        .getOrPut(key) { mutableListOf() }
                        .add(
                            EtfTransaction(
                                transaction.date,
                                -transaction.quantity,
                                transaction.price,
                                transaction.fees,
                                transaction.name,
                                transaction.area,
                            ),
                        )
                }
                else -> {}
            }
        }

        return holdings
            .mapValues { (ticker, txs) ->
                val totalQuantity = txs.sumOf { it.quantity }
                val totalCost =
                    txs
                        .filter { it.quantity > BigDecimal.ZERO }
                        .sumOf { (it.quantity * it.price) + (it.fees ?: BigDecimal.ZERO) }
                val averagePrice =
                    if (totalQuantity > BigDecimal.ZERO) {
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
                    averagePrice = averagePrice,
                )
            }.filterValues { it.quantity > BigDecimal.ZERO }
    }
}

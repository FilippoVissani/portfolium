package io.github.filippovissani.portfolium.model

import java.math.BigDecimal

/**
 * Service for managing bank account operations and calculations
 */
object BankAccountService {

    /**
     * Calculate the total deposited amount in the bank account
     */
    fun getTotalDeposits(account: BankAccount): BigDecimal {
        return account.transactions
            .filterIsInstance<DepositTransaction>()
            .sumOf { it.amount }
    }

    /**
     * Calculate the total withdrawn amount from the bank account
     */
    fun getTotalWithdrawals(account: BankAccount): BigDecimal {
        return account.transactions
            .filterIsInstance<WithdrawalTransaction>()
            .sumOf { it.amount }
    }

    /**
     * Calculate the total amount invested in ETFs
     */
    fun getTotalInvestedInEtfs(account: BankAccount): BigDecimal {
        return account.transactions.sumOf { transaction ->
            when (transaction) {
                is EtfBuyTransaction -> {
                    (transaction.price * transaction.quantity) + (transaction.fees ?: BigDecimal.ZERO)
                }
                is EtfSellTransaction -> {
                    -((transaction.price * transaction.quantity) - (transaction.fees ?: BigDecimal.ZERO))
                }
                else -> BigDecimal.ZERO
            }
        }
    }

    /**
     * Get summary of all ETF transactions grouped by ticker
     */
    fun getEtfTransactionsSummary(account: BankAccount): Map<String, EtfTransactionSummary> {
        val summaryMap = mutableMapOf<String, MutableEtfSummary>()

        account.transactions.forEach { transaction ->
            when (transaction) {
                is EtfBuyTransaction -> {
                    val summary = summaryMap.getOrPut(transaction.ticker) {
                        MutableEtfSummary(transaction.name, transaction.ticker, transaction.area)
                    }
                    summary.totalBought += transaction.quantity
                    summary.totalInvested += (transaction.price * transaction.quantity) + (transaction.fees ?: BigDecimal.ZERO)
                }
                is EtfSellTransaction -> {
                    val summary = summaryMap.getOrPut(transaction.ticker) {
                        MutableEtfSummary(transaction.name, transaction.ticker, transaction.area)
                    }
                    summary.totalSold += transaction.quantity
                    summary.totalRealized += (transaction.price * transaction.quantity) - (transaction.fees ?: BigDecimal.ZERO)
                }
                else -> {}
            }
        }

        return summaryMap.mapValues { (_, summary) ->
            EtfTransactionSummary(
                name = summary.name,
                ticker = summary.ticker,
                area = summary.area,
                totalBought = summary.totalBought,
                totalSold = summary.totalSold,
                netQuantity = summary.totalBought - summary.totalSold,
                totalInvested = summary.totalInvested,
                totalRealized = summary.totalRealized
            )
        }
    }

    /**
     * Get a summary of the bank account
     */
    fun getAccountSummary(account: BankAccount): BankAccountSummary {
        return BankAccountSummary(
            name = account.name,
            initialBalance = account.initialBalance,
            currentBalance = account.currentBalance,
            totalDeposits = getTotalDeposits(account),
            totalWithdrawals = getTotalWithdrawals(account),
            totalInvestedInEtfs = getTotalInvestedInEtfs(account),
            etfHoldings = account.etfHoldings,
            numberOfTransactions = account.transactions.size
        )
    }

    private data class MutableEtfSummary(
        val name: String,
        val ticker: String,
        val area: String?,
        var totalBought: BigDecimal = BigDecimal.ZERO,
        var totalSold: BigDecimal = BigDecimal.ZERO,
        var totalInvested: BigDecimal = BigDecimal.ZERO,
        var totalRealized: BigDecimal = BigDecimal.ZERO
    )
}

data class EtfTransactionSummary(
    val name: String,
    val ticker: String,
    val area: String?,
    val totalBought: BigDecimal,
    val totalSold: BigDecimal,
    val netQuantity: BigDecimal,
    val totalInvested: BigDecimal,
    val totalRealized: BigDecimal
)

data class BankAccountSummary(
    val name: String,
    val initialBalance: BigDecimal,
    val currentBalance: BigDecimal,
    val totalDeposits: BigDecimal,
    val totalWithdrawals: BigDecimal,
    val totalInvestedInEtfs: BigDecimal,
    val etfHoldings: Map<String, EtfHolding>,
    val numberOfTransactions: Int
)


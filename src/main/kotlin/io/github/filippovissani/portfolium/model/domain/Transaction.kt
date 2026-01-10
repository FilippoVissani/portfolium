package io.github.filippovissani.portfolium.model.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Base class for all bank account transactions
 */
sealed class BankAccountTransaction {
    abstract val date: LocalDate
}

/**
 * Liquid money transaction for main bank account
 */
data class LiquidTransaction(
    override val date: LocalDate,
    val description: String,
    val category: String,
    val amount: BigDecimal,
    val note: String? = null,
) : BankAccountTransaction()

/**
 * Generic deposit for specialized accounts
 */
data class DepositTransaction(
    override val date: LocalDate,
    val amount: BigDecimal,
    val description: String? = null,
) : BankAccountTransaction()

/**
 * Generic withdrawal for specialized accounts
 */
data class WithdrawalTransaction(
    override val date: LocalDate,
    val amount: BigDecimal,
    val description: String? = null,
) : BankAccountTransaction()

/**
 * ETF buy transaction for investment accounts
 */
data class EtfBuyTransaction(
    override val date: LocalDate,
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?,
    val description: String? = null,
) : BankAccountTransaction()

/**
 * ETF sell transaction for investment accounts
 */
data class EtfSellTransaction(
    override val date: LocalDate,
    val name: String,
    val ticker: String,
    val area: String?,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fees: BigDecimal?,
    val description: String? = null,
) : BankAccountTransaction()

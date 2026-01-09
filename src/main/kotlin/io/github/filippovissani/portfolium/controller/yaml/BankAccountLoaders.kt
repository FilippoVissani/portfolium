package io.github.filippovissani.portfolium.controller.yaml

import io.github.filippovissani.portfolium.model.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Loaders for specialized bank account types
 */
object BankAccountLoaders {
    private val yaml = Yaml()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Load main bank account from YAML
     * Expected format:
     * name: "Main Account"
     * initialBalance: 10000.00
     * transactions:
     *   - date: 2025-01-01
     *     description: "Salary"
     *     category: "Income"
     *     amount: 5000.00
     *     note: "Monthly salary"
     */
    fun loadMainBankAccount(file: File): MainBankAccount {
        if (!file.exists()) {
            return MainBankAccount()
        }

        val raw = file.inputStream().use { stream ->
            yaml.loadAs(stream, MainBankAccountYaml::class.java) ?: MainBankAccountYaml()
        }

        val name = raw.name ?: "Main Account"
        val initialBalance = raw.initialBalance ?: BigDecimal.ZERO
        val transactions = (raw.transactions ?: emptyList()).mapNotNull { tx ->
            val date = parseDate(tx.date) ?: return@mapNotNull null
            LiquidTransaction(
                date = date,
                description = tx.description.orEmpty(),
                category = tx.category.orEmpty(),
                amount = tx.amount ?: BigDecimal.ZERO,
                note = tx.note
            )
        }

        return MainBankAccount(name, initialBalance, transactions)
    }

    /**
     * Load planned expenses bank account from YAML
     * Expected format:
     * name: "Planned Expenses"
     * initialBalance: 0.00
     * transactions:
     *   - type: deposit
     *     date: 2025-01-01
     *     amount: 1000.00
     *     description: "Initial deposit"
     * plannedExpenses:
     *   - name: "House Down Payment"
     *     expirationDate: 2026-12-31
     *     estimatedAmount: 50000.00
     */
    fun loadPlannedExpensesBankAccount(file: File): PlannedExpensesBankAccount {
        if (!file.exists()) {
            return PlannedExpensesBankAccount()
        }

        val raw = file.inputStream().use { stream ->
            yaml.loadAs(stream, PlannedExpensesBankAccountYaml::class.java) ?: PlannedExpensesBankAccountYaml()
        }

        val name = raw.name ?: "Planned Expenses"
        val initialBalance = raw.initialBalance ?: BigDecimal.ZERO

        val transactions = (raw.transactions ?: emptyList()).mapNotNull { parseGenericTransaction(it) }

        val plannedExpenses = (raw.plannedExpenses ?: emptyList()).mapNotNull { pe ->
            val namePe = pe.name ?: return@mapNotNull null
            PlannedExpenseEntry(
                name = namePe,
                expirationDate = parseDate(pe.expirationDate),
                estimatedAmount = pe.estimatedAmount ?: BigDecimal.ZERO
            )
        }

        return PlannedExpensesBankAccount(name, initialBalance, transactions, plannedExpenses)
    }

    /**
     * Load emergency fund bank account from YAML
     * Expected format:
     * name: "Emergency Fund"
     * initialBalance: 0.00
     * targetMonthlyExpenses: 6
     * transactions:
     *   - type: deposit
     *     date: 2025-01-01
     *     amount: 5000.00
     *     description: "Initial deposit"
     */
    fun loadEmergencyFundBankAccount(file: File): EmergencyFundBankAccount {
        if (!file.exists()) {
            return EmergencyFundBankAccount()
        }

        val raw = file.inputStream().use { stream ->
            yaml.loadAs(stream, EmergencyFundBankAccountYaml::class.java) ?: EmergencyFundBankAccountYaml()
        }

        val name = raw.name ?: "Emergency Fund"
        val initialBalance = raw.initialBalance ?: BigDecimal.ZERO
        val targetMonthlyExpenses = raw.targetMonthlyExpenses ?: 6

        val transactions = (raw.transactions ?: emptyList()).mapNotNull { parseGenericTransaction(it) }

        return EmergencyFundBankAccount(name, initialBalance, transactions, targetMonthlyExpenses)
    }

    /**
     * Load investment bank account from YAML
     * Expected format:
     * name: "Investments"
     * initialBalance: 0.00
     * transactions:
     *   - type: deposit
     *     date: 2025-01-01
     *     amount: 10000.00
     *   - type: etf_buy
     *     date: 2025-01-10
     *     name: "Vanguard S&P 500"
     *     ticker: "VOO"
     *     area: "US"
     *     quantity: 10
     *     price: 450.00
     *     fees: 5.00
     */
    fun loadInvestmentBankAccount(file: File): InvestmentBankAccount {
        if (!file.exists()) {
            return InvestmentBankAccount()
        }

        val raw = file.inputStream().use { stream ->
            yaml.loadAs(stream, InvestmentBankAccountYaml::class.java) ?: InvestmentBankAccountYaml()
        }

        val name = raw.name ?: "Investments"
        val initialBalance = raw.initialBalance ?: BigDecimal.ZERO

        val transactions = (raw.transactions ?: emptyList()).mapNotNull { parseInvestmentTransaction(it) }

        return InvestmentBankAccount(name, initialBalance, transactions)
    }

    // Mapping helpers from YAML DTOs to domain transactions

    private fun parseGenericTransaction(tx: GenericTransactionYaml): BankAccountTransaction? {
        val type = tx.type?.lowercase() ?: return null
        val date = parseDate(tx.date) ?: return null

        return when (type) {
            "deposit" -> DepositTransaction(
                date = date,
                amount = tx.amount ?: BigDecimal.ZERO,
                description = tx.description
            )
            "withdrawal" -> WithdrawalTransaction(
                date = date,
                amount = tx.amount ?: BigDecimal.ZERO,
                description = tx.description
            )
            else -> null
        }
    }

    private fun parseInvestmentTransaction(tx: InvestmentTransactionYaml): BankAccountTransaction? {
        val type = tx.type?.lowercase() ?: return null
        val date = parseDate(tx.date) ?: return null

        return when (type) {
            "deposit" -> DepositTransaction(
                date = date,
                amount = tx.amount ?: BigDecimal.ZERO,
                description = tx.description
            )
            "withdrawal" -> WithdrawalTransaction(
                date = date,
                amount = tx.amount ?: BigDecimal.ZERO,
                description = tx.description
            )
            "etf_buy", "buy_etf" -> EtfBuyTransaction(
                date = date,
                name = tx.name.orEmpty(),
                ticker = tx.ticker.orEmpty(),
                area = tx.area,
                quantity = tx.quantity ?: BigDecimal.ZERO,
                price = tx.price ?: BigDecimal.ZERO,
                fees = tx.fees
            )
            "etf_sell", "sell_etf" -> EtfSellTransaction(
                date = date,
                name = tx.name.orEmpty(),
                ticker = tx.ticker.orEmpty(),
                area = tx.area,
                quantity = tx.quantity ?: BigDecimal.ZERO,
                price = tx.price ?: BigDecimal.ZERO,
                fees = tx.fees
            )
            else -> null
        }
    }

    private fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateStr, dateFormatter)
        } catch (_: Exception) {
            null
        }
    }
}

// YAML DTOs (public, JavaBean-style) for SnakeYAML binding
class MainBankAccountYaml {
    var name: String? = null
    var initialBalance: BigDecimal? = null
    var transactions: List<LiquidTransactionYaml>? = null
}

class LiquidTransactionYaml {
    var date: String? = null
    var description: String? = null
    var category: String? = null
    var amount: BigDecimal? = null
    var note: String? = null
}

class PlannedExpensesBankAccountYaml {
    var name: String? = null
    var initialBalance: BigDecimal? = null
    var transactions: List<GenericTransactionYaml>? = null
    var plannedExpenses: List<PlannedExpenseEntryYaml>? = null
}

class PlannedExpenseEntryYaml {
    var name: String? = null
    var expirationDate: String? = null
    var estimatedAmount: BigDecimal? = null
}

class EmergencyFundBankAccountYaml {
    var name: String? = null
    var initialBalance: BigDecimal? = null
    var targetMonthlyExpenses: Int? = null
    var transactions: List<GenericTransactionYaml>? = null
}

class InvestmentBankAccountYaml {
    var name: String? = null
    var initialBalance: BigDecimal? = null
    var transactions: List<InvestmentTransactionYaml>? = null
}

class GenericTransactionYaml {
    var type: String? = null
    var date: String? = null
    var amount: BigDecimal? = null
    var description: String? = null
    var ticker: String? = null
    var area: String? = null
    var quantity: Int? = null
    var price: BigDecimal? = null
    var fees: BigDecimal? = null
    var name: String? = null
}

class InvestmentTransactionYaml {
    var type: String? = null
    var date: String? = null
    var name: String? = null
    var ticker: String? = null
    var area: String? = null
    var quantity: BigDecimal? = null
    var price: BigDecimal? = null
    var fees: BigDecimal? = null
    var amount: BigDecimal? = null // for deposit/withdrawal reuse
    var description: String? = null // for deposit/withdrawal reuse
}

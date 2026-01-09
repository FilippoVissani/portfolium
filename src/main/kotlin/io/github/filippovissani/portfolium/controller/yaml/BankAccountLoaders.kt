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

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Any>(file.inputStream()) as? Map<String, Any> ?: emptyMap()

        val name = data["name"]?.toString() ?: "Main Account"
        val initialBalance = parseBigDecimal(data["initialBalance"])

        @Suppress("UNCHECKED_CAST")
        val transactionsList = data["transactions"] as? List<*> ?: emptyList<Any>()

        val transactions = transactionsList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val txData = item as? Map<String, Any> ?: return@mapNotNull null
            parseLiquidTransaction(txData)
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

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Any>(file.inputStream()) as? Map<String, Any> ?: emptyMap()

        val name = data["name"]?.toString() ?: "Planned Expenses"
        val initialBalance = parseBigDecimal(data["initialBalance"])

        @Suppress("UNCHECKED_CAST")
        val transactionsList = data["transactions"] as? List<*> ?: emptyList<Any>()
        val transactions = transactionsList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val txData = item as? Map<String, Any> ?: return@mapNotNull null
            parseGenericTransaction(txData)
        }

        @Suppress("UNCHECKED_CAST")
        val plannedExpensesList = data["plannedExpenses"] as? List<*> ?: emptyList<Any>()
        val plannedExpenses = plannedExpensesList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val peData = item as? Map<String, Any> ?: return@mapNotNull null
            PlannedExpenseEntry(
                name = peData["name"]?.toString() ?: "",
                expirationDate = parseDate(peData["expirationDate"]?.toString()),
                estimatedAmount = parseBigDecimal(peData["estimatedAmount"])
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

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Any>(file.inputStream()) as? Map<String, Any> ?: emptyMap()

        val name = data["name"]?.toString() ?: "Emergency Fund"
        val initialBalance = parseBigDecimal(data["initialBalance"])
        val targetMonthlyExpenses = (data["targetMonthlyExpenses"] as? Number)?.toInt() ?: 6

        @Suppress("UNCHECKED_CAST")
        val transactionsList = data["transactions"] as? List<*> ?: emptyList<Any>()
        val transactions = transactionsList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val txData = item as? Map<String, Any> ?: return@mapNotNull null
            parseGenericTransaction(txData)
        }

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

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Any>(file.inputStream()) as? Map<String, Any> ?: emptyMap()

        val name = data["name"]?.toString() ?: "Investments"
        val initialBalance = parseBigDecimal(data["initialBalance"])

        @Suppress("UNCHECKED_CAST")
        val transactionsList = data["transactions"] as? List<*> ?: emptyList<Any>()
        val transactions = transactionsList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val txData = item as? Map<String, Any> ?: return@mapNotNull null
            parseInvestmentTransaction(txData)
        }

        return InvestmentBankAccount(name, initialBalance, transactions)
    }

    private fun parseLiquidTransaction(txData: Map<String, Any>): LiquidTransaction? {
        val date = parseDate(txData["date"]?.toString()) ?: return null
        return LiquidTransaction(
            date = date,
            description = txData["description"]?.toString() ?: "",
            category = txData["category"]?.toString() ?: "",
            amount = parseBigDecimal(txData["amount"]),
            note = txData["note"]?.toString()
        )
    }

    private fun parseGenericTransaction(txData: Map<String, Any>): BankAccountTransaction? {
        val type = txData["type"]?.toString()?.lowercase() ?: return null
        val date = parseDate(txData["date"]?.toString()) ?: return null

        return when (type) {
            "deposit" -> DepositTransaction(
                date = date,
                amount = parseBigDecimal(txData["amount"]),
                description = txData["description"]?.toString()
            )
            "withdrawal" -> WithdrawalTransaction(
                date = date,
                amount = parseBigDecimal(txData["amount"]),
                description = txData["description"]?.toString()
            )
            else -> null
        }
    }

    private fun parseInvestmentTransaction(txData: Map<String, Any>): BankAccountTransaction? {
        val type = txData["type"]?.toString()?.lowercase() ?: return null
        val date = parseDate(txData["date"]?.toString()) ?: return null

        return when (type) {
            "deposit" -> DepositTransaction(
                date = date,
                amount = parseBigDecimal(txData["amount"]),
                description = txData["description"]?.toString()
            )
            "withdrawal" -> WithdrawalTransaction(
                date = date,
                amount = parseBigDecimal(txData["amount"]),
                description = txData["description"]?.toString()
            )
            "etf_buy", "buy_etf" -> EtfBuyTransaction(
                date = date,
                name = txData["name"]?.toString() ?: "",
                ticker = txData["ticker"]?.toString() ?: "",
                area = txData["area"]?.toString(),
                quantity = parseBigDecimal(txData["quantity"]),
                price = parseBigDecimal(txData["price"]),
                fees = txData["fees"]?.let { parseBigDecimal(it) }
            )
            "etf_sell", "sell_etf" -> EtfSellTransaction(
                date = date,
                name = txData["name"]?.toString() ?: "",
                ticker = txData["ticker"]?.toString() ?: "",
                area = txData["area"]?.toString(),
                quantity = parseBigDecimal(txData["quantity"]),
                price = parseBigDecimal(txData["price"]),
                fees = txData["fees"]?.let { parseBigDecimal(it) }
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

    private fun parseBigDecimal(value: Any?): BigDecimal {
        return when (value) {
            is Number -> BigDecimal(value.toString())
            is String -> value.toBigDecimalOrNull() ?: BigDecimal.ZERO
            else -> BigDecimal.ZERO
        }
    }
}


package io.github.filippovissani.portfolium.controller.yaml

import io.github.filippovissani.portfolium.model.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BankAccountLoader {
    private val yaml = Yaml()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun loadBankAccount(file: File): BankAccount {
        if (!file.exists()) {
            throw IllegalArgumentException("Bank account YAML file not found: ${file.path}")
        }

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Any>(file.inputStream()) as? Map<String, Any> ?: emptyMap()

        val name = data["name"]?.toString() ?: "Unnamed Account"
        val initialBalance = parseBigDecimal(data["initialBalance"])

        @Suppress("UNCHECKED_CAST")
        val transactionsList = data["transactions"] as? List<*> ?: emptyList<Any>()

        val transactions = transactionsList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val txData = item as? Map<String, Any> ?: emptyMap()
            parseTransaction(txData)
        }

        return BankAccount(name, initialBalance, transactions)
    }

    private fun parseTransaction(txData: Map<String, Any>): BankAccountTransaction? {
        val type = txData["type"]?.toString() ?: return null
        val date = parseDate(txData["date"]?.toString()) ?: return null

        return when (type.lowercase()) {
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


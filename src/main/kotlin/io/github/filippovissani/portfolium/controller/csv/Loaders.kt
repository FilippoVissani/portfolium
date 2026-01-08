package io.github.filippovissani.portfolium.controller.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.ensureExists
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.parseBigDecimal
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.parseDate
import io.github.filippovissani.portfolium.model.*
import java.io.File
import java.time.LocalDate

class Loaders(
    private val reader: CsvReader = csvReader { skipEmptyLine = true }
) {
    // Transactions CSV header: date,description,type,category,method,amount,note
    fun loadTransactions(file: File): List<Transaction> {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        return rows.mapNotNull { r ->
            val typeStr = r["type"]?.trim()?.lowercase()
            val type = when (typeStr) {
                "income" -> TransactionType.Income
                "expense" -> TransactionType.Expense
                else -> null
            } ?: return@mapNotNull null

            Transaction(
                date = parseDate(r["date"]) ?: LocalDate.MIN,
                description = r["description"].orEmpty(),
                type = type,
                category = r["category"].orEmpty(),
                method = r["method"].orEmpty(),
                amount = parseBigDecimal(r["amount"]),
                note = r["note"]
            )
        }
    }

    // Planned expense transactions CSV header: date,expense_name,description,amount,note
    fun loadPlannedExpenseTransactions(file: File): List<PlannedExpenseTransaction> {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        return rows.map { r ->
            PlannedExpenseTransaction(
                date = parseDate(r["date"]) ?: LocalDate.MIN,
                expenseName = r["expense_name"].orEmpty(),
                description = r["description"].orEmpty(),
                amount = parseBigDecimal(r["amount"]),
                note = r["note"]
            )
        }
    }

    // Emergency fund transactions CSV header: date,description,amount,note
    fun loadEmergencyFundTransactions(file: File): List<EmergencyFundTransaction> {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        return rows.map { r ->
            EmergencyFundTransaction(
                date = parseDate(r["date"]) ?: LocalDate.MIN,
                description = r["description"].orEmpty(),
                amount = parseBigDecimal(r["amount"]),
                note = r["note"]
            )
        }
    }

    // Investment transactions CSV header: date,etf,ticker,area,quantity,price,fees
    fun loadInvestmentTransactions(file: File): List<InvestmentTransaction> {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        return rows.map { r ->
            InvestmentTransaction(
                date = parseDate(r["date"]) ?: LocalDate.MIN,
                etf = r["etf"].orEmpty(),
                ticker = r["ticker"].orEmpty(),
                area = r["area"],
                quantity = parseBigDecimal(r["quantity"]),
                price = parseBigDecimal(r["price"]),
                fees = r["fees"]?.let { parseBigDecimal(it) }
            )
        }
    }
}



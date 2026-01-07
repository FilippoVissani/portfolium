package io.github.filippovissani.portfolium.controller.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.ensureExists
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.parseBigDecimal
import io.github.filippovissani.portfolium.controller.csv.CsvUtils.parseDate
import io.github.filippovissani.portfolium.model.EmergencyFundConfig
import io.github.filippovissani.portfolium.model.InvestmentTransaction
import io.github.filippovissani.portfolium.model.PlannedExpense
import io.github.filippovissani.portfolium.model.Transaction
import io.github.filippovissani.portfolium.model.TransactionType
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

    // Planned expenses CSV header: name,estimated_amount,horizon,due_date,accrued,instrument
    fun loadPlannedExpenses(file: File): List<PlannedExpense> {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        return rows.map { r ->
            PlannedExpense(
                name = r["name"].orEmpty(),
                estimatedAmount = parseBigDecimal(r["estimated_amount"]),
                horizon = r["horizon"],
                dueDate = parseDate(r["due_date"]),
                accrued = parseBigDecimal(r["accrued"]),
                instrument = r["instrument"]
            )
        }
    }

    // Emergency fund config CSV header (single row is enough): target_months,current_capital,instrument
    fun loadEmergencyFund(file: File): EmergencyFundConfig {
        file.ensureExists()
        val rows = reader.readAllWithHeader(file)
        val r = rows.firstOrNull() ?: error("Emergency fund CSV is empty: ${file.path}")
        return EmergencyFundConfig(
            targetMonths = r["target_months"]?.trim()?.toIntOrNull() ?: 6,
            currentCapital = parseBigDecimal(r["current_capital"]),
            instrument = r["instrument"]
        )
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

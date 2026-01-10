package io.github.filippovissani.portfolium.view.data

import com.google.gson.Gson
import io.github.filippovissani.portfolium.model.HistoricalPerformance
import io.github.filippovissani.portfolium.model.Portfolio

/**
 * Handles serialization of portfolio data to JSON for client-side consumption
 */
object DataSerializer {
    private val gson = Gson()

    fun serializeHistoricalPerformance(hp: HistoricalPerformance?): String {
        if (hp == null) return "null"

        val dataPoints = hp.dataPoints.map { dp ->
            mapOf("date" to dp.date.toString(), "value" to dp.value)
        }

        return gson.toJson(
            mapOf(
                "dataPoints" to dataPoints,
                "totalReturn" to hp.totalReturn,
                "annualizedReturn" to hp.annualizedReturn,
            ),
        )
    }

    fun serializeLiquidityStatistics(portfolio: Portfolio): String {
        val stats = portfolio.liquidity.statistics ?: return "null"

        return gson.toJson(
            mapOf(
                "monthlyTrend" to stats.monthlyTrend.map {
                    mapOf(
                        "yearMonth" to it.yearMonth,
                        "income" to it.income,
                        "expense" to it.expense,
                        "net" to it.net,
                    )
                },
                "topExpenseCategories" to stats.topExpenseCategories.map {
                    listOf(it.first, it.second)
                },
                "topIncomeCategories" to stats.topIncomeCategories.map {
                    listOf(it.first, it.second)
                },
                "totalByCategory" to stats.totalByCategory,
            ),
        )
    }

    fun serializeInvestmentItems(portfolio: Portfolio): String {
        if (portfolio.investments.itemsWithWeights.isEmpty()) return "[]"

        val items = portfolio.investments.itemsWithWeights.map { (inv, weight) ->
            mapOf(
                "ticker" to inv.ticker,
                "etf" to inv.etf,
                "currentValue" to inv.currentValue,
                "weight" to weight,
            )
        }

        return gson.toJson(items)
    }
}

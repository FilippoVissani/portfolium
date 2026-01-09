package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.Investment
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.github.filippovissani.portfolium.model.domain.InvestmentsSummary
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for investment calculations
 */
object InvestmentService {
    /**
     * Calculate investments summary from a list of investments
     */
    fun calculateInvestmentsSummary(items: List<Investment>): InvestmentsSummary {
        val totalCurrent = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.currentValue }
        val weights =
            items.map { i ->
                val w =
                    if (totalCurrent.signum() == 0) {
                        BigDecimal.ZERO
                    } else {
                        i.currentValue.divide(
                            totalCurrent,
                            6,
                            RoundingMode.HALF_UP,
                        )
                    }
                i to w
            }
        val totalInvested = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.investedValue }
        return InvestmentsSummary(
            totalInvested = totalInvested.toMoney(),
            totalCurrent = totalCurrent.toMoney(),
            itemsWithWeights = weights,
        )
    }

    /**
     * Calculate investments summary from investment bank account
     */
    fun calculateInvestmentsSummary(
        account: InvestmentBankAccount,
        currentPricesByTicker: Map<String, BigDecimal>,
    ): InvestmentsSummary {
        val items =
            account.etfHoldings.values.map { holding ->
                val currentPrice = currentPricesByTicker[holding.ticker] ?: BigDecimal.ZERO
                Investment(
                    etf = holding.name,
                    ticker = holding.ticker,
                    area = holding.area,
                    quantity = holding.quantity,
                    averagePrice = holding.averagePrice,
                    currentPrice = currentPrice,
                )
            }
        return calculateInvestmentsSummary(items)
    }
}

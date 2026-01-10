package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.EmergencyFundSummary
import io.github.filippovissani.portfolium.model.domain.HistoricalPerformance
import io.github.filippovissani.portfolium.model.domain.InvestmentsSummary
import io.github.filippovissani.portfolium.model.domain.LiquiditySummary
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesSummary
import io.github.filippovissani.portfolium.model.domain.Portfolio
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for building portfolio summaries
 */
object PortfolioService {
    /**
     * Build complete portfolio from all summaries
     */
    fun buildPortfolio(
        liquidity: LiquiditySummary,
        planned: PlannedExpensesSummary,
        emergency: EmergencyFundSummary,
        investments: InvestmentsSummary,
        historicalPerformance: HistoricalPerformance? = null,
        overallHistoricalPerformance: HistoricalPerformance? = null,
    ): Portfolio {
        // Liquid capital includes: net liquidity, liquid planned accrued, and emergency fund if liquid
        val liquidCapital =
            liquidity.net + planned.liquidAccrued +
                if (emergency.isLiquid) emergency.currentCapital else BigDecimal.ZERO

        // Invested capital includes: investments, invested planned accrued, and emergency fund if invested
        val investedCapital =
            investments.totalCurrent + planned.investedAccrued +
                if (!emergency.isLiquid) emergency.currentCapital else BigDecimal.ZERO

        val totalNetWorth = (liquidCapital + investedCapital).toMoney()
        val percentInvested =
            if (totalNetWorth.signum() == 0) {
                BigDecimal.ZERO
            } else {
                investedCapital.divide(
                    totalNetWorth,
                    4,
                    RoundingMode.HALF_UP,
                )
            }
        val percentLiquid = BigDecimal.ONE - percentInvested

        return Portfolio(
            liquidity = liquidity,
            planned = planned,
            emergency = emergency,
            investments = investments,
            totalNetWorth = totalNetWorth,
            percentInvested = percentInvested,
            percentLiquid = percentLiquid,
            historicalPerformance = historicalPerformance,
            overallHistoricalPerformance = overallHistoricalPerformance,
        )
    }
}

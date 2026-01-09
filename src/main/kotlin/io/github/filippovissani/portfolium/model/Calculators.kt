package io.github.filippovissani.portfolium.model

import io.github.filippovissani.portfolium.model.service.EmergencyFundService
import io.github.filippovissani.portfolium.model.service.InvestmentService
import io.github.filippovissani.portfolium.model.service.LiquidityService
import io.github.filippovissani.portfolium.model.service.PlannedExpensesService
import io.github.filippovissani.portfolium.model.service.PortfolioService
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Backward compatibility wrapper - delegates to new service classes
 * @deprecated Use specific service classes instead
 */
object Calculators {
    // Liquidity summary from MainBankAccount
    fun summarizeLiquidity(
        account: MainBankAccount,
        today: LocalDate = LocalDate.now(),
    ): LiquiditySummary = LiquidityService.calculateLiquiditySummary(account, today)

    // Planned expenses summary from PlannedExpensesBankAccount
    fun summarizePlanned(
        account: PlannedExpensesBankAccount,
        currentPrices: Map<String, BigDecimal> = emptyMap(),
    ): PlannedExpensesSummary = PlannedExpensesService.calculatePlannedExpensesSummary(account, currentPrices)

    // Emergency fund summary from EmergencyFundBankAccount
    fun summarizeEmergency(
        account: EmergencyFundBankAccount,
        avgMonthlyExpense: BigDecimal,
    ): EmergencyFundSummary = EmergencyFundService.calculateEmergencyFundSummary(account, avgMonthlyExpense)

    // Investment summary from list of Investment objects
    fun summarizeInvestments(items: List<Investment>): InvestmentsSummary = InvestmentService.calculateInvestmentsSummary(items)

    // Investment summary from InvestmentBankAccount
    fun summarizeInvestments(
        account: InvestmentBankAccount,
        currentPricesByTicker: Map<String, BigDecimal>,
    ): InvestmentsSummary = InvestmentService.calculateInvestmentsSummary(account, currentPricesByTicker)

    // Build complete portfolio from all summaries
    fun buildPortfolio(
        liquidity: LiquiditySummary,
        planned: PlannedExpensesSummary,
        emergency: EmergencyFundSummary,
        investments: InvestmentsSummary,
        historicalPerformance: HistoricalPerformance? = null,
        overallHistoricalPerformance: HistoricalPerformance? = null,
    ): Portfolio =
        PortfolioService.buildPortfolio(
            liquidity,
            planned,
            emergency,
            investments,
            historicalPerformance,
            overallHistoricalPerformance,
        )
}

package io.github.filippovissani.portfolium.view.html

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.data.DataSerializer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatPercentage

/**
 * Generates JavaScript data initialization for charts
 */
object JavaScriptDataGenerator {
    fun generatePortfolioData(portfolio: Portfolio): String {
        return """
            const portfolioData = {
                percentLiquid: ${formatPercentage(portfolio.percentLiquid, 2).removeSuffix("%")},
                percentInvested: ${formatPercentage(portfolio.percentInvested, 2).removeSuffix("%")},
                liquidity: {
                    net: ${portfolio.liquidity.net},
                    totalIncome: ${portfolio.liquidity.totalIncome},
                    totalExpense: ${portfolio.liquidity.totalExpense},
                    avgMonthlyExpense12m: ${portfolio.liquidity.avgMonthlyExpense12m},
                    statistics: ${DataSerializer.serializeLiquidityStatistics(portfolio)}
                },
                emergency: {
                    currentCapital: ${portfolio.emergency.currentCapital},
                    targetCapital: ${portfolio.emergency.targetCapital},
                    deltaToTarget: ${portfolio.emergency.deltaToTarget},
                    status: "${portfolio.emergency.status}",
                    isLiquid: ${portfolio.emergency.isLiquid},
                    historicalPerformance: ${DataSerializer.serializeHistoricalPerformance(
            portfolio.emergency.historicalPerformance
        )}
                },
                investments: {
                    totalCurrent: ${portfolio.investments.totalCurrent},
                    totalInvested: ${portfolio.investments.totalInvested},
                    itemsWithWeights: ${DataSerializer.serializeInvestmentItems(portfolio)}
                },
                planned: {
                    totalEstimated: ${portfolio.planned.totalEstimated},
                    totalAccrued: ${portfolio.planned.totalAccrued},
                    coverageRatio: ${portfolio.planned.coverageRatio},
                    liquidAccrued: ${portfolio.planned.liquidAccrued},
                    investedAccrued: ${portfolio.planned.investedAccrued},
                    isInvested: ${portfolio.planned.isInvested},
                    historicalPerformance: ${DataSerializer.serializeHistoricalPerformance(
            portfolio.planned.historicalPerformance
        )}
                },
                historicalPerformance: ${DataSerializer.serializeHistoricalPerformance(
            portfolio.historicalPerformance
        )},
                overallHistoricalPerformance: ${DataSerializer.serializeHistoricalPerformance(
            portfolio.overallHistoricalPerformance
        )}
            };
        """.trimIndent()
    }
}

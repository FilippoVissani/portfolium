package io.github.filippovissani.portfolium.view.sections

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.components.CardComponent.metricCard
import io.github.filippovissani.portfolium.view.components.ChartComponent.chartContainer
import io.github.filippovissani.portfolium.view.components.ChartComponent.simpleChartContainer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.calculatePercentage
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatCurrency
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatPercentage
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getReturnBadge
import kotlinx.html.*

/**
 * Renders the Planned Expenses section
 */
object PlannedExpensesSection {
    fun DIV.render(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-calendar-check"></i>""") }
                    +" Planned Expenses"
                }
            }

            div(classes = "section-cards") {
                metricCard(
                    icon = "fas fa-piggy-bank",
                    iconColor = "primary",
                    title = "Accrued Capital",
                    value = formatCurrency(portfolio.planned.totalAccrued)
                )
                metricCard(
                    icon = "fas fa-bullseye",
                    iconColor = "warning",
                    title = "Target Capital",
                    value = formatCurrency(portfolio.planned.totalEstimated)
                )
                metricCard(
                    icon = "fas fa-percentage",
                    iconColor = "info",
                    title = "Coverage",
                    value = formatPercentage(portfolio.planned.coverageRatio)
                )
                metricCard(
                    icon = "fas fa-chart-line",
                    iconColor = "success",
                    title = "Invested",
                    value = calculatePercentage(
                        portfolio.planned.investedAccrued,
                        portfolio.planned.totalAccrued
                    )
                )
                metricCard(
                    icon = "fas fa-water",
                    iconColor = "primary",
                    title = "Liquid",
                    value = calculatePercentage(
                        portfolio.planned.liquidAccrued,
                        portfolio.planned.totalAccrued
                    )
                )
            }

            // Charts
            div(classes = "charts-grid") {
                simpleChartContainer(
                    chartId = "plannedExpensesCoverageChart",
                    title = "Coverage Progress",
                    icon = "fas fa-chart-bar"
                )

                if (portfolio.planned.isInvested && portfolio.planned.historicalPerformance != null) {
                    val badge = getReturnBadge(portfolio.planned.historicalPerformance)
                    chartContainer(
                        chartId = "plannedExpensesHistoricalChart",
                        title = "Historical Performance",
                        icon = "fas fa-chart-area",
                        badge = badge?.first,
                        badgeClass = badge?.second ?: ""
                    )
                }
            }
        }
    }
}

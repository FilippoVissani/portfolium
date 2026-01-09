package io.github.filippovissani.portfolium.view.sections

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.components.CardComponent.metricCard
import io.github.filippovissani.portfolium.view.components.ChartComponent.chartContainer
import io.github.filippovissani.portfolium.view.components.ChartComponent.simpleChartContainer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatCurrency
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getReturnBadge
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getValueClass
import kotlinx.html.*

/**
 * Renders the Emergency Fund section
 */
object EmergencyFundSection {
    fun DIV.render(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-shield-alt"></i>""") }
                    +" Emergency Fund"
                }
            }

            div(classes = "section-cards") {
                metricCard(
                    icon = "fas fa-wallet",
                    iconColor = "success",
                    title = "Current Capital",
                    value = formatCurrency(portfolio.emergency.currentCapital)
                )
                metricCard(
                    icon = "fas fa-bullseye",
                    iconColor = "warning",
                    title = "Target Capital",
                    value = formatCurrency(portfolio.emergency.targetCapital)
                )
                metricCard(
                    icon = "fas fa-chart-line",
                    iconColor = "info",
                    title = "Delta",
                    value = formatCurrency(portfolio.emergency.deltaToTarget),
                    valueClass = getValueClass(portfolio.emergency.deltaToTarget, zeroIsPositive = false)
                )
                metricCard(
                    icon = "fas fa-info-circle",
                    iconColor = "primary",
                    title = "Status",
                    value = portfolio.emergency.status,
                    valueClass = "small-value " + when {
                        portfolio.emergency.status.contains("OK", ignoreCase = true) -> "status-good"
                        else -> "status-warning"
                    }
                )
            }

            // Charts
            div(classes = "charts-grid") {
                simpleChartContainer(
                    chartId = "emergencyFundProgressChart",
                    title = "Target Progress",
                    icon = "fas fa-chart-bar"
                )

                if (!portfolio.emergency.isLiquid && portfolio.emergency.historicalPerformance != null) {
                    val badge = getReturnBadge(portfolio.emergency.historicalPerformance)
                    chartContainer(
                        chartId = "emergencyFundHistoricalChart",
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


package io.github.filippovissani.portfolium.view.sections

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.components.CardComponent.metricCard
import io.github.filippovissani.portfolium.view.components.ChartComponent.chartContainer
import io.github.filippovissani.portfolium.view.components.ChartComponent.simpleChartContainer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatCurrency
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatPercentage
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getReturnBadge
import kotlinx.html.*

/**
 * Renders the Overall Performance section
 */
object OverallPerformanceSection {
    fun DIV.render(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-globe"></i>""") }
                    +" Overall Performance"
                }
            }

            div(classes = "section-cards") {
                metricCard(
                    icon = "fas fa-wallet",
                    iconColor = "primary",
                    title = "Total Net Worth",
                    value = formatCurrency(portfolio.totalNetWorth),
                )
                metricCard(
                    icon = "fas fa-chart-line",
                    iconColor = "success",
                    title = "Invested",
                    value = formatPercentage(portfolio.percentInvested),
                )
                metricCard(
                    icon = "fas fa-water",
                    iconColor = "info",
                    title = "Liquid",
                    value = formatPercentage(portfolio.percentLiquid),
                )
            }

            // Charts
            div(classes = "charts-grid") {
                simpleChartContainer(
                    chartId = "overallAssetAllocationChart",
                    title = "Asset Allocation",
                    icon = "fas fa-chart-pie",
                )

                simpleChartContainer(
                    chartId = "overallNetWorthChart",
                    title = "Net Worth Distribution",
                    icon = "fas fa-layer-group",
                )

                if (portfolio.overallHistoricalPerformance != null) {
                    val badge = getReturnBadge(portfolio.overallHistoricalPerformance)
                    chartContainer(
                        chartId = "overallHistoricalPerformanceChart",
                        title = "Overall Historical Performance",
                        icon = "fas fa-chart-area",
                        badge = badge?.first,
                        badgeClass = badge?.second ?: "",
                        fullWidth = true,
                    )
                }
            }
        }
    }
}

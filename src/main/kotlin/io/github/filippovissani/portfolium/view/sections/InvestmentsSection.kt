package io.github.filippovissani.portfolium.view.sections

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.components.CardComponent.metricCard
import io.github.filippovissani.portfolium.view.components.ChartComponent.chartContainer
import io.github.filippovissani.portfolium.view.components.ChartComponent.simpleChartContainer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatCurrency
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatPercentage
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getReturnBadge
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.getValueClass
import kotlinx.html.*

/**
 * Renders the Investments section
 */
object InvestmentsSection {
    fun DIV.render(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                    +" Investments"
                }
            }

            div(classes = "section-cards") {
                metricCard(
                    icon = "fas fa-coins",
                    iconColor = "success",
                    title = "Current Value",
                    value = formatCurrency(portfolio.investments.totalCurrent)
                )
                metricCard(
                    icon = "fas fa-hand-holding-usd",
                    iconColor = "primary",
                    title = "Invested",
                    value = formatCurrency(portfolio.investments.totalInvested)
                )

                val pnl = portfolio.investments.totalCurrent - portfolio.investments.totalInvested
                metricCard(
                    icon = "fas fa-chart-bar",
                    iconColor = "warning",
                    title = "P&L",
                    value = formatCurrency(pnl),
                    valueClass = getValueClass(pnl)
                )
            }

            // Charts
            div(classes = "charts-grid") {
                if (portfolio.investments.itemsWithWeights.isNotEmpty()) {
                    simpleChartContainer(
                        chartId = "investmentsBreakdownChart",
                        title = "Portfolio Breakdown",
                        icon = "fas fa-chart-pie"
                    )
                }

                if (portfolio.historicalPerformance != null) {
                    val badge = getReturnBadge(portfolio.historicalPerformance)
                    chartContainer(
                        chartId = "investmentsHistoricalChart",
                        title = "Historical Performance",
                        icon = "fas fa-chart-area",
                        badge = badge?.first,
                        badgeClass = badge?.second ?: ""
                    )
                }
            }

            // Investment Details Table
            if (portfolio.investments.itemsWithWeights.isNotEmpty()) {
                div(classes = "card") {
                    div(classes = "chart-title") {
                        unsafe { raw("""<i class="fas fa-table"></i>""") }
                        +"Investment Details"
                    }
                    table(classes = "investments-table") {
                        thead {
                            tr {
                                th { +"ETF" }
                                th { +"Ticker" }
                                th { +"Current Value" }
                                th { +"P&L" }
                                th { +"Weight" }
                            }
                        }
                        tbody {
                            portfolio.investments.itemsWithWeights.forEach { (inv, weight) ->
                                tr {
                                    td { +inv.etf }
                                    td { +inv.ticker }
                                    td { +formatCurrency(inv.currentValue) }
                                    td(classes = getValueClass(inv.pnl)) {
                                        +formatCurrency(inv.pnl)
                                    }
                                    td { +formatPercentage(weight, 2) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

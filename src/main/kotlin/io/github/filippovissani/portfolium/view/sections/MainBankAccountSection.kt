package io.github.filippovissani.portfolium.view.sections

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.components.CardComponent.metricCard
import io.github.filippovissani.portfolium.view.components.ChartComponent.simpleChartContainer
import io.github.filippovissani.portfolium.view.utils.FormattingUtils.formatCurrency
import kotlinx.html.*

/**
 * Renders the Main Bank Account section
 */
object MainBankAccountSection {
    fun DIV.render(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-university"></i>""") }
                    +" Main Bank Account"
                }
            }

            div(classes = "section-cards") {
                metricCard(
                    icon = "fas fa-coins",
                    iconColor = "info",
                    title = "Balance",
                    value = formatCurrency(portfolio.liquidity.net)
                )
                metricCard(
                    icon = "fas fa-arrow-up",
                    iconColor = "success",
                    title = "Total Income",
                    value = formatCurrency(portfolio.liquidity.totalIncome),
                    valueClass = "positive"
                )
                metricCard(
                    icon = "fas fa-arrow-down",
                    iconColor = "danger",
                    title = "Total Expense",
                    value = formatCurrency(portfolio.liquidity.totalExpense),
                    valueClass = "negative"
                )
                metricCard(
                    icon = "fas fa-calendar-alt",
                    iconColor = "warning",
                    title = "Avg Monthly (12m)",
                    value = formatCurrency(portfolio.liquidity.avgMonthlyExpense12m)
                )
            }

            // Statistics charts
            if (portfolio.liquidity.statistics != null) {
                div(classes = "charts-grid") {
                    simpleChartContainer(
                        chartId = "mainBankMonthlyTrendChart",
                        title = "Monthly Trend",
                        icon = "fas fa-chart-line"
                    )
                    simpleChartContainer(
                        chartId = "mainBankExpenseCategoryChart",
                        title = "Expense by Category",
                        icon = "fas fa-chart-pie"
                    )
                }
            }
        }
    }
}

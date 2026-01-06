package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.math.RoundingMode
import com.google.gson.Gson
import io.ktor.server.http.content.staticResources

object WebView {
    private lateinit var portfolioData: Portfolio
    private val gson = Gson()

    fun startServer(portfolio: Portfolio, port: Int = 8080) {
        portfolioData = portfolio

        embeddedServer(Netty, port = port) {
            routing {
                // Serve static files (CSS, JS)
                staticResources("/static", "static")

                get("/") {
                    call.respondHtml {
                        head {
                            title { +"Portfolium - Personal Finance Dashboard" }
                            meta(charset = "UTF-8")
                            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                            script(src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js") {}
                            link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css") {}
                            link(rel = "preconnect", href = "https://fonts.googleapis.com")
                            link(rel = "preconnect", href = "https://fonts.gstatic.com") {
                                attributes["crossorigin"] = ""
                            }
                            link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap")
                            link(rel = "stylesheet", href = "/static/css/styles.css")
                        }
                        body {
                            div(classes = "header-wrapper") {
                                div(classes = "header-content") {
                                    h1 { +"Portfolium" }
                                    div(classes = "subtitle") { +"Professional Personal Finance Dashboard" }
                                }
                            }

                            div(classes = "container") {
                                // Summary Cards
                                div(classes = "summary-cards") {
                                    div(classes = "card") {
                                        div(classes = "card-header") {
                                            div(classes = "card-icon primary") {
                                                unsafe { raw("""<i class="fas fa-wallet"></i>""") }
                                            }
                                            h2 { +"Net Worth" }
                                        }
                                        div(classes = "value") { +"€${portfolioData.totalNetWorth}" }
                                    }
                                    div(classes = "card") {
                                        div(classes = "card-header") {
                                            div(classes = "card-icon info") {
                                                unsafe { raw("""<i class="fas fa-water"></i>""") }
                                            }
                                            h2 { +"Liquidity" }
                                        }
                                        div(classes = "value") { +"€${portfolioData.liquidity.net}" }
                                        div(classes = "label") {
                                            span(classes = "label-icon") {}
                                            +"Income: €${portfolioData.liquidity.totalIncome}"
                                        }
                                        div(classes = "label") {
                                            span(classes = "label-icon") {}
                                            +"Expense: €${portfolioData.liquidity.totalExpense}"
                                        }
                                    }
                                    div(classes = "card") {
                                        div(classes = "card-header") {
                                            div(classes = "card-icon success") {
                                                unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                                            }
                                            h2 { +"Investments" }
                                        }
                                        div(classes = "value") { +"€${portfolioData.investments.totalCurrent}" }
                                        div(classes = "label") {
                                            span(classes = "label-icon") {}
                                            +"Invested: €${portfolioData.investments.totalInvested}"
                                        }
                                        val pnl = portfolioData.investments.totalCurrent - portfolioData.investments.totalInvested
                                        div(classes = if (pnl >= java.math.BigDecimal.ZERO) "label positive" else "label negative") {
                                            span(classes = "label-icon") {}
                                            +"P&L: €$pnl"
                                        }
                                    }
                                    div(classes = "card") {
                                        div(classes = "card-header") {
                                            div(classes = "card-icon warning") {
                                                unsafe { raw("""<i class="fas fa-shield-alt"></i>""") }
                                            }
                                            h2 { +"Emergency Fund" }
                                        }
                                        div(classes = "value") { +"€${portfolioData.emergency.currentCapital}" }
                                        div(classes = "label") {
                                            span(classes = "label-icon") {}
                                            +"Target: €${portfolioData.emergency.targetCapital}"
                                        }
                                        val statusClass = when {
                                            portfolioData.emergency.status.contains("OK", ignoreCase = true) -> "status-good"
                                            portfolioData.emergency.status.contains("below", ignoreCase = true) -> "status-warning"
                                            else -> "status-bad"
                                        }
                                        div(classes = "label $statusClass") {
                                            span(classes = "label-icon") {}
                                            +portfolioData.emergency.status
                                        }
                                    }
                                }

                                // Charts
                                div(classes = "charts-grid") {
                                    // Asset Allocation Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") {
                                            unsafe { raw("""<i class="fas fa-chart-pie"></i>""") }
                                            +"Asset Allocation"
                                        }
                                        canvas { id = "assetAllocationChart" }
                                    }

                                    // Net Worth Distribution Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") {
                                            unsafe { raw("""<i class="fas fa-layer-group"></i>""") }
                                            +"Net Worth Distribution"
                                        }
                                        canvas { id = "netWorthChart" }
                                    }

                                    // Investments Breakdown Chart
                                    if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                        div(classes = "chart-container") {
                                            div(classes = "chart-title") {
                                                unsafe { raw("""<i class="fas fa-briefcase"></i>""") }
                                                +"Investment Portfolio Breakdown"
                                            }
                                            canvas { id = "investmentsChart" }
                                        }
                                    }

                                    // Planned Expenses Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") {
                                            unsafe { raw("""<i class="fas fa-calendar-check"></i>""") }
                                            +"Planned Expenses Coverage"
                                        }
                                        canvas { id = "plannedExpensesChart" }
                                    }
                                }

                                // Investments Detail Table
                                if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
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
                                                portfolioData.investments.itemsWithWeights.forEach { (inv, weight) ->
                                                    tr {
                                                        td { +inv.etf }
                                                        td { +inv.ticker }
                                                        td { +"€${inv.currentValue}" }
                                                        val pnlClass = if (inv.pnl >= java.math.BigDecimal.ZERO) "positive" else "negative"
                                                        td(classes = pnlClass) { +"€${inv.pnl}" }
                                                        td {
                                                            +"${(weight * java.math.BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                footer {
                                    +"Portfolium - Personal Finance Dashboard | Server running on port $port"
                                }
                            }

                            // Initialize portfolio data for charts
                            script {
                                unsafe {
                                    raw("""
                                        const portfolioData = {
                                            percentLiquid: ${(portfolioData.percentLiquid * java.math.BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)},
                                            percentInvested: ${(portfolioData.percentInvested * java.math.BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)},
                                            emergency: {
                                                currentCapital: ${portfolioData.emergency.currentCapital}
                                            },
                                            investments: {
                                                totalCurrent: ${portfolioData.investments.totalCurrent},
                                                itemsWithWeights: ${
                                                    if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                                        val items = portfolioData.investments.itemsWithWeights.map { (inv, _) ->
                                                            mapOf("ticker" to inv.ticker, "currentValue" to inv.currentValue)
                                                        }
                                                        gson.toJson(items)
                                                    } else "[]"
                                                }
                                            },
                                            liquidity: {
                                                net: ${portfolioData.liquidity.net}
                                            },
                                            planned: {
                                                totalEstimated: ${portfolioData.planned.totalEstimated},
                                                totalAccrued: ${portfolioData.planned.totalAccrued}
                                            }
                                        };
                                    """.trimIndent())
                                }
                            }
                            // Load Chart.js library and custom charts
                            script(src = "/static/js/charts.js") {}
                            script {
                                unsafe {
                                    raw("""
                                        // Initialize charts when DOM is ready
                                        document.addEventListener('DOMContentLoaded', function() {
                                            initializeCharts(portfolioData);
                                        });
                                    """.trimIndent())
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}


package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.html.*
import java.math.RoundingMode
import com.google.gson.Gson

object WebView {
    private lateinit var portfolioData: Portfolio
    private val gson = Gson()

    fun startServer(portfolio: Portfolio, port: Int = 8080) {
        portfolioData = portfolio

        embeddedServer(Netty, port = port) {
            routing {
                get("/") {
                    call.respondHtml {
                        head {
                            title { +"Portfolium - Personal Finance Dashboard" }
                            meta(charset = "UTF-8")
                            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                            script(src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js") {}
                            style {
                                unsafe {
                                    raw("""
                                        * { margin: 0; padding: 0; box-sizing: border-box; }
                                        body {
                                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                            color: #333;
                                            padding: 20px;
                                            min-height: 100vh;
                                        }
                                        .container {
                                            max-width: 1400px;
                                            margin: 0 auto;
                                        }
                                        h1 {
                                            color: white;
                                            text-align: center;
                                            margin-bottom: 30px;
                                            font-size: 2.5em;
                                            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                                        }
                                        .summary-cards {
                                            display: grid;
                                            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                                            gap: 20px;
                                            margin-bottom: 30px;
                                        }
                                        .card {
                                            background: white;
                                            padding: 25px;
                                            border-radius: 15px;
                                            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                                            transition: transform 0.3s ease, box-shadow 0.3s ease;
                                        }
                                        .card:hover {
                                            transform: translateY(-5px);
                                            box-shadow: 0 15px 40px rgba(0,0,0,0.3);
                                        }
                                        .card h2 {
                                            color: #667eea;
                                            font-size: 1.2em;
                                            margin-bottom: 15px;
                                            border-bottom: 2px solid #667eea;
                                            padding-bottom: 10px;
                                        }
                                        .card .value {
                                            font-size: 2em;
                                            font-weight: bold;
                                            color: #2d3748;
                                            margin: 10px 0;
                                        }
                                        .card .label {
                                            color: #718096;
                                            font-size: 0.9em;
                                            margin: 5px 0;
                                        }
                                        .charts-grid {
                                            display: grid;
                                            grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
                                            gap: 20px;
                                            margin-bottom: 30px;
                                        }
                                        .chart-container {
                                            background: white;
                                            padding: 25px;
                                            border-radius: 15px;
                                            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                                            position: relative;
                                            height: 400px;
                                        }
                                        .chart-title {
                                            color: #667eea;
                                            font-size: 1.3em;
                                            margin-bottom: 15px;
                                            text-align: center;
                                            font-weight: bold;
                                        }
                                        canvas {
                                            max-height: 320px !important;
                                        }
                                        .status-good { color: #48bb78; }
                                        .status-warning { color: #ed8936; }
                                        .status-bad { color: #f56565; }
                                        footer {
                                            text-align: center;
                                            color: white;
                                            margin-top: 40px;
                                            padding: 20px;
                                            font-size: 0.9em;
                                        }
                                        .investments-table {
                                            width: 100%;
                                            margin-top: 15px;
                                            border-collapse: collapse;
                                        }
                                        .investments-table th,
                                        .investments-table td {
                                            padding: 8px;
                                            text-align: left;
                                            border-bottom: 1px solid #e2e8f0;
                                        }
                                        .investments-table th {
                                            background: #f7fafc;
                                            font-weight: bold;
                                            color: #4a5568;
                                        }
                                        .positive { color: #48bb78; }
                                        .negative { color: #f56565; }
                                    """.trimIndent())
                                }
                            }
                        }
                        body {
                            div(classes = "container") {
                                h1 { +"💰 Portfolium Dashboard" }

                                // Summary Cards
                                div(classes = "summary-cards") {
                                    div(classes = "card") {
                                        h2 { +"Net Worth" }
                                        div(classes = "value") { +"€${portfolioData.totalNetWorth}" }
                                    }
                                    div(classes = "card") {
                                        h2 { +"Liquidity" }
                                        div(classes = "value") { +"€${portfolioData.liquidity.net}" }
                                        div(classes = "label") { +"Income: €${portfolioData.liquidity.totalIncome}" }
                                        div(classes = "label") { +"Expense: €${portfolioData.liquidity.totalExpense}" }
                                    }
                                    div(classes = "card") {
                                        h2 { +"Investments" }
                                        div(classes = "value") { +"€${portfolioData.investments.totalCurrent}" }
                                        div(classes = "label") {
                                            +"Invested: €${portfolioData.investments.totalInvested}"
                                        }
                                        val pnl = portfolioData.investments.totalCurrent - portfolioData.investments.totalInvested
                                        div(classes = if (pnl >= java.math.BigDecimal.ZERO) "label positive" else "label negative") {
                                            +"P&L: €$pnl"
                                        }
                                    }
                                    div(classes = "card") {
                                        h2 { +"Emergency Fund" }
                                        div(classes = "value") { +"€${portfolioData.emergency.currentCapital}" }
                                        div(classes = "label") { +"Target: €${portfolioData.emergency.targetCapital}" }
                                        val statusClass = when {
                                            portfolioData.emergency.status.contains("OK", ignoreCase = true) -> "status-good"
                                            portfolioData.emergency.status.contains("below", ignoreCase = true) -> "status-warning"
                                            else -> "status-bad"
                                        }
                                        div(classes = "label $statusClass") { +portfolioData.emergency.status }
                                    }
                                }

                                // Charts
                                div(classes = "charts-grid") {
                                    // Asset Allocation Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") { +"Asset Allocation" }
                                        canvas { id = "assetAllocationChart" }
                                    }

                                    // Net Worth Distribution Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") { +"Net Worth Distribution" }
                                        canvas { id = "netWorthChart" }
                                    }

                                    // Investments Breakdown Chart
                                    if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                        div(classes = "chart-container") {
                                            div(classes = "chart-title") { +"Investment Portfolio Breakdown" }
                                            canvas { id = "investmentsChart" }
                                        }
                                    }

                                    // Planned Expenses Chart
                                    div(classes = "chart-container") {
                                        div(classes = "chart-title") { +"Planned Expenses Coverage" }
                                        canvas { id = "plannedExpensesChart" }
                                    }
                                }

                                // Investments Detail Table
                                if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                    div(classes = "card") {
                                        h2 { +"Investment Details" }
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

                            // Chart.js Scripts
                            script {
                                unsafe {
                                    raw("""
                                        // Asset Allocation Chart
                                        const assetCtx = document.getElementById('assetAllocationChart');
                                        new Chart(assetCtx, {
                                            type: 'doughnut',
                                            data: {
                                                labels: ['Liquid', 'Invested'],
                                                datasets: [{
                                                    data: [
                                                        ${(portfolioData.percentLiquid * java.math.BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)},
                                                        ${(portfolioData.percentInvested * java.math.BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}
                                                    ],
                                                    backgroundColor: ['#4299e1', '#48bb78'],
                                                    borderWidth: 2,
                                                    borderColor: '#fff'
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { position: 'bottom' },
                                                    tooltip: {
                                                        callbacks: {
                                                            label: function(context) {
                                                                return context.label + ': ' + context.parsed + '%';
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        
                                        // Net Worth Distribution Chart
                                        const netWorthCtx = document.getElementById('netWorthChart');
                                        new Chart(netWorthCtx, {
                                            type: 'bar',
                                            data: {
                                                labels: ['Emergency Fund', 'Investments', 'Liquidity'],
                                                datasets: [{
                                                    label: 'Value (€)',
                                                    data: [
                                                        ${portfolioData.emergency.currentCapital},
                                                        ${portfolioData.investments.totalCurrent},
                                                        ${portfolioData.liquidity.net}
                                                    ],
                                                    backgroundColor: ['#ed8936', '#48bb78', '#4299e1'],
                                                    borderWidth: 0
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { display: false }
                                                },
                                                scales: {
                                                    y: { beginAtZero: true }
                                                }
                                            }
                                        });
                                        
                                        ${if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                            val labels = portfolioData.investments.itemsWithWeights.map { it.first.ticker }
                                            val values = portfolioData.investments.itemsWithWeights.map { it.first.currentValue }
                                            val colors = listOf("#667eea", "#764ba2", "#f093fb", "#4facfe", "#43e97b", "#fa709a")
                                            """
                                            // Investments Breakdown Chart
                                            const investmentsCtx = document.getElementById('investmentsChart');
                                            new Chart(investmentsCtx, {
                                                type: 'pie',
                                                data: {
                                                    labels: ${gson.toJson(labels)},
                                                    datasets: [{
                                                        data: ${gson.toJson(values)},
                                                        backgroundColor: ${gson.toJson(colors.take(labels.size))},
                                                        borderWidth: 2,
                                                        borderColor: '#fff'
                                                    }]
                                                },
                                                options: {
                                                    responsive: true,
                                                    maintainAspectRatio: true,
                                                    plugins: {
                                                        legend: { position: 'bottom' },
                                                        tooltip: {
                                                            callbacks: {
                                                                label: function(context) {
                                                                    return context.label + ': €' + context.parsed;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                            """.trimIndent()
                                        } else ""}
                                        
                                        // Planned Expenses Chart
                                        const plannedCtx = document.getElementById('plannedExpensesChart');
                                        new Chart(plannedCtx, {
                                            type: 'bar',
                                            data: {
                                                labels: ['Estimated', 'Accrued'],
                                                datasets: [{
                                                    label: 'Amount (€)',
                                                    data: [
                                                        ${portfolioData.planned.totalEstimated},
                                                        ${portfolioData.planned.totalAccrued}
                                                    ],
                                                    backgroundColor: ['#667eea', '#48bb78'],
                                                    borderWidth: 0
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { display: false }
                                                },
                                                scales: {
                                                    y: { beginAtZero: true }
                                                }
                                            }
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


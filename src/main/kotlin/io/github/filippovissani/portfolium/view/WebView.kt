package io.github.filippovissani.portfolium.view

import com.google.gson.Gson
import io.github.filippovissani.portfolium.model.Portfolio
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.math.RoundingMode

object WebView {
    private lateinit var portfolioData: Portfolio
    private val gson = Gson()

    // SECTION 1: Main Bank Account
    private fun DIV.renderMainBankAccountSection(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-university"></i>""") }
                    +" Main Bank Account"
                }
            }

            div(classes = "section-cards") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon info") {
                            unsafe { raw("""<i class="fas fa-coins"></i>""") }
                        }
                        h3 { +"Balance" }
                    }
                    div(classes = "value") { +"€${portfolio.liquidity.net}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon success") {
                            unsafe { raw("""<i class="fas fa-arrow-up"></i>""") }
                        }
                        h3 { +"Total Income" }
                    }
                    div(classes = "value positive") { +"€${portfolio.liquidity.totalIncome}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon danger") {
                            unsafe { raw("""<i class="fas fa-arrow-down"></i>""") }
                        }
                        h3 { +"Total Expense" }
                    }
                    div(classes = "value negative") { +"€${portfolio.liquidity.totalExpense}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon warning") {
                            unsafe { raw("""<i class="fas fa-calendar-alt"></i>""") }
                        }
                        h3 { +"Avg Monthly (12m)" }
                    }
                    div(classes = "value") { +"€${portfolio.liquidity.avgMonthlyExpense12m}" }
                }
            }

            // Statistics charts
            if (portfolio.liquidity.statistics != null) {
                div(classes = "charts-grid") {
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                            +"Monthly Trend"
                        }
                        canvas { id = "mainBankMonthlyTrendChart" }
                    }
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-pie"></i>""") }
                            +"Expense by Category"
                        }
                        canvas { id = "mainBankExpenseCategoryChart" }
                    }
                }
            }
        }
    }

    // SECTION 2: Planned Expenses
    private fun DIV.renderPlannedExpensesSection(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-calendar-check"></i>""") }
                    +" Planned Expenses"
                }
            }

            div(classes = "section-cards") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon primary") {
                            unsafe { raw("""<i class="fas fa-piggy-bank"></i>""") }
                        }
                        h3 { +"Accrued Capital" }
                    }
                    div(classes = "value") { +"€${portfolio.planned.totalAccrued}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon warning") {
                            unsafe { raw("""<i class="fas fa-bullseye"></i>""") }
                        }
                        h3 { +"Target Capital" }
                    }
                    div(classes = "value") { +"€${portfolio.planned.totalEstimated}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon info") {
                            unsafe { raw("""<i class="fas fa-percentage"></i>""") }
                        }
                        h3 { +"Coverage" }
                    }
                    div(classes = "value") {
                        +"${
                            (portfolio.planned.coverageRatio * java.math.BigDecimal(100)).setScale(
                                1,
                                RoundingMode.HALF_UP,
                            )
                        }%"
                    }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon success") {
                            unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                        }
                        h3 { +"Invested" }
                    }
                    val investedPercentage =
                        if (portfolio.planned.totalAccrued > java.math.BigDecimal.ZERO) {
                            (portfolio.planned.investedAccrued / portfolio.planned.totalAccrued * java.math.BigDecimal(100)).setScale(
                                1,
                                RoundingMode.HALF_UP,
                            )
                        } else {
                            java.math.BigDecimal.ZERO
                        }
                    div(classes = "value") { +"$investedPercentage%" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon primary") {
                            unsafe { raw("""<i class="fas fa-water"></i>""") }
                        }
                        h3 { +"Liquid" }
                    }
                    val liquidPercentage =
                        if (portfolio.planned.totalAccrued > java.math.BigDecimal.ZERO) {
                            (portfolio.planned.liquidAccrued / portfolio.planned.totalAccrued * java.math.BigDecimal(100)).setScale(
                                1,
                                RoundingMode.HALF_UP,
                            )
                        } else {
                            java.math.BigDecimal.ZERO
                        }
                    div(classes = "value") { +"$liquidPercentage%" }
                }
            }

            // Charts
            div(classes = "charts-grid") {
                div(classes = "chart-container") {
                    div(classes = "chart-title") {
                        unsafe { raw("""<i class="fas fa-chart-bar"></i>""") }
                        +"Coverage Progress"
                    }
                    canvas { id = "plannedExpensesCoverageChart" }
                }

                if (portfolio.planned.isInvested && portfolio.planned.historicalPerformance != null) {
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-area"></i>""") }
                            +"Historical Performance"
                            val hp = portfolio.planned.historicalPerformance
                            if (hp.totalReturn != java.math.BigDecimal.ZERO) {
                                val returnClass =
                                    if (hp.totalReturn >= java.math.BigDecimal.ZERO) "positive" else "negative"
                                span(classes = "return-badge $returnClass") {
                                    +"${hp.totalReturn}%"
                                }
                            }
                        }
                        canvas { id = "plannedExpensesHistoricalChart" }
                    }
                }
            }
        }
    }

    // SECTION 3: Emergency Fund
    private fun DIV.renderEmergencyFundSection(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-shield-alt"></i>""") }
                    +" Emergency Fund"
                }
            }

            div(classes = "section-cards") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon success") {
                            unsafe { raw("""<i class="fas fa-wallet"></i>""") }
                        }
                        h3 { +"Current Capital" }
                    }
                    div(classes = "value") { +"€${portfolio.emergency.currentCapital}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon warning") {
                            unsafe { raw("""<i class="fas fa-bullseye"></i>""") }
                        }
                        h3 { +"Target Capital" }
                    }
                    div(classes = "value") { +"€${portfolio.emergency.targetCapital}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon info") {
                            unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                        }
                        h3 { +"Delta" }
                    }
                    val deltaClass =
                        if (portfolio.emergency.deltaToTarget <= java.math.BigDecimal.ZERO) "positive" else "negative"
                    div(classes = "value $deltaClass") { +"€${portfolio.emergency.deltaToTarget}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon primary") {
                            unsafe { raw("""<i class="fas fa-info-circle"></i>""") }
                        }
                        h3 { +"Status" }
                    }
                    val statusClass =
                        when {
                            portfolio.emergency.status.contains("OK", ignoreCase = true) -> "status-good"
                            else -> "status-warning"
                        }
                    div(classes = "value small-value $statusClass") { +portfolio.emergency.status }
                }
            }

            // Charts
            div(classes = "charts-grid") {
                div(classes = "chart-container") {
                    div(classes = "chart-title") {
                        unsafe { raw("""<i class="fas fa-chart-bar"></i>""") }
                        +"Target Progress"
                    }
                    canvas { id = "emergencyFundProgressChart" }
                }

                if (!portfolio.emergency.isLiquid && portfolio.emergency.historicalPerformance != null) {
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-area"></i>""") }
                            +"Historical Performance"
                            val hp = portfolio.emergency.historicalPerformance
                            if (hp.totalReturn != java.math.BigDecimal.ZERO) {
                                val returnClass =
                                    if (hp.totalReturn >= java.math.BigDecimal.ZERO) "positive" else "negative"
                                span(classes = "return-badge $returnClass") {
                                    +"${hp.totalReturn}%"
                                }
                            }
                        }
                        canvas { id = "emergencyFundHistoricalChart" }
                    }
                }
            }
        }
    }

    // SECTION 4: Investments
    private fun DIV.renderInvestmentsSection(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                    +" Investments"
                }
            }

            div(classes = "section-cards") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon success") {
                            unsafe { raw("""<i class="fas fa-coins"></i>""") }
                        }
                        h3 { +"Current Value" }
                    }
                    div(classes = "value") { +"€${portfolio.investments.totalCurrent}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon primary") {
                            unsafe { raw("""<i class="fas fa-hand-holding-usd"></i>""") }
                        }
                        h3 { +"Invested" }
                    }
                    div(classes = "value") { +"€${portfolio.investments.totalInvested}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon warning") {
                            unsafe { raw("""<i class="fas fa-chart-bar"></i>""") }
                        }
                        h3 { +"P&L" }
                    }
                    val pnl = portfolio.investments.totalCurrent - portfolio.investments.totalInvested
                    val pnlClass = if (pnl >= java.math.BigDecimal.ZERO) "positive" else "negative"
                    div(classes = "value $pnlClass") { +"€$pnl" }
                }
            }

            // Charts
            div(classes = "charts-grid") {
                if (portfolio.investments.itemsWithWeights.isNotEmpty()) {
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-pie"></i>""") }
                            +"Portfolio Breakdown"
                        }
                        canvas { id = "investmentsBreakdownChart" }
                    }
                }

                if (portfolio.historicalPerformance != null) {
                    div(classes = "chart-container") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-area"></i>""") }
                            +"Historical Performance"
                            val hp = portfolio.historicalPerformance
                            if (hp.totalReturn != java.math.BigDecimal.ZERO) {
                                val returnClass =
                                    if (hp.totalReturn >= java.math.BigDecimal.ZERO) "positive" else "negative"
                                span(classes = "return-badge $returnClass") {
                                    +"${hp.totalReturn}%"
                                }
                            }
                        }
                        canvas { id = "investmentsHistoricalChart" }
                    }
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
        }
    }

    // SECTION 5: Overall Performance
    private fun DIV.renderOverallPerformanceSection(portfolio: Portfolio) {
        div(classes = "section") {
            div(classes = "section-header") {
                h2 {
                    unsafe { raw("""<i class="fas fa-globe"></i>""") }
                    +" Overall Performance"
                }
            }

            div(classes = "section-cards") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon primary") {
                            unsafe { raw("""<i class="fas fa-wallet"></i>""") }
                        }
                        h3 { +"Total Net Worth" }
                    }
                    div(classes = "value") { +"€${portfolio.totalNetWorth}" }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon success") {
                            unsafe { raw("""<i class="fas fa-chart-line"></i>""") }
                        }
                        h3 { +"Invested" }
                    }
                    div(classes = "value") {
                        +"${
                            (portfolio.percentInvested * java.math.BigDecimal(100)).setScale(
                                1,
                                RoundingMode.HALF_UP,
                            )
                        }%"
                    }
                }
                div(classes = "card") {
                    div(classes = "card-header") {
                        div(classes = "card-icon info") {
                            unsafe { raw("""<i class="fas fa-water"></i>""") }
                        }
                        h3 { +"Liquid" }
                    }
                    div(classes = "value") {
                        +"${
                            (portfolio.percentLiquid * java.math.BigDecimal(100)).setScale(
                                1,
                                RoundingMode.HALF_UP,
                            )
                        }%"
                    }
                }
            }

            // Charts
            div(classes = "charts-grid") {
                div(classes = "chart-container") {
                    div(classes = "chart-title") {
                        unsafe { raw("""<i class="fas fa-chart-pie"></i>""") }
                        +"Asset Allocation"
                    }
                    canvas { id = "overallAssetAllocationChart" }
                }

                div(classes = "chart-container") {
                    div(classes = "chart-title") {
                        unsafe { raw("""<i class="fas fa-layer-group"></i>""") }
                        +"Net Worth Distribution"
                    }
                    canvas { id = "overallNetWorthChart" }
                }

                if (portfolio.overallHistoricalPerformance != null) {
                    div(classes = "chart-container full-width") {
                        div(classes = "chart-title") {
                            unsafe { raw("""<i class="fas fa-chart-area"></i>""") }
                            +"Overall Historical Performance"
                            val hp = portfolio.overallHistoricalPerformance
                            if (hp.totalReturn != java.math.BigDecimal.ZERO) {
                                val returnClass =
                                    if (hp.totalReturn >= java.math.BigDecimal.ZERO) "positive" else "negative"
                                span(classes = "return-badge $returnClass") {
                                    +"${hp.totalReturn}%"
                                }
                            }
                        }
                        canvas { id = "overallHistoricalPerformanceChart" }
                    }
                }
            }
        }
    }

    fun startServer(
        portfolio: Portfolio,
        port: Int = 8080,
    ) {
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
                            link(
                                rel = "stylesheet",
                                href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css",
                            ) {}
                            link(rel = "preconnect", href = "https://fonts.googleapis.com")
                            link(rel = "preconnect", href = "https://fonts.gstatic.com") {
                                attributes["crossorigin"] = ""
                            }
                            link(
                                rel = "stylesheet",
                                href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap",
                            )
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
                                // SECTION 1: Main Bank Account
                                renderMainBankAccountSection(portfolioData)

                                // SECTION 2: Planned Expenses
                                renderPlannedExpensesSection(portfolioData)

                                // SECTION 3: Emergency Fund
                                renderEmergencyFundSection(portfolioData)

                                // SECTION 4: Investments
                                renderInvestmentsSection(portfolioData)

                                // SECTION 5: Overall Performance
                                renderOverallPerformanceSection(portfolioData)

                                footer {
                                    +"Portfolium - Personal Finance Dashboard | Server running on port $port"
                                }
                            }

                            // Initialize portfolio data for charts
                            script {
                                unsafe {
                                    raw(
                                        """
                                        const portfolioData = {
                                            percentLiquid: ${
                                            (portfolioData.percentLiquid * java.math.BigDecimal(100)).setScale(
                                                2,
                                                RoundingMode.HALF_UP,
                                            )
                                        },
                                            percentInvested: ${
                                            (portfolioData.percentInvested * java.math.BigDecimal(100)).setScale(
                                                2,
                                                RoundingMode.HALF_UP,
                                            )
                                        },
                                            liquidity: {
                                                net: ${portfolioData.liquidity.net},
                                                totalIncome: ${portfolioData.liquidity.totalIncome},
                                                totalExpense: ${portfolioData.liquidity.totalExpense},
                                                avgMonthlyExpense12m: ${portfolioData.liquidity.avgMonthlyExpense12m},
                                                statistics: ${
                                            if (portfolioData.liquidity.statistics != null) {
                                                gson.toJson(
                                                    mapOf(
                                                        "monthlyTrend" to
                                                            portfolioData.liquidity.statistics?.monthlyTrend?.map {
                                                                mapOf(
                                                                    "yearMonth" to it.yearMonth,
                                                                    "income" to it.income,
                                                                    "expense" to it.expense,
                                                                    "net" to it.net,
                                                                )
                                                            },
                                                        "topExpenseCategories" to
                                                            portfolioData.liquidity.statistics?.topExpenseCategories?.map {
                                                                listOf(
                                                                    it.first,
                                                                    it.second,
                                                                )
                                                            },
                                                        "topIncomeCategories" to
                                                            portfolioData.liquidity.statistics?.topIncomeCategories?.map {
                                                                listOf(
                                                                    it.first,
                                                                    it.second,
                                                                )
                                                            },
                                                        "totalByCategory" to portfolioData.liquidity.statistics?.totalByCategory,
                                                    ),
                                                )
                                            } else {
                                                "null"
                                            }
                                        }
                                            },
                                            emergency: {
                                                currentCapital: ${portfolioData.emergency.currentCapital},
                                                targetCapital: ${portfolioData.emergency.targetCapital},
                                                deltaToTarget: ${portfolioData.emergency.deltaToTarget},
                                                status: "${portfolioData.emergency.status}",
                                                isLiquid: ${portfolioData.emergency.isLiquid},
                                                historicalPerformance: ${
                                            if (portfolioData.emergency.historicalPerformance != null) {
                                                val dataPoints =
                                                    portfolioData.emergency.historicalPerformance?.dataPoints?.map { dp ->
                                                        mapOf("date" to dp.date.toString(), "value" to dp.value)
                                                    }
                                                gson.toJson(
                                                    mapOf(
                                                        "dataPoints" to dataPoints,
                                                        "totalReturn" to portfolioData.emergency.historicalPerformance?.totalReturn,
                                                        "annualizedReturn" to portfolioData.emergency.historicalPerformance?.annualizedReturn,
                                                    ),
                                                )
                                            } else {
                                                "null"
                                            }
                                        }
                                            },
                                            investments: {
                                                totalCurrent: ${portfolioData.investments.totalCurrent},
                                                totalInvested: ${portfolioData.investments.totalInvested},
                                                itemsWithWeights: ${
                                            if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                                val items =
                                                    portfolioData.investments.itemsWithWeights.map { (inv, weight) ->
                                                        mapOf(
                                                            "ticker" to inv.ticker,
                                                            "etf" to inv.etf,
                                                            "currentValue" to inv.currentValue,
                                                            "weight" to weight,
                                                        )
                                                    }
                                                gson.toJson(items)
                                            } else {
                                                "[]"
                                            }
                                        }
                                            },
                                            planned: {
                                                totalEstimated: ${portfolioData.planned.totalEstimated},
                                                totalAccrued: ${portfolioData.planned.totalAccrued},
                                                coverageRatio: ${portfolioData.planned.coverageRatio},
                                                liquidAccrued: ${portfolioData.planned.liquidAccrued},
                                                investedAccrued: ${portfolioData.planned.investedAccrued},
                                                isInvested: ${portfolioData.planned.isInvested},
                                                historicalPerformance: ${
                                            if (portfolioData.planned.historicalPerformance != null) {
                                                val dataPoints =
                                                    portfolioData.planned.historicalPerformance?.dataPoints?.map { dp ->
                                                        mapOf("date" to dp.date.toString(), "value" to dp.value)
                                                    }
                                                gson.toJson(
                                                    mapOf(
                                                        "dataPoints" to dataPoints,
                                                        "totalReturn" to portfolioData.planned.historicalPerformance?.totalReturn,
                                                        "annualizedReturn" to portfolioData.planned.historicalPerformance?.annualizedReturn,
                                                    ),
                                                )
                                            } else {
                                                "null"
                                            }
                                        }
                                            },
                                            historicalPerformance: ${
                                            if (portfolioData.historicalPerformance != null) {
                                                val dataPoints =
                                                    portfolioData.historicalPerformance?.dataPoints?.map { dp ->
                                                        mapOf("date" to dp.date.toString(), "value" to dp.value)
                                                    }
                                                gson.toJson(
                                                    mapOf(
                                                        "dataPoints" to dataPoints,
                                                        "totalReturn" to portfolioData.historicalPerformance?.totalReturn,
                                                        "annualizedReturn" to portfolioData.historicalPerformance?.annualizedReturn,
                                                    ),
                                                )
                                            } else {
                                                "null"
                                            }
                                        },
                                            overallHistoricalPerformance: ${
                                            if (portfolioData.overallHistoricalPerformance != null) {
                                                val dataPoints =
                                                    portfolioData.overallHistoricalPerformance?.dataPoints?.map { dp ->
                                                        mapOf("date" to dp.date.toString(), "value" to dp.value)
                                                    }
                                                gson.toJson(
                                                    mapOf(
                                                        "dataPoints" to dataPoints,
                                                        "totalReturn" to portfolioData.overallHistoricalPerformance?.totalReturn,
                                                        "annualizedReturn" to portfolioData.overallHistoricalPerformance?.annualizedReturn,
                                                    ),
                                                )
                                            } else {
                                                "null"
                                            }
                                        }
                                        };
                                        """.trimIndent(),
                                    )
                                }
                            }
                            // Load Chart.js library and custom charts
                            script(src = "/static/js/charts.js") {}
                            script {
                                unsafe {
                                    raw(
                                        """
                                        // Initialize charts when DOM is ready
                                        document.addEventListener('DOMContentLoaded', function() {
                                            initializeCharts(portfolioData);
                                        });
                                        """.trimIndent(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}

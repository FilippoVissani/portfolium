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
                            link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css") {}
                            link(rel = "preconnect", href = "https://fonts.googleapis.com")
                            link(rel = "preconnect", href = "https://fonts.gstatic.com") {
                                attributes["crossorigin"] = ""
                            }
                            link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap")
                            style {
                                unsafe {
                                    raw("""
                                        :root {
                                            --color-primary: #2563eb;
                                            --color-primary-dark: #1e40af;
                                            --color-secondary: #7c3aed;
                                            --color-success: #10b981;
                                            --color-warning: #f59e0b;
                                            --color-danger: #ef4444;
                                            --color-info: #06b6d4;
                                            --color-bg-main: #f8fafc;
                                            --color-bg-card: #ffffff;
                                            --color-text-primary: #0f172a;
                                            --color-text-secondary: #64748b;
                                            --color-border: #e2e8f0;
                                            --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
                                            --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                                            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
                                            --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
                                            --radius-sm: 0.375rem;
                                            --radius-md: 0.5rem;
                                            --radius-lg: 0.75rem;
                                            --radius-xl: 1rem;
                                        }
                                        
                                        * { 
                                            margin: 0; 
                                            padding: 0; 
                                            box-sizing: border-box; 
                                        }
                                        
                                        body {
                                            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                            background: var(--color-bg-main);
                                            color: var(--color-text-primary);
                                            padding: 0;
                                            min-height: 100vh;
                                            line-height: 1.6;
                                            -webkit-font-smoothing: antialiased;
                                            -moz-osx-font-smoothing: grayscale;
                                        }
                                        
                                        .header-wrapper {
                                            background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
                                            padding: 3rem 1.5rem 8rem;
                                            margin-bottom: -5rem;
                                            box-shadow: var(--shadow-lg);
                                        }
                                        
                                        .header-content {
                                            max-width: 1400px;
                                            margin: 0 auto;
                                        }
                                        
                                        h1 {
                                            color: white;
                                            text-align: center;
                                            margin-bottom: 0.5rem;
                                            font-size: 2.5rem;
                                            font-weight: 700;
                                            letter-spacing: -0.025em;
                                        }
                                        
                                        .subtitle {
                                            color: rgba(255, 255, 255, 0.9);
                                            text-align: center;
                                            font-size: 1rem;
                                            font-weight: 400;
                                        }
                                        
                                        .container {
                                            max-width: 1400px;
                                            margin: 0 auto;
                                            padding: 0 1.5rem 2rem;
                                        }
                                        
                                        .summary-cards {
                                            display: grid;
                                            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                                            gap: 1.5rem;
                                            margin-bottom: 2rem;
                                        }
                                        
                                        .card {
                                            background: var(--color-bg-card);
                                            padding: 1.75rem;
                                            border-radius: var(--radius-xl);
                                            box-shadow: var(--shadow-lg);
                                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                                            border: 1px solid var(--color-border);
                                            position: relative;
                                            overflow: hidden;
                                        }
                                        
                                        .card::before {
                                            content: '';
                                            position: absolute;
                                            top: 0;
                                            left: 0;
                                            right: 0;
                                            height: 4px;
                                            background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
                                            opacity: 0;
                                            transition: opacity 0.3s ease;
                                        }
                                        
                                        .card:hover {
                                            transform: translateY(-4px);
                                            box-shadow: var(--shadow-xl);
                                        }
                                        
                                        .card:hover::before {
                                            opacity: 1;
                                        }
                                        
                                        .card-header {
                                            display: flex;
                                            align-items: center;
                                            margin-bottom: 1rem;
                                        }
                                        
                                        .card-icon {
                                            width: 3rem;
                                            height: 3rem;
                                            border-radius: var(--radius-lg);
                                            display: flex;
                                            align-items: center;
                                            justify-content: center;
                                            font-size: 1.5rem;
                                            margin-right: 1rem;
                                        }
                                        
                                        .card-icon.primary {
                                            background: linear-gradient(135deg, rgba(37, 99, 235, 0.1), rgba(124, 58, 237, 0.1));
                                            color: var(--color-primary);
                                        }
                                        
                                        .card-icon.success {
                                            background: rgba(16, 185, 129, 0.1);
                                            color: var(--color-success);
                                        }
                                        
                                        .card-icon.info {
                                            background: rgba(6, 182, 212, 0.1);
                                            color: var(--color-info);
                                        }
                                        
                                        .card-icon.warning {
                                            background: rgba(245, 158, 11, 0.1);
                                            color: var(--color-warning);
                                        }
                                        
                                        .card h2 {
                                            color: var(--color-text-primary);
                                            font-size: 0.875rem;
                                            font-weight: 600;
                                            text-transform: uppercase;
                                            letter-spacing: 0.05em;
                                            margin: 0;
                                        }
                                        
                                        .card .value {
                                            font-size: 2rem;
                                            font-weight: 700;
                                            color: var(--color-text-primary);
                                            margin: 0.75rem 0;
                                            line-height: 1.2;
                                        }
                                        
                                        .card .label {
                                            color: var(--color-text-secondary);
                                            font-size: 0.875rem;
                                            margin: 0.375rem 0;
                                            display: flex;
                                            align-items: center;
                                            gap: 0.5rem;
                                        }
                                        
                                        .label-icon {
                                            width: 4px;
                                            height: 4px;
                                            border-radius: 50%;
                                            background: var(--color-text-secondary);
                                        }
                                        
                                        .charts-grid {
                                            display: grid;
                                            grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
                                            gap: 1.5rem;
                                            margin-bottom: 2rem;
                                        }
                                        
                                        @media (max-width: 1024px) {
                                            .charts-grid {
                                                grid-template-columns: 1fr;
                                            }
                                        }
                                        
                                        .chart-container {
                                            background: var(--color-bg-card);
                                            padding: 1.75rem;
                                            border-radius: var(--radius-xl);
                                            box-shadow: var(--shadow-lg);
                                            position: relative;
                                            border: 1px solid var(--color-border);
                                        }
                                        
                                        .chart-title {
                                            color: var(--color-text-primary);
                                            font-size: 1.125rem;
                                            font-weight: 600;
                                            margin-bottom: 1.5rem;
                                            display: flex;
                                            align-items: center;
                                            gap: 0.5rem;
                                        }
                                        
                                        .chart-title i {
                                            color: var(--color-primary);
                                        }
                                        
                                        canvas {
                                            max-height: 320px !important;
                                        }
                                        
                                        .status-good { 
                                            color: var(--color-success);
                                            font-weight: 600;
                                        }
                                        
                                        .status-warning { 
                                            color: var(--color-warning);
                                            font-weight: 600;
                                        }
                                        
                                        .status-bad { 
                                            color: var(--color-danger);
                                            font-weight: 600;
                                        }
                                        
                                        footer {
                                            text-align: center;
                                            color: var(--color-text-secondary);
                                            margin-top: 3rem;
                                            padding: 2rem 1.5rem;
                                            font-size: 0.875rem;
                                            border-top: 1px solid var(--color-border);
                                        }
                                        
                                        .investments-table {
                                            width: 100%;
                                            margin-top: 1.25rem;
                                            border-collapse: separate;
                                            border-spacing: 0;
                                        }
                                        
                                        .investments-table th,
                                        .investments-table td {
                                            padding: 1rem;
                                            text-align: left;
                                        }
                                        
                                        .investments-table thead th {
                                            background: var(--color-bg-main);
                                            font-weight: 600;
                                            color: var(--color-text-primary);
                                            font-size: 0.875rem;
                                            text-transform: uppercase;
                                            letter-spacing: 0.05em;
                                            border-bottom: 2px solid var(--color-border);
                                        }
                                        
                                        .investments-table thead th:first-child {
                                            border-radius: var(--radius-md) 0 0 0;
                                        }
                                        
                                        .investments-table thead th:last-child {
                                            border-radius: 0 var(--radius-md) 0 0;
                                        }
                                        
                                        .investments-table tbody tr {
                                            transition: background-color 0.2s ease;
                                        }
                                        
                                        .investments-table tbody tr:hover {
                                            background: var(--color-bg-main);
                                        }
                                        
                                        .investments-table tbody td {
                                            border-bottom: 1px solid var(--color-border);
                                            font-size: 0.875rem;
                                        }
                                        
                                        .investments-table tbody tr:last-child td:first-child {
                                            border-radius: 0 0 0 var(--radius-md);
                                        }
                                        
                                        .investments-table tbody tr:last-child td:last-child {
                                            border-radius: 0 0 var(--radius-md) 0;
                                        }
                                        
                                        .positive { 
                                            color: var(--color-success);
                                            font-weight: 600;
                                        }
                                        
                                        .negative { 
                                            color: var(--color-danger);
                                            font-weight: 600;
                                        }
                                        
                                        .badge {
                                            display: inline-block;
                                            padding: 0.25rem 0.75rem;
                                            border-radius: 9999px;
                                            font-size: 0.75rem;
                                            font-weight: 600;
                                            letter-spacing: 0.025em;
                                        }
                                        
                                        .badge-success {
                                            background: rgba(16, 185, 129, 0.1);
                                            color: var(--color-success);
                                        }
                                        
                                        .badge-danger {
                                            background: rgba(239, 68, 68, 0.1);
                                            color: var(--color-danger);
                                        }
                                        
                                        @keyframes fadeIn {
                                            from {
                                                opacity: 0;
                                                transform: translateY(20px);
                                            }
                                            to {
                                                opacity: 1;
                                                transform: translateY(0);
                                            }
                                        }
                                        
                                        .card, .chart-container {
                                            animation: fadeIn 0.6s ease-out;
                                        }
                                        
                                        .summary-cards .card:nth-child(1) { animation-delay: 0.1s; }
                                        .summary-cards .card:nth-child(2) { animation-delay: 0.2s; }
                                        .summary-cards .card:nth-child(3) { animation-delay: 0.3s; }
                                        .summary-cards .card:nth-child(4) { animation-delay: 0.4s; }
                                    """.trimIndent())
                                }
                            }
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

                            // Chart.js Scripts
                            script {
                                unsafe {
                                    raw("""
                                        // Chart.js default configuration for professional look
                                        Chart.defaults.font.family = 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
                                        Chart.defaults.color = '#64748b';
                                        Chart.defaults.borderColor = '#e2e8f0';
                                        
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
                                                    backgroundColor: ['#06b6d4', '#10b981'],
                                                    borderWidth: 3,
                                                    borderColor: '#fff',
                                                    hoverOffset: 8
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { 
                                                        position: 'bottom',
                                                        labels: {
                                                            padding: 20,
                                                            font: { size: 13, weight: '500' },
                                                            usePointStyle: true,
                                                            pointStyle: 'circle'
                                                        }
                                                    },
                                                    tooltip: {
                                                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                                                        padding: 12,
                                                        titleFont: { size: 14, weight: '600' },
                                                        bodyFont: { size: 13 },
                                                        borderColor: '#e2e8f0',
                                                        borderWidth: 1,
                                                        callbacks: {
                                                            label: function(context) {
                                                                return context.label + ': ' + context.parsed.toFixed(2) + '%';
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
                                                    backgroundColor: ['#f59e0b', '#10b981', '#06b6d4'],
                                                    borderRadius: 8,
                                                    borderWidth: 0
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { display: false },
                                                    tooltip: {
                                                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                                                        padding: 12,
                                                        titleFont: { size: 14, weight: '600' },
                                                        bodyFont: { size: 13 },
                                                        borderColor: '#e2e8f0',
                                                        borderWidth: 1,
                                                        callbacks: {
                                                            label: function(context) {
                                                                return 'Value: €' + context.parsed.y.toFixed(2);
                                                            }
                                                        }
                                                    }
                                                },
                                                scales: {
                                                    y: { 
                                                        beginAtZero: true,
                                                        grid: {
                                                            color: '#f1f5f9',
                                                            drawBorder: false
                                                        },
                                                        ticks: {
                                                            font: { size: 12 },
                                                            callback: function(value) {
                                                                return '€' + value.toLocaleString();
                                                            }
                                                        }
                                                    },
                                                    x: {
                                                        grid: {
                                                            display: false,
                                                            drawBorder: false
                                                        },
                                                        ticks: {
                                                            font: { size: 12, weight: '500' }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        
                                        ${if (portfolioData.investments.itemsWithWeights.isNotEmpty()) {
                                            val labels = portfolioData.investments.itemsWithWeights.map { it.first.ticker }
                                            val values = portfolioData.investments.itemsWithWeights.map { it.first.currentValue }
                                            val colors = listOf("#2563eb", "#7c3aed", "#ec4899", "#f59e0b", "#10b981", "#06b6d4")
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
                                                        borderWidth: 3,
                                                        borderColor: '#fff',
                                                        hoverOffset: 8
                                                    }]
                                                },
                                                options: {
                                                    responsive: true,
                                                    maintainAspectRatio: true,
                                                    plugins: {
                                                        legend: { 
                                                            position: 'bottom',
                                                            labels: {
                                                                padding: 15,
                                                                font: { size: 12, weight: '500' },
                                                                usePointStyle: true,
                                                                pointStyle: 'circle'
                                                            }
                                                        },
                                                        tooltip: {
                                                            backgroundColor: 'rgba(15, 23, 42, 0.95)',
                                                            padding: 12,
                                                            titleFont: { size: 14, weight: '600' },
                                                            bodyFont: { size: 13 },
                                                            borderColor: '#e2e8f0',
                                                            borderWidth: 1,
                                                            callbacks: {
                                                                label: function(context) {
                                                                    return context.label + ': €' + context.parsed.toFixed(2);
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
                                                    backgroundColor: ['#7c3aed', '#10b981'],
                                                    borderRadius: 8,
                                                    borderWidth: 0
                                                }]
                                            },
                                            options: {
                                                responsive: true,
                                                maintainAspectRatio: true,
                                                plugins: {
                                                    legend: { display: false },
                                                    tooltip: {
                                                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                                                        padding: 12,
                                                        titleFont: { size: 14, weight: '600' },
                                                        bodyFont: { size: 13 },
                                                        borderColor: '#e2e8f0',
                                                        borderWidth: 1,
                                                        callbacks: {
                                                            label: function(context) {
                                                                return 'Amount: €' + context.parsed.y.toFixed(2);
                                                            }
                                                        }
                                                    }
                                                },
                                                scales: {
                                                    y: { 
                                                        beginAtZero: true,
                                                        grid: {
                                                            color: '#f1f5f9',
                                                            drawBorder: false
                                                        },
                                                        ticks: {
                                                            font: { size: 12 },
                                                            callback: function(value) {
                                                                return '€' + value.toLocaleString();
                                                            }
                                                        }
                                                    },
                                                    x: {
                                                        grid: {
                                                            display: false,
                                                            drawBorder: false
                                                        },
                                                        ticks: {
                                                            font: { size: 12, weight: '500' }
                                                        }
                                                    }
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


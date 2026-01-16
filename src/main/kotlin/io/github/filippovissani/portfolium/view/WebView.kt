package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio
import io.github.filippovissani.portfolium.view.html.HtmlHeadGenerator.generateHead
import io.github.filippovissani.portfolium.view.html.JavaScriptDataGenerator
import io.github.filippovissani.portfolium.view.pdf.PdfExporter
import io.github.filippovissani.portfolium.view.sections.EmergencyFundSection
import io.github.filippovissani.portfolium.view.sections.InvestmentsSection
import io.github.filippovissani.portfolium.view.sections.MainBankAccountSection
import io.github.filippovissani.portfolium.view.sections.OverallPerformanceSection
import io.github.filippovissani.portfolium.view.sections.PlannedExpensesSection
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.*
import kotlinx.html.*

/**
 * Web-based view for the portfolio dashboard.
 * Provides an interactive HTML interface with charts and detailed metrics.
 */
object WebView {
    private lateinit var portfolioData: Portfolio

    /**
     * Starts the web server for the portfolio dashboard.
     *
     * @param portfolio The portfolio data to display
     * @param port The port to run the server on (default: 8080)
     */
    fun startServer(portfolio: Portfolio, port: Int = 8080) {
        portfolioData = portfolio

        embeddedServer(Netty, port = port) {
            routing {
                staticResources("/static", "static")

                get("/") {
                    call.respondHtml {
                        head {
                            generateHead()
                        }
                        body {
                            renderHeader()

                            div(classes = "container") {
                                renderSections()
                                renderFooter(port)
                            }

                            renderScripts()
                        }
                    }
                }

                get("/export/pdf") {
                    try {
                        val pdfBytes = PdfExporter.exportToPdf(portfolioData)
                        val filename = "portfolium-dashboard-${java.time.LocalDate.now()}.pdf"
                        call.response.headers.append(
                            HttpHeaders.ContentDisposition,
                            "attachment; filename=\"$filename\""
                        )
                        call.respondBytes(
                            bytes = pdfBytes,
                            contentType = ContentType.Application.Pdf
                        )
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }.start(wait = true)
    }

    private fun BODY.renderHeader() {
        div(classes = "header-wrapper") {
            div(classes = "header-content") {
                h1 { +"Portfolium" }
                div(classes = "subtitle") { +"Professional Personal Finance Dashboard" }
                div(classes = "header-actions") {
                    a(href = "/export/pdf", classes = "export-button") {
                        +"📄 Export as PDF"
                    }
                }
            }
        }
    }

    private fun DIV.renderSections() {
        // Main Bank Account
        with(MainBankAccountSection) {
            render(portfolioData)
        }

        // Planned Expenses
        with(PlannedExpensesSection) {
            render(portfolioData)
        }

        // Emergency Fund
        with(EmergencyFundSection) {
            render(portfolioData)
        }

        // Investments
        with(InvestmentsSection) {
            render(portfolioData)
        }

        // Overall Performance
        with(OverallPerformanceSection) {
            render(portfolioData)
        }
    }

    private fun DIV.renderFooter(port: Int) {
        footer {
            +"Portfolium - Personal Finance Dashboard | Server running on port $port"
        }
    }

    private fun BODY.renderScripts() {
        // Portfolio data for charts
        script {
            unsafe {
                raw(JavaScriptDataGenerator.generatePortfolioData(portfolioData))
            }
        }

        // Chart initialization
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

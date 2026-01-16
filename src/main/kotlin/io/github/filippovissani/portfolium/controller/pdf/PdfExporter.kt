package io.github.filippovissani.portfolium.controller.pdf

import com.lowagie.text.Chunk
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import io.github.filippovissani.portfolium.model.domain.Portfolio
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for exporting portfolio data to PDF format
 */
object PdfExporter {
    private val logger = LoggerFactory.getLogger(PdfExporter::class.java)

    // Font definitions
    private val TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24f, Color.BLACK)
    private val HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f, Color.BLACK)
    private val SUBHEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Color.BLACK)
    private val NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10f, Color.BLACK)
    private val SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8f, Color.GRAY)

    /**
     * Export portfolio to PDF format
     *
     * @param portfolio The portfolio data to export
     * @return ByteArray containing the PDF document
     */
    fun exportToPdf(portfolio: Portfolio): ByteArray {
        logger.info("Generating PDF export for portfolio")
        val outputStream = ByteArrayOutputStream()

        try {
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // Add title
            addTitle(document)

            // Add generation timestamp
            addTimestamp(document)

            // Add summary section
            addSummarySection(document, portfolio)

            // Add liquidity section
            addLiquiditySection(document, portfolio)

            // Add planned expenses section
            addPlannedExpensesSection(document, portfolio)

            // Add emergency fund section
            addEmergencyFundSection(document, portfolio)

            // Add investments section
            addInvestmentsSection(document, portfolio)

            // Add historical performance if available
            if (portfolio.overallHistoricalPerformance != null) {
                addHistoricalPerformanceSection(document, portfolio)
            }

            document.close()
            logger.info("PDF export generated successfully")
        } catch (e: Exception) {
            logger.error("Error generating PDF export", e)
            throw e
        }

        return outputStream.toByteArray()
    }

    private fun addTitle(document: Document) {
        val title = Paragraph("Portfolium Dashboard", TITLE_FONT)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 10f
        document.add(title)

        val subtitle = Paragraph("Professional Personal Finance Dashboard", NORMAL_FONT)
        subtitle.alignment = Element.ALIGN_CENTER
        subtitle.spacingAfter = 20f
        document.add(subtitle)
    }

    private fun addTimestamp(document: Document) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val timestampPara = Paragraph("Generated: $timestamp", SMALL_FONT)
        timestampPara.alignment = Element.ALIGN_RIGHT
        timestampPara.spacingAfter = 20f
        document.add(timestampPara)
    }

    private fun addSummarySection(document: Document, portfolio: Portfolio) {
        val heading = Paragraph("Portfolio Summary", HEADING_FONT)
        heading.spacingBefore = 10f
        heading.spacingAfter = 10f
        document.add(heading)

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f))

        addTableRow(table, "Total Net Worth", formatMoney(portfolio.totalNetWorth), true)
        addTableRow(table, "Invested Capital", formatPercentage(portfolio.percentInvested))
        addTableRow(table, "Liquid Capital", formatPercentage(portfolio.percentLiquid))

        document.add(table)
        document.add(Chunk.NEWLINE)
    }

    private fun addLiquiditySection(document: Document, portfolio: Portfolio) {
        val heading = Paragraph("Liquidity (Main Bank Account)", HEADING_FONT)
        heading.spacingBefore = 10f
        heading.spacingAfter = 10f
        document.add(heading)

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f))

        addTableRow(table, "Total Income", formatMoney(portfolio.liquidity.totalIncome), true)
        addTableRow(table, "Total Expense", formatMoney(portfolio.liquidity.totalExpense))
        addTableRow(table, "Net Balance", formatMoney(portfolio.liquidity.net))
        addTableRow(table, "Avg Monthly Expense (12m)", formatMoney(portfolio.liquidity.avgMonthlyExpense12m))

        document.add(table)
        document.add(Chunk.NEWLINE)
    }

    private fun addPlannedExpensesSection(document: Document, portfolio: Portfolio) {
        val heading = Paragraph("Planned & Predictable Expenses", HEADING_FONT)
        heading.spacingBefore = 10f
        heading.spacingAfter = 10f
        document.add(heading)

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f))

        addTableRow(table, "Total Estimated", formatMoney(portfolio.planned.totalEstimated), true)
        addTableRow(table, "Total Accrued", formatMoney(portfolio.planned.totalAccrued))
        addTableRow(table, "  - Liquid", formatMoney(portfolio.planned.liquidAccrued))
        addTableRow(table, "  - Invested", formatMoney(portfolio.planned.investedAccrued))
        addTableRow(table, "Coverage Ratio", formatPercentage(portfolio.planned.coverageRatio))

        document.add(table)

        // Add historical performance if available
        if (portfolio.planned.historicalPerformance != null) {
            val perf = portfolio.planned.historicalPerformance
            document.add(Chunk.NEWLINE)
            val perfHeading = Paragraph("Performance", SUBHEADING_FONT)
            perfHeading.spacingAfter = 5f
            document.add(perfHeading)

            val perfTable = PdfPTable(2)
            perfTable.widthPercentage = 100f
            perfTable.setWidths(floatArrayOf(3f, 2f))

            addTableRow(perfTable, "Total Return", formatPercentage(perf.totalReturn))
            perf.annualizedReturn?.let {
                addTableRow(perfTable, "Annualized Return", formatPercentage(it))
            }

            document.add(perfTable)
        }

        document.add(Chunk.NEWLINE)
    }

    private fun addEmergencyFundSection(document: Document, portfolio: Portfolio) {
        val heading = Paragraph("Emergency Fund", HEADING_FONT)
        heading.spacingBefore = 10f
        heading.spacingAfter = 10f
        document.add(heading)

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f))

        addTableRow(table, "Target Capital", formatMoney(portfolio.emergency.targetCapital), true)
        addTableRow(table, "Current Capital", formatMoney(portfolio.emergency.currentCapital))
        addTableRow(table, "Delta to Target", formatMoney(portfolio.emergency.deltaToTarget))
        addTableRow(table, "Status", portfolio.emergency.status)
        addTableRow(table, "Type", if (portfolio.emergency.isLiquid) "Liquid" else "Invested")

        document.add(table)

        // Add historical performance if available
        if (portfolio.emergency.historicalPerformance != null) {
            val perf = portfolio.emergency.historicalPerformance
            document.add(Chunk.NEWLINE)
            val perfHeading = Paragraph("Performance", SUBHEADING_FONT)
            perfHeading.spacingAfter = 5f
            document.add(perfHeading)

            val perfTable = PdfPTable(2)
            perfTable.widthPercentage = 100f
            perfTable.setWidths(floatArrayOf(3f, 2f))

            addTableRow(perfTable, "Total Return", formatPercentage(perf.totalReturn))
            perf.annualizedReturn?.let {
                addTableRow(perfTable, "Annualized Return", formatPercentage(it))
            }

            document.add(perfTable)
        }

        document.add(Chunk.NEWLINE)
    }

    private fun addInvestmentsSection(document: Document, portfolio: Portfolio) {
        val heading = Paragraph("Investments (Long Term)", HEADING_FONT)
        heading.spacingBefore = 10f
        heading.spacingAfter = 10f
        document.add(heading)

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f))

        addTableRow(table, "Total Invested", formatMoney(portfolio.investments.totalInvested), true)
        addTableRow(table, "Total Current Value", formatMoney(portfolio.investments.totalCurrent))
        val totalPnl = portfolio.investments.totalCurrent - portfolio.investments.totalInvested
        addTableRow(table, "Total P&L", formatMoney(totalPnl))

        document.add(table)

        // Add breakdown if available
        if (portfolio.investments.itemsWithWeights.isNotEmpty()) {
            document.add(Chunk.NEWLINE)
            val breakdownHeading = Paragraph("Investment Breakdown", SUBHEADING_FONT)
            breakdownHeading.spacingAfter = 5f
            document.add(breakdownHeading)

            val breakdownTable = PdfPTable(4)
            breakdownTable.widthPercentage = 100f
            breakdownTable.setWidths(floatArrayOf(3f, 2f, 2f, 2f))

            // Header row
            addTableRow(breakdownTable, listOf("Asset", "Current Value", "P&L", "Weight"), true)

            // Data rows
            portfolio.investments.itemsWithWeights.forEach { (inv, weight) ->
                val assetName = "${inv.etf} (${inv.ticker})"
                val currentValue = formatMoney(inv.currentValue)
                val pnl = formatMoney(inv.pnl)
                val weightPct = formatPercentage(weight)
                addTableRow(breakdownTable, listOf(assetName, currentValue, pnl, weightPct))
            }

            document.add(breakdownTable)
        }

        // Add historical performance if available
        if (portfolio.historicalPerformance != null) {
            val perf = portfolio.historicalPerformance
            document.add(Chunk.NEWLINE)
            val perfHeading = Paragraph("Performance", SUBHEADING_FONT)
            perfHeading.spacingAfter = 5f
            document.add(perfHeading)

            val perfTable = PdfPTable(2)
            perfTable.widthPercentage = 100f
            perfTable.setWidths(floatArrayOf(3f, 2f))

            addTableRow(perfTable, "Total Return", formatPercentage(perf.totalReturn))
            perf.annualizedReturn?.let {
                addTableRow(perfTable, "Annualized Return", formatPercentage(it))
            }

            document.add(perfTable)
        }

        document.add(Chunk.NEWLINE)
    }

    private fun addHistoricalPerformanceSection(document: Document, portfolio: Portfolio) {
        portfolio.overallHistoricalPerformance?.let { perf ->
            val heading = Paragraph("Overall Portfolio Performance", HEADING_FONT)
            heading.spacingBefore = 10f
            heading.spacingAfter = 10f
            document.add(heading)

            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 2f))

            addTableRow(table, "Total Return", formatPercentage(perf.totalReturn), true)
            perf.annualizedReturn?.let {
                addTableRow(table, "Annualized Return", formatPercentage(it))
            }

            document.add(table)
            document.add(Chunk.NEWLINE)
        }
    }

    private fun addTableRow(table: PdfPTable, label: String, value: String, isHeader: Boolean = false) {
        val font = if (isHeader) SUBHEADING_FONT else NORMAL_FONT
        val labelCell = PdfPCell(Phrase(label, font))
        labelCell.border = PdfPCell.NO_BORDER
        labelCell.paddingBottom = 5f
        table.addCell(labelCell)

        val valueCell = PdfPCell(Phrase(value, font))
        valueCell.border = PdfPCell.NO_BORDER
        valueCell.horizontalAlignment = Element.ALIGN_RIGHT
        valueCell.paddingBottom = 5f
        table.addCell(valueCell)
    }

    private fun addTableRow(table: PdfPTable, values: List<String>, isHeader: Boolean = false) {
        val font = if (isHeader) SUBHEADING_FONT else NORMAL_FONT
        values.forEach { value ->
            val cell = PdfPCell(Phrase(value, font))
            cell.border = if (isHeader) PdfPCell.BOTTOM else PdfPCell.NO_BORDER
            cell.paddingBottom = 5f
            cell.horizontalAlignment = if (isHeader) Element.ALIGN_CENTER else Element.ALIGN_RIGHT
            table.addCell(cell)
        }
    }

    private fun formatMoney(amount: BigDecimal): String {
        return "€${amount.setScale(2, RoundingMode.HALF_UP)}"
    }

    private fun formatPercentage(ratio: BigDecimal): String {
        return "${(ratio * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%"
    }
}

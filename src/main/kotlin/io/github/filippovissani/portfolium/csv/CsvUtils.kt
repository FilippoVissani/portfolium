package io.github.filippovissani.portfolium.csv

import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object CsvUtils {
    private val dateFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,               // 2025-12-31
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // 31/12/2025
        DateTimeFormatter.ofPattern("d/M/yyyy"),       // 1/2/2025
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),     // 12/31/2025
        DateTimeFormatter.ofPattern("M/d/yyyy")        // 1/2/2025
    )

    fun parseDate(value: String?): LocalDate? {
        val v = value?.trim()?.ifEmpty { return null } ?: return null
        for (fmt in dateFormatters) {
            try {
                return LocalDate.parse(v, fmt)
            } catch (_: DateTimeParseException) {
            }
        }
        throw IllegalArgumentException("Cannot parse date: '$v'")
    }

    fun parseBigDecimal(value: String?, default: BigDecimal = BigDecimal.ZERO): BigDecimal {
        val v = value?.trim() ?: return default
        if (v.isEmpty()) return default
        // Support both comma and dot decimals
        val normalized = v.replace(" ", "").replace("€", "").replace(",", ".")
        return normalized.toBigDecimalOrNull() ?: default
    }

    fun File.ensureExists(): File {
        if (!this.exists()) throw IllegalArgumentException("File not found: ${this.absolutePath}")
        return this
    }

    fun BigDecimal.toMoney(scale: Int = 2): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)
}

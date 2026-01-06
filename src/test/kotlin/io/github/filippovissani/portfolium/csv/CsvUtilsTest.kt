package io.github.filippovissani.portfolium.csv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.math.BigDecimal
import java.time.LocalDate

class CsvUtilsTest {
    @Test
    fun parseDate_nullOrEmpty() {
        assertNull(CsvUtils.parseDate(null))
        assertNull(CsvUtils.parseDate(""))
        assertNull(CsvUtils.parseDate("   "))
    }

    @Test
    fun parseBigDecimal_commonCases() {
        fun bd(s: String) = s.toBigDecimal().setScale(2)
        assertEquals(bd("123.45"), CsvUtils.parseBigDecimal("123.45").setScale(2))
        assertEquals(bd("123.45"), CsvUtils.parseBigDecimal("123,45").setScale(2))
        // space as thousands separator (EU style)
        assertEquals(bd("1234.56"), CsvUtils.parseBigDecimal("1 234,56").setScale(2))
        // currency symbol + comma decimals
        assertEquals(bd("123.45"), CsvUtils.parseBigDecimal("€123,45").setScale(2))
        // invalid -> default zero
        assertEquals(BigDecimal.ZERO, CsvUtils.parseBigDecimal("abc"))
    }

    @Test
    fun toMoney_roundsHalfUp() {
        assertEquals(BigDecimal("1.23"), BigDecimal("1.2345").let { CsvUtils.run { it.toMoney(2) } })
        assertEquals(BigDecimal("1.24"), BigDecimal("1.235").let { CsvUtils.run { it.toMoney(2) } })
    }
}

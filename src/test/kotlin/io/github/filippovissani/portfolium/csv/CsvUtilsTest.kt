package io.github.filippovissani.portfolium.csv

import io.github.filippovissani.portfolium.controller.csv.CsvUtils
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class CsvUtilsTest : StringSpec({
    "parseDate returns null for null or empty" {
        CsvUtils.parseDate(null).shouldBeNull()
        CsvUtils.parseDate("").shouldBeNull()
        CsvUtils.parseDate("   ").shouldBeNull()
    }

    "parseBigDecimal common cases" {
        fun bd(s: String) = s.toBigDecimal().setScale(2)
        CsvUtils.parseBigDecimal("123.45").setScale(2) shouldBe bd("123.45")
        CsvUtils.parseBigDecimal("123,45").setScale(2) shouldBe bd("123.45")
        // space as thousands separator (EU style)
        CsvUtils.parseBigDecimal("1 234,56").setScale(2) shouldBe bd("1234.56")
        // currency symbol + comma decimals
        CsvUtils.parseBigDecimal("€123,45").setScale(2) shouldBe bd("123.45")
        // invalid -> default zero
        CsvUtils.parseBigDecimal("abc") shouldBe BigDecimal.ZERO
    }

    "toMoney rounds half up" {
        BigDecimal("1.2345").let { CsvUtils.run { it.toMoney(2) } } shouldBe BigDecimal("1.23")
        BigDecimal("1.235").let { CsvUtils.run { it.toMoney(2) } } shouldBe BigDecimal("1.24")
    }
})

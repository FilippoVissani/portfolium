package io.github.filippovissani.portfolium.model.util

import java.math.BigDecimal
import java.math.RoundingMode

operator fun BigDecimal.plus(other: BigDecimal): BigDecimal = this.add(other)

operator fun BigDecimal.minus(other: BigDecimal): BigDecimal = this.subtract(other)

operator fun BigDecimal.times(other: BigDecimal): BigDecimal = this.multiply(other)

operator fun BigDecimal.div(other: BigDecimal): BigDecimal = this.divide(other, 8, RoundingMode.HALF_UP)

package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.controller.Controller.computePortfolioSummary
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("io.github.filippovissani.portfolium.Main")

fun main() {
    try {
        computePortfolioSummary()
    } catch (e: Exception) {
        logger.error("Error computing portfolio summary", e)
    }
}
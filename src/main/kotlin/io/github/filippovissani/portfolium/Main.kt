package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.controller.Controller.computePortfolioSummary
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("io.github.filippovissani.portfolium.Main")

fun main(args: Array<String>) {
    try {
        computePortfolioSummary(args.getOrNull(0) ?: "data")
    } catch (e: Exception) {
        logger.error("Error computing portfolio summary", e)
    }
}
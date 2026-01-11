package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.controller.Controller.computePortfolioSummary
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("io.github.filippovissani.portfolium.Main")

fun main() {
    // Add shutdown hook to handle Ctrl+C gracefully
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Shutdown signal received (Ctrl+C). Exiting gracefully...")
        exitProcess(0)
    })

    try {
        computePortfolioSummary()
    } catch (e: Exception) {
        logger.error("Error computing portfolio summary", e)
    }
}

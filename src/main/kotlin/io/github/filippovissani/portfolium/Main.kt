package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.controller.Controller.computePortfolioSummary

fun main(args: Array<String>) {
    try {
        computePortfolioSummary(args.getOrNull(0) ?: "data")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
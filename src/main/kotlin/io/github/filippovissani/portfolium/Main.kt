package io.github.filippovissani.portfolium

import io.github.filippovissani.portfolium.controller.Controller.computePortfolioSummary

fun main(args: Array<String>) {
    computePortfolioSummary(args.getOrNull(0) ?: "data")
}
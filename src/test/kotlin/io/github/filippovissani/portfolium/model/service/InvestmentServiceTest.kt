package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.Investment
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class InvestmentServiceTest :
    FunSpec({

        test("calculateInvestmentsSummary should calculate totals from investments") {
            val investments =
                listOf(
                    Investment(
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        averagePrice = BigDecimal("400.00"),
                        currentPrice = BigDecimal("450.00"),
                    ),
                    Investment(
                        etf = "World ETF",
                        ticker = "VT",
                        area = "World",
                        quantity = BigDecimal("5"),
                        averagePrice = BigDecimal("100.00"),
                        currentPrice = BigDecimal("110.00"),
                    ),
                )

            val summary = InvestmentService.calculateInvestmentsSummary(investments)

            // Total invested = (10 * 400) + (5 * 100) = 4500
            summary.totalInvested shouldBe BigDecimal("4500.00")
            // Total current = (10 * 450) + (5 * 110) = 5050
            summary.totalCurrent shouldBe BigDecimal("5050.00")
            summary.itemsWithWeights.size shouldBe 2
        }

        test("calculateInvestmentsSummary should calculate weights correctly") {
            val investments =
                listOf(
                    Investment(
                        etf = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        averagePrice = BigDecimal("100.00"),
                        currentPrice = BigDecimal("150.00"),
                    ),
                    Investment(
                        etf = "World ETF",
                        ticker = "VT",
                        area = "World",
                        quantity = BigDecimal("5"),
                        averagePrice = BigDecimal("100.00"),
                        currentPrice = BigDecimal("50.00"),
                    ),
                )

            val summary = InvestmentService.calculateInvestmentsSummary(investments)

            // Total current = (10 * 150) + (5 * 50) = 1750
            // Weight SPY = 1500 / 1750 = 0.857142...
            // Weight VT = 250 / 1750 = 0.142857...
            val spyWeight = summary.itemsWithWeights[0].second
            val vtWeight = summary.itemsWithWeights[1].second

            spyWeight.compareTo(BigDecimal("0.85")) shouldBe 1
            spyWeight.compareTo(BigDecimal("0.86")) shouldBe -1
            vtWeight.compareTo(BigDecimal("0.14")) shouldBe 1
            vtWeight.compareTo(BigDecimal("0.15")) shouldBe -1
        }

        test("calculateInvestmentsSummary should handle empty investments") {
            val summary = InvestmentService.calculateInvestmentsSummary(emptyList())

            summary.totalInvested shouldBe BigDecimal("0.00")
            summary.totalCurrent shouldBe BigDecimal("0.00")
            summary.itemsWithWeights shouldBe emptyList()
        }

        test("calculateInvestmentsSummary should work with InvestmentBankAccount") {
            val account =
                InvestmentBankAccount(
                    name = "Investment Account",
                    initialBalance = BigDecimal("1000.00"),
                    transactions = emptyList(),
                )

            val currentPrices =
                mapOf(
                    "SPY" to BigDecimal("450.00"),
                    "VT" to BigDecimal("110.00"),
                )

            val summary = InvestmentService.calculateInvestmentsSummary(account, currentPrices)

            // Empty account should have zero values
            summary.totalInvested shouldBe BigDecimal("0.00")
            summary.totalCurrent shouldBe BigDecimal("0.00")
        }
    })

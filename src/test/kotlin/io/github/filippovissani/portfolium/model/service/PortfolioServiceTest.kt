package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.EmergencyFundSummary
import io.github.filippovissani.portfolium.model.domain.InvestmentsSummary
import io.github.filippovissani.portfolium.model.domain.LiquiditySummary
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.math.RoundingMode

class PortfolioServiceTest :
    FunSpec({

        test("buildPortfolio should calculate total net worth correctly") {
            val liquidity =
                LiquiditySummary(
                    totalIncome = BigDecimal("10000.00"),
                    totalExpense = BigDecimal("6000.00"),
                    net = BigDecimal("4000.00"),
                    avgMonthlyExpense12m = BigDecimal("500.00"),
                    statistics = null,
                )

            val planned =
                PlannedExpensesSummary(
                    totalEstimated = BigDecimal("20000.00"),
                    totalAccrued = BigDecimal("5000.00"),
                    coverageRatio = BigDecimal("0.25"),
                    liquidAccrued = BigDecimal("3000.00"),
                    investedAccrued = BigDecimal("2000.00"),
                    isInvested = true,
                )

            val emergency =
                EmergencyFundSummary(
                    targetCapital = BigDecimal("3000.00"),
                    currentCapital = BigDecimal("3000.00"),
                    deltaToTarget = BigDecimal.ZERO,
                    status = "OK",
                    isLiquid = true,
                )

            val investments =
                InvestmentsSummary(
                    totalInvested = BigDecimal("8000.00"),
                    totalCurrent = BigDecimal("10000.00"),
                    itemsWithWeights = emptyList(),
                )

            val portfolio = PortfolioService.buildPortfolio(liquidity, planned, emergency, investments)

            // Liquid = 4000 (net) + 3000 (planned liquid) + 3000 (emergency liquid) = 10000
            // Invested = 10000 (investments) + 2000 (planned invested) = 12000
            // Total = 22000
            portfolio.totalNetWorth shouldBe BigDecimal("22000.00")
        }

        test("buildPortfolio should calculate percentages correctly") {
            val liquidity =
                LiquiditySummary(
                    totalIncome = BigDecimal.ZERO,
                    totalExpense = BigDecimal.ZERO,
                    net = BigDecimal("5000.00"),
                    avgMonthlyExpense12m = BigDecimal.ZERO,
                )

            val planned =
                PlannedExpensesSummary(
                    totalEstimated = BigDecimal.ZERO,
                    totalAccrued = BigDecimal.ZERO,
                    coverageRatio = BigDecimal.ZERO,
                    liquidAccrued = BigDecimal.ZERO,
                    investedAccrued = BigDecimal.ZERO,
                    isInvested = false,
                )

            val emergency =
                EmergencyFundSummary(
                    targetCapital = BigDecimal.ZERO,
                    currentCapital = BigDecimal.ZERO,
                    deltaToTarget = BigDecimal.ZERO,
                    status = "OK",
                    isLiquid = true,
                )

            val investments =
                InvestmentsSummary(
                    totalInvested = BigDecimal("10000.00"),
                    totalCurrent = BigDecimal("15000.00"),
                    itemsWithWeights = emptyList(),
                )

            val portfolio = PortfolioService.buildPortfolio(liquidity, planned, emergency, investments)

            // Total = 5000 (liquid) + 15000 (invested) = 20000
            // Percent invested = 15000 / 20000 = 0.75
            // Percent liquid = 0.25
            portfolio.percentInvested.setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal("0.75")
            portfolio.percentLiquid.setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal("0.25")
        }

        test("buildPortfolio should handle invested emergency fund") {
            val liquidity =
                LiquiditySummary(
                    totalIncome = BigDecimal.ZERO,
                    totalExpense = BigDecimal.ZERO,
                    net = BigDecimal("1000.00"),
                    avgMonthlyExpense12m = BigDecimal.ZERO,
                )

            val planned =
                PlannedExpensesSummary(
                    totalEstimated = BigDecimal.ZERO,
                    totalAccrued = BigDecimal.ZERO,
                    coverageRatio = BigDecimal.ZERO,
                    liquidAccrued = BigDecimal.ZERO,
                    investedAccrued = BigDecimal.ZERO,
                    isInvested = false,
                )

            val emergency =
                EmergencyFundSummary(
                    targetCapital = BigDecimal("5000.00"),
                    currentCapital = BigDecimal("5000.00"),
                    deltaToTarget = BigDecimal.ZERO,
                    status = "OK",
                    isLiquid = false, // Invested emergency fund
                )

            val investments =
                InvestmentsSummary(
                    totalInvested = BigDecimal.ZERO,
                    totalCurrent = BigDecimal.ZERO,
                    itemsWithWeights = emptyList(),
                )

            val portfolio = PortfolioService.buildPortfolio(liquidity, planned, emergency, investments)

            // Liquid = 1000 (net only, emergency not counted)
            // Invested = 5000 (emergency fund)
            // Total = 6000
            portfolio.totalNetWorth shouldBe BigDecimal("6000.00")
            portfolio.percentInvested.setScale(4, RoundingMode.HALF_UP) shouldBe BigDecimal("0.8333")
        }

        test("buildPortfolio should handle zero net worth") {
            val liquidity =
                LiquiditySummary(
                    totalIncome = BigDecimal.ZERO,
                    totalExpense = BigDecimal.ZERO,
                    net = BigDecimal.ZERO,
                    avgMonthlyExpense12m = BigDecimal.ZERO,
                )

            val planned =
                PlannedExpensesSummary(
                    totalEstimated = BigDecimal.ZERO,
                    totalAccrued = BigDecimal.ZERO,
                    coverageRatio = BigDecimal.ZERO,
                    liquidAccrued = BigDecimal.ZERO,
                    investedAccrued = BigDecimal.ZERO,
                    isInvested = false,
                )

            val emergency =
                EmergencyFundSummary(
                    targetCapital = BigDecimal.ZERO,
                    currentCapital = BigDecimal.ZERO,
                    deltaToTarget = BigDecimal.ZERO,
                    status = "OK",
                    isLiquid = true,
                )

            val investments =
                InvestmentsSummary(
                    totalInvested = BigDecimal.ZERO,
                    totalCurrent = BigDecimal.ZERO,
                    itemsWithWeights = emptyList(),
                )

            val portfolio = PortfolioService.buildPortfolio(liquidity, planned, emergency, investments)

            portfolio.totalNetWorth shouldBe BigDecimal("0.00")
            portfolio.percentInvested shouldBe BigDecimal.ZERO
            portfolio.percentLiquid shouldBe BigDecimal.ONE
        }
    })


package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.model.domain.DepositTransaction
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class EmergencyFundServiceTest :
    FunSpec({

        test("calculateEmergencyFundSummary should calculate target capital") {
            val account =
                EmergencyFundBankAccount(
                    name = "Emergency Fund",
                    initialBalance = BigDecimal("3000.00"),
                    transactions = emptyList(),
                    targetMonthlyExpenses = 6,
                )

            val avgMonthlyExpense = BigDecimal("800.00")

            val summary = EmergencyFundService.calculateEmergencyFundSummary(account, avgMonthlyExpense)

            // Target = 800 * 6 = 4800
            summary.targetCapital shouldBe BigDecimal("4800.00")
            summary.currentCapital shouldBe BigDecimal("3000.00")
            summary.deltaToTarget shouldBe BigDecimal("1800.00")
            summary.status shouldBe "BELOW TARGET"
        }

        test("calculateEmergencyFundSummary should show OK status when target is met") {
            val account =
                EmergencyFundBankAccount(
                    name = "Emergency Fund",
                    initialBalance = BigDecimal("5000.00"),
                    transactions = emptyList(),
                    targetMonthlyExpenses = 6,
                )

            val avgMonthlyExpense = BigDecimal("800.00")

            val summary = EmergencyFundService.calculateEmergencyFundSummary(account, avgMonthlyExpense)

            // Target = 800 * 6 = 4800, current = 5000
            summary.currentCapital shouldBe BigDecimal("5000.00")
            summary.deltaToTarget shouldBe BigDecimal("-200.00") // Negative means surplus
            summary.status shouldBe "OK"
        }

        test("calculateEmergencyFundSummary should detect if fund is invested") {
            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("5000.00"),
                        description = "Initial deposit",
                    ),
                    EtfBuyTransaction(
                        date = LocalDate.of(2025, 1, 15),
                        name = "S&P 500 ETF",
                        ticker = "SPY",
                        area = "US",
                        quantity = BigDecimal("10"),
                        price = BigDecimal("450.00"),
                        fees = BigDecimal("5.00"),
                    ),
                )

            val account =
                EmergencyFundBankAccount(
                    name = "Emergency Fund",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                    targetMonthlyExpenses = 6,
                )

            val summary = EmergencyFundService.calculateEmergencyFundSummary(account, BigDecimal("800.00"))

            summary.isLiquid shouldBe false
        }

        test("calculateEmergencyFundSummary should detect if fund is liquid") {
            val transactions =
                listOf(
                    DepositTransaction(
                        date = LocalDate.of(2025, 1, 1),
                        amount = BigDecimal("5000.00"),
                        description = "Initial deposit",
                    ),
                )

            val account =
                EmergencyFundBankAccount(
                    name = "Emergency Fund",
                    initialBalance = BigDecimal.ZERO,
                    transactions = transactions,
                    targetMonthlyExpenses = 6,
                )

            val summary = EmergencyFundService.calculateEmergencyFundSummary(account, BigDecimal("800.00"))

            summary.isLiquid shouldBe true
        }

        test("calculateEmergencyFundSummary should handle empty account") {
            val account = EmergencyFundBankAccount()
            val summary = EmergencyFundService.calculateEmergencyFundSummary(account, BigDecimal.ZERO)

            summary.targetCapital shouldBe BigDecimal("0.00")
            summary.currentCapital shouldBe BigDecimal("0.00")
            summary.deltaToTarget shouldBe BigDecimal("0.00")
            summary.isLiquid shouldBe true
        }
    })


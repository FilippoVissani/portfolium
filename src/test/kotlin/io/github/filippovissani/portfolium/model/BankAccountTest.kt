package io.github.filippovissani.portfolium.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.maps.shouldHaveSize
import java.math.BigDecimal
import java.time.LocalDate

class BankAccountTest : FunSpec({

    test("MainBankAccount should calculate current balance correctly") {
        val account = MainBankAccount(
            name = "Test Main Account",
            initialBalance = BigDecimal("1000.00"),
            transactions = listOf(
                LiquidTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    description = "Salary",
                    category = "Income",
                    amount = BigDecimal("500.00"),
                    note = "Monthly salary"
                ),
                LiquidTransaction(
                    date = LocalDate.of(2025, 1, 2),
                    description = "Groceries",
                    category = "Food",
                    amount = BigDecimal("-200.00"),
                    note = null
                )
            )
        )

        account.currentBalance.compareTo(BigDecimal("1300.00")) shouldBe 0
        account.totalIncome.compareTo(BigDecimal("500.00")) shouldBe 0
        account.totalExpenses.compareTo(BigDecimal("200.00")) shouldBe 0
    }

    test("InvestmentBankAccount should calculate balance with ETF buy transactions") {
        val account = InvestmentBankAccount(
            name = "Test Investment Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00"),
                )
            )
        )

        // Balance = 10000 - (10 * 450 + 5) = 10000 - 4505 = 5495
        account.currentBalance.compareTo(BigDecimal("5495.00")) shouldBe 0
    }

    test("InvestmentBankAccount should calculate balance with ETF sell transactions") {
        val account = InvestmentBankAccount(
            name = "Test Investment Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00"),
                ),
                EtfSellTransaction(
                    date = LocalDate.of(2025, 1, 20),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("5"),
                    price = BigDecimal("455.00"),
                    fees = BigDecimal("2.50"),
                )
            )
        )

        // Buy: 10000 - 4505 = 5495
        // Sell: 5495 + (5 * 455 - 2.50) = 5495 + 2272.50 = 7767.50
        account.currentBalance.compareTo(BigDecimal("7767.50")) shouldBe 0
    }

    test("InvestmentBankAccount should track ETF holdings correctly") {
        val account = InvestmentBankAccount(
            name = "Test Investment Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00"),
                ),
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 15),
                    name = "MSCI World",
                    ticker = "IWDA",
                    area = "World",
                    quantity = BigDecimal("20"),
                    price = BigDecimal("80.00"),
                    fees = BigDecimal("3.00"),
                ),
                EtfSellTransaction(
                    date = LocalDate.of(2025, 1, 20),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("5"),
                    price = BigDecimal("455.00"),
                    fees = BigDecimal("2.50"),
                )
            )
        )

        val holdings = account.etfHoldings
        holdings shouldHaveSize 2

        val vooHolding = holdings["VOO"]
        vooHolding shouldNotBe null
        vooHolding!!.quantity.compareTo(BigDecimal("5")) shouldBe 0
        vooHolding.ticker shouldBe "VOO"

        val iwdaHolding = holdings["IWDA"]
        iwdaHolding shouldNotBe null
        iwdaHolding!!.quantity.compareTo(BigDecimal("20")) shouldBe 0
        iwdaHolding.ticker shouldBe "IWDA"
    }

    test("InvestmentBankAccount should calculate average price for ETF holdings") {
        val account = InvestmentBankAccount(
            name = "Test Investment Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("10.00"),
                ),
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 15),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("460.00"),
                    fees = BigDecimal("10.00"),
                )
            )
        )

        val holdings = account.etfHoldings
        val vooHolding = holdings["VOO"]
        vooHolding shouldNotBe null
        vooHolding!!.quantity.compareTo(BigDecimal("20")) shouldBe 0

        // Total cost = (10 * 450 + 10) + (10 * 460 + 10) = 4510 + 4610 = 9120
        // Average price = 9120 / 20 = 456
        vooHolding.averagePrice.compareTo(BigDecimal("456.00")) shouldBe 0
    }

    test("PlannedExpensesBankAccount should track transactions and planned expenses") {
        val account = PlannedExpensesBankAccount(
            name = "Planned Expenses",
            initialBalance = BigDecimal.ZERO,
            transactions = listOf(
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("500.00"),
                    description = "Initial"
                ),
                DepositTransaction(
                    date = LocalDate.of(2025, 2, 1),
                    amount = BigDecimal("300.00"),
                    description = "Monthly"
                )
            ),
            plannedExpenses = listOf(
                PlannedExpenseEntry("Car", LocalDate.of(2026, 12, 31), BigDecimal("15000.00")),
                PlannedExpenseEntry("Vacation", LocalDate.of(2025, 8, 1), BigDecimal("3000.00"))
            )
        )

        account.currentBalance.compareTo(BigDecimal("800.00")) shouldBe 0
        account.plannedExpenses.size shouldBe 2
    }

    test("EmergencyFundBankAccount should track target and balance") {
        val account = EmergencyFundBankAccount(
            name = "Emergency Fund",
            initialBalance = BigDecimal.ZERO,
            transactions = listOf(
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("5000.00"),
                    description = "Initial"
                ),
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 3, 1),
                    amount = BigDecimal("500.00"),
                    description = "Emergency"
                )
            ),
            targetMonthlyExpenses = 6
        )

        account.currentBalance.compareTo(BigDecimal("4500.00")) shouldBe 0
        account.targetMonthlyExpenses shouldBe 6
    }
})


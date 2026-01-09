package io.github.filippovissani.portfolium.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.maps.shouldHaveSize
import java.math.BigDecimal
import java.time.LocalDate

class BankAccountTest : FunSpec({

    test("BankAccount should calculate current balance correctly with deposits and withdrawals") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("1000.00"),
            transactions = listOf(
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("500.00"),
                    description = "Deposit"
                ),
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 1, 2),
                    amount = BigDecimal("200.00"),
                    description = "Withdrawal"
                )
            )
        )

        account.currentBalance.compareTo(BigDecimal("1300.00")) shouldBe 0
    }

    test("BankAccount should calculate balance with ETF buy transactions") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00")
                )
            )
        )

        // Balance = 10000 - (10 * 450 + 5) = 10000 - 4505 = 5495
        account.currentBalance.compareTo(BigDecimal("5495.00")) shouldBe 0
    }

    test("BankAccount should calculate balance with ETF sell transactions") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00")
                ),
                EtfSellTransaction(
                    date = LocalDate.of(2025, 1, 20),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("5"),
                    price = BigDecimal("455.00"),
                    fees = BigDecimal("2.50")
                )
            )
        )

        // Buy: 10000 - 4505 = 5495
        // Sell: 5495 + (5 * 455 - 2.50) = 5495 + 2272.50 = 7767.50
        account.currentBalance.compareTo(BigDecimal("7767.50")) shouldBe 0
    }

    test("BankAccount should track ETF holdings correctly") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00")
                ),
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 15),
                    name = "MSCI World",
                    ticker = "IWDA",
                    area = "World",
                    quantity = BigDecimal("20"),
                    price = BigDecimal("80.00"),
                    fees = BigDecimal("3.00")
                ),
                EtfSellTransaction(
                    date = LocalDate.of(2025, 1, 20),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("5"),
                    price = BigDecimal("455.00"),
                    fees = BigDecimal("2.50")
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

    test("BankAccount should calculate average price for ETF holdings") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("10000.00"),
            transactions = listOf(
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("10.00")
                ),
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 15),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("10"),
                    price = BigDecimal("460.00"),
                    fees = BigDecimal("10.00")
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

    test("BankAccountService should calculate total deposits") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("1000.00"),
            transactions = listOf(
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("500.00")
                ),
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 5),
                    amount = BigDecimal("300.00")
                ),
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    amount = BigDecimal("100.00")
                )
            )
        )

        BankAccountService.getTotalDeposits(account).compareTo(BigDecimal("800.00")) shouldBe 0
    }

    test("BankAccountService should calculate total withdrawals") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("1000.00"),
            transactions = listOf(
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("200.00")
                ),
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 1, 5),
                    amount = BigDecimal("150.00")
                ),
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    amount = BigDecimal("500.00")
                )
            )
        )

        BankAccountService.getTotalWithdrawals(account).compareTo(BigDecimal("350.00")) shouldBe 0
    }

    test("BankAccountService should provide account summary") {
        val account = BankAccount(
            name = "Test Account",
            initialBalance = BigDecimal("1000.00"),
            transactions = listOf(
                DepositTransaction(
                    date = LocalDate.of(2025, 1, 1),
                    amount = BigDecimal("500.00")
                ),
                WithdrawalTransaction(
                    date = LocalDate.of(2025, 1, 2),
                    amount = BigDecimal("200.00")
                ),
                EtfBuyTransaction(
                    date = LocalDate.of(2025, 1, 10),
                    name = "S&P 500",
                    ticker = "VOO",
                    area = "US",
                    quantity = BigDecimal("5"),
                    price = BigDecimal("450.00"),
                    fees = BigDecimal("5.00")
                )
            )
        )

        val summary = BankAccountService.getAccountSummary(account)
        summary.name shouldBe "Test Account"
        summary.initialBalance.compareTo(BigDecimal("1000.00")) shouldBe 0
        summary.totalDeposits.compareTo(BigDecimal("500.00")) shouldBe 0
        summary.totalWithdrawals.compareTo(BigDecimal("200.00")) shouldBe 0
        summary.numberOfTransactions shouldBe 3
        summary.etfHoldings shouldHaveSize 1
    }
})


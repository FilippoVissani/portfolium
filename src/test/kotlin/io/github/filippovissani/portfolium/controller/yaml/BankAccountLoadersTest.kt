package io.github.filippovissani.portfolium.controller.yaml

import io.github.filippovissani.portfolium.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File
import java.math.BigDecimal

class BankAccountLoadersTest : FunSpec({

    test("loadMainBankAccount parses YAML into MainBankAccount") {
        val yaml = """
            name: Main Account
            initialBalance: 1000.00
            transactions:
              - date: 2025-01-01
                description: Salary
                category: Income
                amount: 500.00
                note: Monthly salary
              - date: 2025-01-02
                description: Groceries
                category: Food
                amount: -200.00
          """.trimIndent()

        val tmp = createTempYaml(yaml)
        val account = BankAccountLoaders.loadMainBankAccount(tmp)

        account.name shouldBe "Main Account"
        account.initialBalance.compareTo(BigDecimal("1000.00")) shouldBe 0
        account.transactions.size shouldBe 2
        account.transactions[0] shouldBe LiquidTransaction(
            date = java.time.LocalDate.of(2025, 1, 1),
            description = "Salary",
            category = "Income",
            amount = BigDecimal("500.00"),
            note = "Monthly salary"
        )
        // currentBalance = 1000 + 500 - 200 = 1300
        account.currentBalance.compareTo(BigDecimal("1300.00")) shouldBe 0
    }

    test("loadPlannedExpensesBankAccount parses YAML with planned expenses and transactions") {
        val yaml = """
            name: Planned Expenses
            initialBalance: 0.00
            transactions:
              - type: deposit
                date: 2025-01-01
                amount: 500.00
                description: Initial deposit
              - type: withdrawal
                date: 2025-02-01
                amount: 100.00
                description: Use funds
            plannedExpenses:
              - name: Car
                expirationDate: 2026-12-31
                estimatedAmount: 15000.00
              - name: Vacation
                expirationDate: 2025-08-01
                estimatedAmount: 3000.00
          """.trimIndent()

        val tmp = createTempYaml(yaml)
        val account = BankAccountLoaders.loadPlannedExpensesBankAccount(tmp)

        account.name shouldBe "Planned Expenses"
        account.transactions.size shouldBe 2
        account.transactions[0] shouldBe DepositTransaction(
            date = java.time.LocalDate.of(2025, 1, 1),
            amount = BigDecimal("500.00"),
            description = "Initial deposit"
        )
        account.transactions[1] shouldBe WithdrawalTransaction(
            date = java.time.LocalDate.of(2025, 2, 1),
            amount = BigDecimal("100.00"),
            description = "Use funds"
        )
        account.plannedExpenses.size shouldBe 2
        account.plannedExpenses[0] shouldBe PlannedExpenseEntry(
            name = "Car",
            expirationDate = java.time.LocalDate.of(2026, 12, 31),
            estimatedAmount = BigDecimal("15000.00")
        )
        account.currentBalance.compareTo(BigDecimal("400.00")) shouldBe 0
    }

    test("loadEmergencyFundBankAccount parses YAML with target Monthly Expenses") {
        val yaml = """
            name: Emergency Fund
            initialBalance: 0.00
            targetMonthlyExpenses: 6
            transactions:
              - type: deposit
                date: 2025-01-01
                amount: 5000.00
                description: Initial deposit
              - type: withdrawal
                date: 2025-03-01
                amount: 500.00
                description: Emergency
          """.trimIndent()

        val tmp = createTempYaml(yaml)
        val account = BankAccountLoaders.loadEmergencyFundBankAccount(tmp)

        account.name shouldBe "Emergency Fund"
        account.targetMonthlyExpenses shouldBe 6
        account.transactions.size shouldBe 2
        account.currentBalance.compareTo(BigDecimal("4500.00")) shouldBe 0
    }

    test("loadInvestmentBankAccount parses deposits and ETF transactions") {
        val yaml = """
            name: Investments
            initialBalance: 10000.00
            transactions:
              - type: deposit
                date: 2025-01-01
                amount: 1000.00
                description: top-up
              - type: etf_buy
                date: 2025-01-10
                name: S&P 500
                ticker: VOO
                area: US
                quantity: 10
                price: 450.00
                fees: 5.00
              - type: etf_sell
                date: 2025-01-20
                name: S&P 500
                ticker: VOO
                area: US
                quantity: 5
                price: 455.00
                fees: 2.50
          """.trimIndent()

        val tmp = createTempYaml(yaml)
        val account = BankAccountLoaders.loadInvestmentBankAccount(tmp)

        account.name shouldBe "Investments"
        account.transactions.size shouldBe 3
        // Verify mapping of ETF buy
        val buy = account.transactions[1] as EtfBuyTransaction
        buy.ticker shouldBe "VOO"
        buy.quantity.compareTo(BigDecimal("10")) shouldBe 0
        buy.price.compareTo(BigDecimal("450.00")) shouldBe 0
        buy.fees shouldNotBe null
        // Current balance calc: 10000 + 1000 - (10*450+5) + (5*455-2.50) = 8767.50
        account.currentBalance.compareTo(BigDecimal("8767.50")) shouldBe 0
    }

    test("missing file returns default accounts") {
        BankAccountLoaders.loadMainBankAccount(File("/non/existent/file-main.yaml")).name shouldBe "Main Account"
        BankAccountLoaders.loadPlannedExpensesBankAccount(File("/non/existent/file-planned.yaml")).name shouldBe "Planned Expenses"
        BankAccountLoaders.loadEmergencyFundBankAccount(File("/non/existent/file-em.yaml")).name shouldBe "Emergency Fund"
        BankAccountLoaders.loadInvestmentBankAccount(File("/non/existent/file-inv.yaml")).name shouldBe "Investments"
    }
})

private fun createTempYaml(content: String): File {
    val file = kotlin.io.path.createTempFile("bank-account-test", ".yaml").toFile()
    file.writeText(content)
    file.deleteOnExit()
    return file
}

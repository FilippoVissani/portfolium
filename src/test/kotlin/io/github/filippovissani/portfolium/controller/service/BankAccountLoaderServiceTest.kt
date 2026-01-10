package io.github.filippovissani.portfolium.controller.service

import io.github.filippovissani.portfolium.controller.config.Config
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.github.filippovissani.portfolium.model.domain.MainBankAccount
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File

class BankAccountLoaderServiceTest :
    FunSpec({

        test("loadMainBankAccount should return empty account when file does not exist") {
            val config =
                Config(
                    dataPath = "nonexistent",
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadMainBankAccount(config)

            account.shouldBeInstanceOf<MainBankAccount>()
            account.transactions shouldBe emptyList()
        }

        test("loadPlannedExpensesBankAccount should return empty account when file does not exist") {
            val config =
                Config(
                    dataPath = "nonexistent",
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadPlannedExpensesBankAccount(config)

            account.shouldBeInstanceOf<PlannedExpensesBankAccount>()
            account.transactions shouldBe emptyList()
            account.plannedExpenses shouldBe emptyList()
        }

        test("loadEmergencyFundBankAccount should return empty account when file does not exist") {
            val config =
                Config(
                    dataPath = "nonexistent",
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadEmergencyFundBankAccount(config)

            account.shouldBeInstanceOf<EmergencyFundBankAccount>()
            account.transactions shouldBe emptyList()
        }

        test("loadInvestmentBankAccount should return empty account when file does not exist") {
            val config =
                Config(
                    dataPath = "nonexistent",
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadInvestmentBankAccount(config)

            account.shouldBeInstanceOf<InvestmentBankAccount>()
            account.transactions shouldBe emptyList()
        }

        test("loadMainBankAccount should handle invalid YAML file gracefully") {
            val tempDir =
                File.createTempFile("test_data", "").apply {
                    delete()
                    mkdir()

                    deleteOnExit()
                }

            val invalidYamlFile = File(tempDir, "invalid_main.yaml")
            invalidYamlFile.writeText("invalid: yaml: content: : :")
            invalidYamlFile.deleteOnExit()

            val config =
                Config(
                    dataPath = tempDir.absolutePath,
                    mainBankAccountFile = "invalid_main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadMainBankAccount(config)

            // Should return empty account on error
            account.shouldBeInstanceOf<MainBankAccount>()
            tempDir.deleteRecursively()
        }

        test("loadPlannedExpensesBankAccount should handle invalid YAML file gracefully") {
            val tempDir =
                File.createTempFile("test_data", "").apply {
                    delete()
                    mkdir()
                    deleteOnExit()
                }

            val invalidYamlFile = File(tempDir, "invalid_planned.yaml")
            invalidYamlFile.writeText("invalid: yaml: content:")
            invalidYamlFile.deleteOnExit()

            val config =
                Config(
                    dataPath = tempDir.absolutePath,
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "invalid_planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 24,
                    historicalPerformanceIntervalDays = 7,
                    serverPort = 8080,
                )

            val account = BankAccountLoaderService.loadPlannedExpensesBankAccount(config)

            // Should return empty account on error
            account.shouldBeInstanceOf<PlannedExpensesBankAccount>()
            tempDir.deleteRecursively()
        }
    })

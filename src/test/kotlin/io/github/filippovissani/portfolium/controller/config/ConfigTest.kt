package io.github.filippovissani.portfolium.controller.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class ConfigTest :
    FunSpec({

        test("Config should build correct paths") {
            val config =
                Config(
                    dataPath = "data",
                    mainBankAccountFile = "main_bank_account.yaml",
                    plannedExpensesBankAccountFile = "planned_expenses_bank_account.yaml",
                    emergencyFundBankAccountFile = "emergency_fund_bank_account.yaml",
                    investmentBankAccountFile = "investment_bank_account.yaml",
                    priceCacheFile = "price_cache.csv",
                    cacheDurationHours = 24L,
                    historicalPerformanceIntervalDays = 7L,
                    serverPort = 8080,
                )

            config.getPriceCachePath() shouldBe File("data/price_cache.csv")
            config.getMainBankAccountPath() shouldBe File("data/main_bank_account.yaml")
            config.getPlannedExpensesBankAccountPath() shouldBe File("data/planned_expenses_bank_account.yaml")
            config.getEmergencyFundBankAccountPath() shouldBe File("data/emergency_fund_bank_account.yaml")
            config.getInvestmentBankAccountPath() shouldBe File("data/investment_bank_account.yaml")
        }

        test("Config should handle custom data path") {
            val config =
                Config(
                    dataPath = "custom/path",
                    mainBankAccountFile = "main.yaml",
                    plannedExpensesBankAccountFile = "planned.yaml",
                    emergencyFundBankAccountFile = "emergency.yaml",
                    investmentBankAccountFile = "investment.yaml",
                    priceCacheFile = "cache.csv",
                    cacheDurationHours = 48L,
                    historicalPerformanceIntervalDays = 14L,
                    serverPort = 9090,
                )

            config.getPriceCachePath() shouldBe File("custom/path/cache.csv")
            config.getMainBankAccountPath() shouldBe File("custom/path/main.yaml")
            config.serverPort shouldBe 9090
            config.cacheDurationHours shouldBe 48L
            config.historicalPerformanceIntervalDays shouldBe 14L
        }
    })


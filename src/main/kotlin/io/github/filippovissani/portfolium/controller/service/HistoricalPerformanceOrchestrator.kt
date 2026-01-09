package io.github.filippovissani.portfolium.controller.service

import io.github.filippovissani.portfolium.controller.config.Config
import io.github.filippovissani.portfolium.controller.datasource.PriceDataSource
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.github.filippovissani.portfolium.model.domain.EtfSellTransaction
import io.github.filippovissani.portfolium.model.domain.HistoricalPerformance
import io.github.filippovissani.portfolium.model.domain.InvestmentBankAccount
import io.github.filippovissani.portfolium.model.domain.InvestmentTransaction
import io.github.filippovissani.portfolium.model.domain.PlannedExpensesBankAccount
import io.github.filippovissani.portfolium.model.service.HistoricalPerformanceService
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * Service for orchestrating historical performance calculations
 */
object HistoricalPerformanceOrchestrator {
    private val logger = LoggerFactory.getLogger(HistoricalPerformanceOrchestrator::class.java)

    /**
     * Calculate historical performance for a single account
     */
    fun calculateForAccount(
        account: Any,
        priceSource: PriceDataSource,
        config: Config,
    ): HistoricalPerformance? {
        val transactions = extractTransactionsFromAccount(account)
        val etfTransactions = convertToInvestmentTransactions(transactions)
        return calculateFromTransactions(etfTransactions, priceSource, config)
    }

    /**
     * Calculate combined historical performance from multiple accounts
     */
    fun calculateCombined(
        accounts: List<Any>,
        priceSource: PriceDataSource,
        config: Config,
    ): HistoricalPerformance? {
        val allEtfTransactions =
            accounts.flatMap { account ->
                val transactions = extractTransactionsFromAccount(account)
                convertToInvestmentTransactions(transactions)
            }
        return calculateFromTransactions(allEtfTransactions, priceSource, config)
    }

    /**
     * Extract transactions from any account type
     */
    private fun extractTransactionsFromAccount(account: Any): List<Any> =
        when (account) {
            is InvestmentBankAccount -> account.transactions
            is PlannedExpensesBankAccount -> account.transactions
            is EmergencyFundBankAccount -> account.transactions
            else -> emptyList()
        }

    /**
     * Convert bank account transactions to investment transactions
     */
    private fun convertToInvestmentTransactions(transactions: List<Any>): List<InvestmentTransaction> =
        transactions.mapNotNull { tx ->
            when (tx) {
                is EtfBuyTransaction ->
                    InvestmentTransaction(
                        date = tx.date,
                        etf = tx.name,
                        ticker = tx.ticker,
                        area = tx.area,
                        quantity = tx.quantity,
                        price = tx.price,
                        fees = tx.fees,
                    )

                is EtfSellTransaction ->
                    InvestmentTransaction(
                        date = tx.date,
                        etf = tx.name,
                        ticker = tx.ticker,
                        area = tx.area,
                        quantity = -tx.quantity,
                        price = tx.price,
                        fees = tx.fees,
                    )

                else -> null
            }
        }

    /**
     * Calculate historical performance from investment transactions
     */
    private fun calculateFromTransactions(
        etfTransactions: List<InvestmentTransaction>,
        priceSource: PriceDataSource,
        config: Config,
    ): HistoricalPerformance? =
        if (etfTransactions.isNotEmpty()) {
            val earliestDate = etfTransactions.minOfOrNull { it.date } ?: LocalDate.now()
            HistoricalPerformanceService.calculateHistoricalPerformance(
                transactions = etfTransactions,
                priceSource = priceSource,
                startDate = earliestDate,
                endDate = LocalDate.now(),
                intervalDays = config.historicalPerformanceIntervalDays,
            )
        } else {
            null
        }
}

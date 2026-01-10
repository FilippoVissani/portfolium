package io.github.filippovissani.portfolium.model.service

import io.github.filippovissani.portfolium.controller.csv.CsvUtils.toMoney
import io.github.filippovissani.portfolium.model.domain.EmergencyFundBankAccount
import io.github.filippovissani.portfolium.model.domain.EmergencyFundSummary
import io.github.filippovissani.portfolium.model.domain.EtfBuyTransaction
import io.github.filippovissani.portfolium.model.domain.EtfSellTransaction
import java.math.BigDecimal

/**
 * Service for emergency fund calculations
 */
object EmergencyFundService {
    /**
     * Calculate emergency fund summary
     */
    fun calculateEmergencyFundSummary(
        account: EmergencyFundBankAccount,
        avgMonthlyExpense: BigDecimal,
    ): EmergencyFundSummary {
        val targetCapital = avgMonthlyExpense.multiply(BigDecimal(account.targetMonthlyExpenses))
        val currentCapital = account.currentBalance
        val delta = targetCapital - currentCapital
        val status = if (currentCapital >= targetCapital) "OK" else "BELOW TARGET"

        // Check if account has ETF transactions (invested)
        val hasEtfTransactions = account.transactions.any { it is EtfBuyTransaction || it is EtfSellTransaction }
        val isLiquid = !hasEtfTransactions

        return EmergencyFundSummary(
            targetCapital = targetCapital.toMoney(),
            currentCapital = currentCapital.toMoney(),
            deltaToTarget = delta.toMoney(),
            status = status,
            isLiquid = isLiquid,
            historicalPerformance = null, // Will be set by Controller if needed
        )
    }
}

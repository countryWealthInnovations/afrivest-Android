package com.afrivest.app.data.model

import com.afrivest.app.data.model.User

data class Dashboard(
    val user: User,
    val wallets: List<Wallet>,
    val recent_transactions: List<Transaction>,
    val statistics: DashboardStatistics
)

data class DashboardStatistics(
    val total_deposits: String,
    val total_withdrawals: String,
    val total_transfers: String,
    val transaction_count: Int
) {
    fun getTotalDepositsDouble(): Double = total_deposits.toDoubleOrNull() ?: 0.0
    fun getTotalWithdrawalsDouble(): Double = total_withdrawals.toDoubleOrNull() ?: 0.0
    fun getTotalTransfersDouble(): Double = total_transfers.toDoubleOrNull() ?: 0.0
}
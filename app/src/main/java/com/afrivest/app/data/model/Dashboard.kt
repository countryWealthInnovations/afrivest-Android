package com.afrivest.app.data.model

import com.google.gson.annotations.SerializedName

data class Dashboard(
    val user: User,
    val wallets: List<Wallet>,
    @SerializedName("recent_transactions")
    val recentTransactions: List<Transaction>,
    @SerializedName("stats")
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
package com.afrivest.app.utils

object FeeCalculator {

    fun calculateFee(amount: Double): Double {
        return if (amount < 125_000) {
            1_000.0
        } else {
            amount * 0.012 // 1.2%
        }
    }

    fun formatCurrency(amount: Double): String {
        val formatter = java.text.NumberFormat.getInstance()
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return formatter.format(amount)
    }
}
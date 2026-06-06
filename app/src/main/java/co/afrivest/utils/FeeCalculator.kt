package co.afrivest.utils

import co.afrivest.data.local.PreferencesManager

object FeeCalculator {

    fun calculateFee(amount: Double): Double {
        return if (amount < 125_000) 1_000.0 else amount * 0.012
    }

    fun formatCurrency(amount: Double): String {
        val formatter = java.text.NumberFormat.getInstance()
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return formatter.format(amount)
    }

    fun flutterwavePayoutFee(amount: Double, currency: String, method: String): Double {
        if (method == "bank_transfer") {
            val rate = getRate("UGX", currency)
            return (5000.0 * rate)
        }
        return when (currency.uppercase()) {
            "UGX" -> if (amount < 125000) 1000.0 else amount * 0.012
            "KES" -> 100.0
            "TZS" -> if (amount < 40000) 500.0 else amount * 0.015
            "RWF" -> 500.0
            "ZMW" -> amount * 0.02
            "NGN" -> if (amount <= 5000) 10.0 else if (amount <= 50000) 25.0 else 50.0
            "GHS" -> amount * 0.015
            "XAF", "XOF" -> 1500.0
            else -> amount * 0.015
        }
    }

    fun flutterwaveCollectionFee(amount: Double, currency: String, method: String): Double {
        if (method == "card" || method == "international_card") {
            val pct = when (currency.uppercase()) {
                "GHS" -> 2.6; "NGN" -> 2.0; "ZAR" -> 2.9
                else -> 4.8
            }
            return amount * pct / 100
        }
        val pct = when (currency.uppercase()) {
            "UGX" -> 3.0; "KES" -> 2.9; "TZS" -> 2.5; "RWF" -> 3.5
            "ZMW" -> 3.0; "NGN" -> 2.0; "GHS" -> 2.0
            "XAF", "XOF" -> 2.0; "ZAR" -> 2.5
            else -> 3.0
        }
        return amount * pct / 100
    }

    fun convertCurrency(amount: Double, from: String, to: String, preferencesManager: PreferencesManager): Double {
        if (from == to) return amount
        val rates = preferencesManager.forexRates
        val fromRate = if (from == "UGX") 1.0 else rates[from] ?: fallbackRate(from)
        val toRate = if (to == "UGX") 1.0 else rates[to] ?: fallbackRate(to)
        val inUGX = if (from == "UGX") amount else amount * fromRate
        return if (to == "UGX") inUGX else inUGX / toRate
    }

    private fun getRate(from: String, to: String): Double {
        val fallback = mapOf(
            "UGX" to 1.0, "USD" to 3700.0, "GBP" to 4700.0,
            "EUR" to 4000.0, "KES" to 28.0, "NGN" to 2.5,
            "ZAR" to 200.0, "CAD" to 2700.0, "AED" to 1000.0
        )
        val fromUGX = fallback[from] ?: 1.0
        val toUGX = fallback[to] ?: 1.0
        if (from == "UGX") return 1.0 / toUGX
        if (to == "UGX") return fromUGX
        return fromUGX / toUGX
    }

    private fun fallbackRate(currency: String): Double {
        return mapOf(
            "USD" to 3700.0, "GBP" to 4700.0, "EUR" to 4000.0,
            "KES" to 28.0, "NGN" to 2.5, "ZAR" to 200.0,
            "CAD" to 2700.0, "AED" to 1000.0
        )[currency] ?: 1.0
    }
}
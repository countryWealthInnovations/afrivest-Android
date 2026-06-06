package co.afrivest.utils

import co.afrivest.data.local.PreferencesManager

object CurrencyConverter {

    // Fallback rates: 1 unit of currency = X UGX
    private val fallbackRates = mapOf(
        "UGX" to 1.0,
        "USD" to 3700.0,
        "GBP" to 4700.0,
        "EUR" to 4000.0,
        "KES" to 28.0,
        "NGN" to 2.5,
        "ZAR" to 200.0,
        "CAD" to 2700.0,
        "AED" to 1000.0,
    )

    fun getRate(from: String, to: String, preferencesManager: co.afrivest.data.local.PreferencesManager? = null): Double {
        if (from == to) return 1.0

        val stored = preferencesManager?.forexRates ?: emptyMap()
        val fromUGX = if (from == "UGX") 1.0 else (stored[from] ?: fallbackRates[from] ?: 1.0)
        val toUGX   = if (to   == "UGX") 1.0 else (stored[to]   ?: fallbackRates[to]   ?: 1.0)

        return when {
            from == "UGX" -> 1.0 / toUGX
            to   == "UGX" -> fromUGX
            else          -> fromUGX / toUGX
        }
    }

    fun convert(amount: Double, from: String, to: String): Double {
        return amount * getRate(from, to)
    }

    fun format(amount: Double, currency: String): String {
        return String.format("%,.2f %s", amount, currency)
    }
}
package co.afrivest.data.model

data class ForexRateItem(
    val base_currency: String,
    val target_currency: String,
    val rate: Double
)
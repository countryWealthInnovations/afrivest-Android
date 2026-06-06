package co.afrivest.data.model

data class CurrencyRequest(
    val default_currency: String,
    val secondary_currency: String?
)
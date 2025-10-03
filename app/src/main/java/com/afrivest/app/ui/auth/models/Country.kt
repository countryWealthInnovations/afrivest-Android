package com.afrivest.app.ui.auth.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(
    val code: String,
    val name: String,
    val dialCode: String,
    val flag: String
) : Parcelable {

    companion object {
        val ALL_COUNTRIES = listOf(
            // Africa
            Country("UG", "Uganda", "+256", "🇺🇬"),
            Country("KE", "Kenya", "+254", "🇰🇪"),
            Country("TZ", "Tanzania", "+255", "🇹🇿"),
            Country("RW", "Rwanda", "+250", "🇷🇼"),
            Country("NG", "Nigeria", "+234", "🇳🇬"),
            Country("GH", "Ghana", "+233", "🇬🇭"),
            Country("ZA", "South Africa", "+27", "🇿🇦"),

            // North America
            Country("US", "United States", "+1", "🇺🇸"),
            Country("CA", "Canada", "+1", "🇨🇦"),

            // Europe
            Country("GB", "United Kingdom", "+44", "🇬🇧"),
            Country("FR", "France", "+33", "🇫🇷"),
            Country("DE", "Germany", "+49", "🇩🇪"),
            Country("IT", "Italy", "+39", "🇮🇹"),
            Country("ES", "Spain", "+34", "🇪🇸"),
            Country("NL", "Netherlands", "+31", "🇳🇱"),
            Country("BE", "Belgium", "+32", "🇧🇪"),
            Country("SE", "Sweden", "+46", "🇸🇪"),
            Country("NO", "Norway", "+47", "🇳🇴"),
            Country("DK", "Denmark", "+45", "🇩🇰"),

            // Middle East
            Country("AE", "UAE", "+971", "🇦🇪"),
            Country("SA", "Saudi Arabia", "+966", "🇸🇦"),

            // Asia
            Country("CN", "China", "+86", "🇨🇳"),
            Country("IN", "India", "+91", "🇮🇳"),
            Country("JP", "Japan", "+81", "🇯🇵"),
            Country("SG", "Singapore", "+65", "🇸🇬"),

            // Oceania
            Country("AU", "Australia", "+61", "🇦🇺"),
            Country("NZ", "New Zealand", "+64", "🇳🇿")
        )

        val DEFAULT = ALL_COUNTRIES.first { it.code == "UG" }

        fun findByDialCode(dialCode: String): Country? {
            return ALL_COUNTRIES.firstOrNull { it.dialCode == dialCode }
        }
    }
}
package com.afrivest.app.utils

import android.util.Patterns
import java.util.regex.Pattern

object Validators {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates international phone number with country code
     * Accepts any valid country code (1-4 digits) followed by 6-15 digits
     * Examples:
     * - 256700000001 (Uganda)
     * - 14155552671 (USA)
     * - 447911123456 (UK)
     * - 9711234567890 (UAE)
     * - 861234567890 (China)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        // Remove any spaces, dashes, or parentheses
        val cleanPhone = phone.replace(Regex("[\\s\\-\\(\\)\\+]"), "")

        // International phone pattern:
        // - Starts with 1-4 digit country code
        // - Followed by 6-15 digits (total length 7-19 digits)
        val pattern = Pattern.compile("^[1-9][0-9]{6,18}$")

        return cleanPhone.isNotEmpty() && pattern.matcher(cleanPhone).matches()
    }

    /**
     * Formats phone number for display
     * Adds +256 country code prefix for Uganda numbers
     */
    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace(Regex("[\\s\\-\\(\\)\\+]"), "")

        // If already has 256 country code
        if (cleanPhone.startsWith("256")) {
            return "+$cleanPhone"
        }

        // If it's a 9-digit Uganda number (7xxxxxxxx), add +256
        if (cleanPhone.length == 9 && cleanPhone.startsWith("7")) {
            return "+256$cleanPhone"
        }

        // Default: add + if not present
        return if (cleanPhone.startsWith("+")) {
            cleanPhone
        } else {
            "+256$cleanPhone"
        }
    }

    /**
     * Validates and formats phone number
     * Returns formatted number or null if invalid
     */
    fun validateAndFormatPhone(phone: String): String? {
        return if (isValidPhoneNumber(phone)) {
            formatPhoneNumber(phone)
        } else {
            null
        }
    }

    /**
     * Extracts country code from phone number
     * Returns null if unable to determine
     */
    fun extractCountryCode(phone: String): String? {
        val cleanPhone = phone.replace(Regex("[\\s\\-\\(\\)\\+]"), "")

        // Try to match common country code patterns (1-4 digits)
        return when {
            cleanPhone.startsWith("1") && cleanPhone.length >= 11 -> "1" // USA/Canada
            cleanPhone.startsWith("44") && cleanPhone.length >= 12 -> "44" // UK
            cleanPhone.startsWith("971") && cleanPhone.length >= 12 -> "971" // UAE
            cleanPhone.startsWith("256") && cleanPhone.length >= 12 -> "256" // Uganda
            cleanPhone.startsWith("254") && cleanPhone.length >= 12 -> "254" // Kenya
            cleanPhone.startsWith("86") && cleanPhone.length >= 13 -> "86" // China
            cleanPhone.length >= 10 -> cleanPhone.substring(0, 3) // Default: first 3 digits
            else -> null
        }
    }

    fun isValidPassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("At least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("One uppercase letter")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("One number")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("One special character")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    fun getPasswordStrength(password: String): PasswordStrength {
        var strength = 0

        if (password.length >= 8) strength++
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++

        return when {
            strength < 3 -> PasswordStrength.WEAK
            strength < 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    fun isValidName(name: String): Boolean {
        val words = name.trim().split("\\s+".toRegex())
        return words.size >= 2 && words.all { it.all { char -> char.isLetter() } }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val messages: List<String>) : ValidationResult()
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}
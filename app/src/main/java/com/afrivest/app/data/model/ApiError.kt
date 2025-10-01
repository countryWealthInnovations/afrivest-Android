package com.afrivest.app.data.model

data class ApiError(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, List<String>>? = null
) {
    fun getFirstError(): String {
        return errors?.values?.firstOrNull()?.firstOrNull() ?: message
    }

    fun getAllErrors(): List<String> {
        return errors?.values?.flatten() ?: listOf(message)
    }
}
package com.afrivest.app.data.model

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

// ==================== UI STATE ====================

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> loading() = UiState<T>(isLoading = true)
        fun <T> success(data: T) = UiState(data = data)
        fun <T> error(message: String) = UiState<T>(error = message)
    }
}
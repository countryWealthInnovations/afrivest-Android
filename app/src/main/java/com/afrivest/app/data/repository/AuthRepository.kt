package com.afrivest.app.data.repository

import com.afrivest.app.data.api.*
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.ApiError
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.User
import com.afrivest.app.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String,
        deviceToken: String? = null
    ): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                name = name,
                email = email,
                phone_number = phoneNumber,
                password = password,
                password_confirmation = password,
                device_token = deviceToken,
                device_type = "android",
                app_version = com.afrivest.app.BuildConfig.VERSION_NAME
            )

            val response = apiService.register(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Register error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun login(
        email: String,
        password: String,
        deviceToken: String? = null
    ): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(
                email = email,
                password = password,
                device_token = deviceToken,
                device_type = "android"
            )

            val response = apiService.login(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun verifyOTP(code: String): Resource<OTPResponse> = withContext(Dispatchers.IO) {
        try {
            val request = OTPRequest(code)
            val response = apiService.verifyOTP(request)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Verify OTP error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun resendOTP(): Resource<OTPResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resendOTP()
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Resend OTP error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun forgotPassword(email: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ForgotPasswordRequest(email)
            val response = apiService.forgotPassword(request)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Forgot password error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        password: String
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ResetPasswordRequest(
                email = email,
                code = code,
                password = password,
                password_confirmation = password
            )
            val response = apiService.resetPassword(request)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Reset password error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun getCurrentUser(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get current user error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            clearSession()
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            clearSession()
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    private fun handleAuthResponse(response: Response<ApiResponse<AuthResponse>>): Resource<AuthResponse> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                securePreferences.saveAuthToken(body.data.token)
                securePreferences.saveUserId(body.data.user.id)
                securePreferences.saveUserEmail(body.data.user.email)
                securePreferences.saveUserName(body.data.user.name)
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                apiError.getFirstError()
            } catch (e: Exception) {
                response.message() ?: Constants.ErrorMessages.UNKNOWN_ERROR
            }
            Resource.Error(errorMessage)
        }
    }

    private fun <T> handleResponse(response: Response<ApiResponse<T>>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                apiError.getFirstError()
            } catch (e: Exception) {
                response.message() ?: Constants.ErrorMessages.UNKNOWN_ERROR
            }
            Resource.Error(errorMessage)
        }
    }

    fun clearSession() {
        securePreferences.clearAll()
    }

    fun isLoggedIn(): Boolean {
        return securePreferences.isLoggedIn()
    }
}
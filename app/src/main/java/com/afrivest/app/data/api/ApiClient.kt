package com.afrivest.app.data.api

import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.model.User
import com.afrivest.app.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null

    fun getClient(securePreferences: SecurePreferences): Retrofit {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(Constants.TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .readTimeout(Constants.TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(Constants.TIMEOUT_WRITE, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(securePreferences))
                .addInterceptor(createLoggingInterceptor())
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}

// ==================== AUTH INTERCEPTOR ====================

class AuthInterceptor(
    private val securePreferences: SecurePreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = securePreferences.getAuthToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        }

        return chain.proceed(newRequest)
    }
}

package co.afrivest.data.api

import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.Transaction
import co.afrivest.data.model.User
import co.afrivest.utils.Constants
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
    var logoutCallback: (() -> Unit)? = null

    fun reset() {
        retrofit = null
    }

    fun getClient(securePreferences: SecurePreferences): Retrofit {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(Constants.TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .readTimeout(Constants.TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(Constants.TIMEOUT_WRITE, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(securePreferences))
                .addInterceptor(createLoggingInterceptor())
                .addInterceptor(UnauthorizedInterceptor(securePreferences))
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

// ==================== UNAUTHORIZED INTERCEPTOR ====================

class UnauthorizedInterceptor(
    private val securePreferences: SecurePreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            securePreferences.clearAll()
            ApiClient.reset()
            ApiClient.logoutCallback?.invoke()
        }
        return response
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

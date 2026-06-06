package co.afrivest.di

import android.content.Context
import co.afrivest.data.api.ApiClient
import co.afrivest.data.api.ApiService
import co.afrivest.data.local.PreferencesManager
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(
        securePreferences: SecurePreferences
    ): ApiService {
        return ApiClient.getClient(securePreferences).create(ApiService::class.java)
    }
}

package com.afrivest.app.di

import android.content.Context
import com.afrivest.app.data.api.ApiClient
import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.local.PreferencesManager
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.repository.*
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

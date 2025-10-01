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
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        securePreferences: SecurePreferences
    ): AuthRepository {
        return AuthRepository(apiService, securePreferences)
    }

    @Provides
    @Singleton
    fun provideWalletRepository(
        apiService: ApiService
    ): WalletRepository {
        return WalletRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        apiService: ApiService
    ): TransactionRepository {
        return TransactionRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        apiService: ApiService
    ): ProfileRepository {
        return ProfileRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideTransferRepository(
        apiService: ApiService
    ): TransferRepository {
        return TransferRepository(apiService)
    }
}
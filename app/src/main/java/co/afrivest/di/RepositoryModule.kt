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
        apiService: ApiService,
        securePreferences: SecurePreferences
    ): ProfileRepository {
        return ProfileRepository(apiService, securePreferences)
    }

    @Provides
    @Singleton
    fun provideTransferRepository(
        apiService: ApiService
    ): TransferRepository {
        return TransferRepository(apiService)
    }
}
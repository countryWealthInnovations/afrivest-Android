package com.afrivest.app.ui.splash

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.local.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> = _navigateTo

    enum class NavigationDestination {
        ONBOARDING,
        LOGIN,
        DASHBOARD
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            // Small delay for smooth transition
            delay(500)

            // Check if onboarding has been completed (first launch = false means completed)
            val hasCompletedOnboarding = !securePreferences.isFirstLaunch()

            // Check if user has valid auth token
            val token = securePreferences.getAuthToken()

            when {
                token != null && token.isNotEmpty() -> {
                    // Validate token with API (for now, assume valid)
                    validateToken(token)
                }
                hasCompletedOnboarding -> {
                    _navigateTo.value = NavigationDestination.LOGIN
                }
                else -> {
                    _navigateTo.value = NavigationDestination.ONBOARDING
                }
            }
        }
    }

    private suspend fun validateToken(token: String) {
        // TODO: Call API to validate token
        // For now, assume token is valid
        delay(300)

        // If token is valid
        _navigateTo.value = NavigationDestination.DASHBOARD

        // If token is invalid:
        // _navigateTo.value = NavigationDestination.LOGIN
    }
}
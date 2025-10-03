package com.afrivest.app.ui.onboarding

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afrivest.app.data.local.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _shouldNavigateToAuth = MutableLiveData<Boolean>()
    val shouldNavigateToAuth: LiveData<Boolean> = _shouldNavigateToAuth

    fun completeOnboarding() {
        // Mark onboarding as completed (set first launch to false)
        securePreferences.setFirstLaunch(false)

        // Navigate to auth
        _shouldNavigateToAuth.value = true
    }

    fun skipOnboarding() {
        completeOnboarding()
    }
}
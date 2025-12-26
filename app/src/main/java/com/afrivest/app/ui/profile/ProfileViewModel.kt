package com.afrivest.app.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.User
import com.afrivest.app.data.repository.AuthRepository
import com.afrivest.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _biometricEnabled = MutableLiveData(false)
    val biometricEnabled: LiveData<Boolean> = _biometricEnabled

    private val _logoutSuccess = MutableLiveData(false)
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    init {
        loadBiometricPreference()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.getCurrentUser()

                when (response) {
                    is Resource.Success -> {
                        _user.value = response.data
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _errorMessage.value = response.message
                        _isLoading.value = false
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile")
                _errorMessage.value = e.message ?: "Failed to load profile"
                _isLoading.value = false
            }
        }
    }

    private fun loadBiometricPreference() {
        val prefs = context.getSharedPreferences("afrivest_prefs", Context.MODE_PRIVATE)
        _biometricEnabled.value = prefs.getBoolean("biometric_enabled", false)
    }

    fun enableBiometric(enabled: Boolean) {
        val prefs = context.getSharedPreferences("afrivest_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _biometricEnabled.value = enabled
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.logout()

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _logoutSuccess.value = true
                    }
                    is Resource.Error -> {
                        _errorMessage.value = response.message
                        _isLoading.value = false
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Logout failed")
                _errorMessage.value = e.message ?: "Failed to logout"
                _isLoading.value = false
            }
        }
    }
}
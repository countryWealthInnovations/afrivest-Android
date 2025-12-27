package com.afrivest.app.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.User
import com.afrivest.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _updateSuccess = MutableLiveData(false)
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val response = profileRepository.getProfile()) {
                    is Resource.Success -> {
                        _user.value = User(
                            id = response.data!!.id,
                            name = response.data.name,
                            email = response.data.email,
                            phone_number = response.data.phoneNumber ?: "",
                            role = response.data.role,
                            status = response.data.status,
                            avatar_url = response.data.avatarUrl,
                            email_verified = response.data.emailVerified,
                            kyc_verified = response.data.kycVerified,
                            created_at = response.data.createdAt
                        )
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
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, phone: String, avatarBitmap: Bitmap?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Upload avatar first if changed
                if (avatarBitmap != null) {
                    val part = bitmapToMultipartBody(avatarBitmap)
                    when (profileRepository.uploadAvatar(part)) {
                        is Resource.Error -> {
                            _errorMessage.value = "Failed to upload avatar"
                            _isLoading.value = false
                            return@launch
                        }
                        else -> {}
                    }
                }

                // Update profile info
                when (profileRepository.updateProfile(name, phone)) {
                    is Resource.Success -> {
                        _updateSuccess.value = true
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _errorMessage.value = "Failed to update profile"
                        _isLoading.value = false
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Update profile failed")
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            try {
                profileRepository.deleteAvatar()
            } catch (e: Exception) {
                Timber.e(e, "Delete avatar failed")
            }
        }
    }

    private fun bitmapToMultipartBody(bitmap: Bitmap): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        val requestBody = byteArray.toRequestBody(
            "image/jpeg".toMediaTypeOrNull(),
            0,
            byteArray.size
        )

        return MultipartBody.Part.createFormData(
            "avatar",
            "avatar.jpg",
            requestBody
        )
    }
}
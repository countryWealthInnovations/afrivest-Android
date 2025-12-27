package com.afrivest.app.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.databinding.ActivityNotificationSettingsBinding
import com.afrivest.app.data.local.PreferencesManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Notifications"
        }
    }

    private fun loadSettings() {
        // Load saved settings from PreferencesManager
//        binding.switchPush.isChecked = preferencesManager.isPushNotificationEnabled()
//        binding.switchEmail.isChecked = preferencesManager.isEmailNotificationEnabled()
//        binding.switchSms.isChecked = preferencesManager.isSmsNotificationEnabled()
    }

    private fun setupListeners() {
//        binding.switchPush.setOnCheckedChangeListener { _, isChecked ->
//            preferencesManager.setPushNotificationEnabled(isChecked)
//            showSettingSavedMessage("Push notifications")
//        }
//
//        binding.switchEmail.setOnCheckedChangeListener { _, isChecked ->
//            preferencesManager.setEmailNotificationEnabled(isChecked)
//            showSettingSavedMessage("Email notifications")
//        }
//
//        binding.switchSms.setOnCheckedChangeListener { _, isChecked ->
//            preferencesManager.setSmsNotificationEnabled(isChecked)
//            showSettingSavedMessage("SMS notifications")
//        }
    }

    private fun showSettingSavedMessage(setting: String) {
        Snackbar.make(
            binding.root,
            "$setting setting saved",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
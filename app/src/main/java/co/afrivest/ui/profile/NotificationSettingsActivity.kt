package co.afrivest.ui.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import co.afrivest.data.api.ApiService
import co.afrivest.databinding.ActivityNotificationSettingsBinding
import co.afrivest.data.local.PreferencesManager
import co.afrivest.ui.base.BaseActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _push  = MutableStateFlow(prefs.notifPush)
    private val _email = MutableStateFlow(prefs.notifEmail)
    private val _sms   = MutableStateFlow(prefs.notifSms)

    val push:  StateFlow<Boolean> = _push
    val email: StateFlow<Boolean> = _email
    val sms:   StateFlow<Boolean> = _sms

    init { fetchSettings() }

    private fun fetchSettings() {
        viewModelScope.launch {
            try {
                val response = apiService.getNotificationSettings()
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _push.value  = it.push_enabled
                        _email.value = it.email_enabled
                        _sms.value   = it.sms_enabled
                        prefs.notifPush  = it.push_enabled
                        prefs.notifEmail = it.email_enabled
                        prefs.notifSms   = it.sms_enabled
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("NotifSettings", "Fetch failed: ${e.message}")
            }
        }
    }

    fun update(push: Boolean? = null, email: Boolean? = null, sms: Boolean? = null) {
        push?.let  { _push.value  = it; prefs.notifPush  = it }
        email?.let { _email.value = it; prefs.notifEmail = it }
        sms?.let   { _sms.value   = it; prefs.notifSms   = it }

        viewModelScope.launch {
            try {
                apiService.updateNotificationSettings(mapOf(
                    "push_enabled"  to _push.value,
                    "email_enabled" to _email.value,
                    "sms_enabled"   to _sms.value,
                ))
            } catch (e: Exception) {
                android.util.Log.w("NotifSettings", "Update failed: ${e.message}")
            }
        }
    }
}

// ── Activity ───────────────────────────────────────────────

@AndroidEntryPoint
class NotificationSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private val viewModel: NotificationSettingsViewModel by viewModels()

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        observeSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Notifications"
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.push.collect  { binding.switchPush.isChecked  = it } }
                launch { viewModel.email.collect { binding.switchEmail.isChecked = it } }
                launch { viewModel.sms.collect   { binding.switchSms.isChecked   = it } }
            }
        }
    }

    private fun setupListeners() {
        binding.switchPush.setOnCheckedChangeListener { _, checked ->
            viewModel.update(push = checked)
            showSaved("Push notifications")
        }
        binding.switchEmail.setOnCheckedChangeListener { _, checked ->
            viewModel.update(email = checked)
            showSaved("Email notifications")
        }
        binding.switchSms.setOnCheckedChangeListener { _, checked ->
            viewModel.update(sms = checked)
            showSaved("SMS notifications")
        }
    }

    private fun showSaved(setting: String) {
        Snackbar.make(binding.root, "$setting updated", Snackbar.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
package co.afrivest.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.afrivest.R
import co.afrivest.databinding.ActivityMainBinding
import co.afrivest.ui.dashboard.DashboardFragment
import co.afrivest.ui.assets.AssetsFragment
import co.afrivest.ui.history.HistoryFragment
import co.afrivest.ui.profile.ProfileFragment
import co.afrivest.ui.transfer.SendMoneyActivity
import co.afrivest.data.local.PreferencesManager
import co.afrivest.ui.base.BaseActivity
import co.afrivest.ui.onboarding.CurrencySelectionActivity
import co.afrivest.ui.transfer.WithdrawActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isFabExpanded = false

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Auto logout on expired token
        co.afrivest.data.api.ApiClient.logoutCallback = {
            runOnUiThread {
                val intent = Intent(this, co.afrivest.ui.auth.LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }

        // Show currency setup on first login
        if (preferencesManager.defaultCurrency == null) {
            startActivity(Intent(this, CurrencySelectionActivity::class.java))
            finish()
            return
        }
        setupFAB()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_assets -> {
                    loadFragment(AssetsFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }



    private fun setupFAB() {
        binding.fab.hide()
    }

    private fun collapseFAB() {
        isFabExpanded = false
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (isFabExpanded) {
            collapseFAB()
        } else {
            super.onBackPressed()
        }
    }
}
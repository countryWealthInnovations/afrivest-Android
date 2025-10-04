package com.afrivest.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityMainBinding
import com.afrivest.app.ui.dashboard.DashboardFragment
import com.afrivest.app.ui.assets.AssetsFragment
import com.afrivest.app.ui.history.HistoryFragment
import com.afrivest.app.ui.profile.ProfileFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isFabExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
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
                    showFAB()
                    true
                }
                R.id.nav_assets -> {
                    loadFragment(AssetsFragment())
                    hideFAB()
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    hideFAB()
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    hideFAB()
                    true
                }
                else -> false
            }
        }
    }

    private fun showFAB() {
        binding.fab.show()
    }

    private fun hideFAB() {
        binding.fab.hide()
        if (isFabExpanded) {
            collapseFAB()
        }
    }

    private fun setupFAB() {
        binding.fab.setOnClickListener {
            if (isFabExpanded) {
                collapseFAB()
            } else {
                expandFAB()
            }
        }

        binding.fabSendMoney.setOnClickListener {
            // TODO: Navigate to send money
            MaterialAlertDialogBuilder(this)
                .setTitle("Send Money")
                .setMessage("Send money feature coming soon")
                .setPositiveButton("OK", null)
                .show()
            collapseFAB()
        }

        binding.fabReceiveMoney.setOnClickListener {
            // TODO: Navigate to receive money
            MaterialAlertDialogBuilder(this)
                .setTitle("Receive Money")
                .setMessage("Receive money feature coming soon")
                .setPositiveButton("OK", null)
                .show()
            collapseFAB()
        }

        binding.fabOverlay.setOnClickListener {
            collapseFAB()
        }
    }

    private fun expandFAB() {
        isFabExpanded = true
        binding.fabOverlay.visibility = android.view.View.VISIBLE
        binding.fabSendMoney.show()
        binding.fabReceiveMoney.show()
        binding.fab.setImageResource(R.drawable.ic_close)
    }

    private fun collapseFAB() {
        isFabExpanded = false
        binding.fabOverlay.visibility = android.view.View.GONE
        binding.fabSendMoney.hide()
        binding.fabReceiveMoney.hide()
        binding.fab.setImageResource(R.drawable.ic_add)
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
package com.afrivest.app.ui.marketplace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.databinding.ActivityMarketplaceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarketplaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarketplaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMarketplace()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Marketplace"
        }
    }

    private fun setupMarketplace() {
        binding.btnGold.setOnClickListener {
            startActivity(Intent(this, GoldMarketplaceActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
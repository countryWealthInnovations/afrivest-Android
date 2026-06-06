package co.afrivest.ui.investments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.databinding.ActivityInvestmentCategoriesBinding
import co.afrivest.R
import co.afrivest.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentCategoriesActivity : BaseActivity() {

    private lateinit var binding: ActivityInvestmentCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Investments"
        }
    }

    private fun setupCategories() {
        binding.btnTreasuryBonds.setOnClickListener {
            navigateToProducts("treasury-bonds")
        }

        binding.btnUnitTrusts.setOnClickListener {
            navigateToProducts("unit-trusts")
        }

        binding.btnFixedDeposits.setOnClickListener {
            navigateToProducts("fixed-deposits")
        }

        binding.btnRealEstate.setOnClickListener {
            navigateToProducts("real-estate")
        }
    }

    private fun navigateToProducts(categorySlug: String) {
        startActivity(Intent(this, InvestmentProductsActivity::class.java).apply {
            putExtra("category_slug", categorySlug)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
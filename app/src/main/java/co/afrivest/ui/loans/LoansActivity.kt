package co.afrivest.ui.loans

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.afrivest.data.model.Loan
import co.afrivest.databinding.ActivityLoansBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoansActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoansBinding
    private val viewModel: LoanViewModel by viewModels()
    private lateinit var adapter: LoanAdapter
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LoanAdapter(emptyList()) { openDetail(it) }
        binding.rvLoans.layoutManager = LinearLayoutManager(this)
        binding.rvLoans.adapter = adapter

        binding.btnRequestLoan.setOnClickListener {
            startActivity(Intent(this, RequestLoanActivity::class.java))
        }

        binding.tabLoans.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                render()
                if (tab.position == 2) viewModel.loadAvailable()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyLoans()
        if (currentTab == 2) viewModel.loadAvailable()
    }

    private fun observe() {
        viewModel.loading.observe(this) {
            binding.progressLoans.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) {
            it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }
        viewModel.borrowed.observe(this) { if (currentTab == 0) render() }
        viewModel.lent.observe(this) { if (currentTab == 1) render() }
        viewModel.available.observe(this) { if (currentTab == 2) render() }
    }

    private fun render() {
        val list: List<Loan> = when (currentTab) {
            0 -> viewModel.borrowed.value ?: emptyList()
            1 -> viewModel.lent.value ?: emptyList()
            else -> viewModel.available.value ?: emptyList()
        }
        adapter.submit(list)
        binding.tvLoansEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.tvLoansEmpty.text = when (currentTab) {
            0 -> "You have not borrowed yet"
            1 -> "You have not lent yet"
            else -> "No open requests to fund"
        }
    }

    private fun openDetail(loan: Loan) {
        val role = when (currentTab) {
            2 -> "available"
            1 -> "lender"
            else -> "borrower"
        }
        startActivity(Intent(this, LoanDetailActivity::class.java).apply {
            putExtra("loan_uuid", loan.uuid)
            putExtra("role", role)
        })
    }
}
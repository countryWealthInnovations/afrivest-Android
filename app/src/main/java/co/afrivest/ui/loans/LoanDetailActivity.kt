package co.afrivest.ui.loans

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.model.Loan
import co.afrivest.databinding.ActivityLoanDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoanDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoanDetailBinding
    private val viewModel: LoanViewModel by viewModels()
    private lateinit var uuid: String
    private var role: String = "borrower"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uuid = intent.getStringExtra("loan_uuid") ?: run { finish(); return }
        role = intent.getStringExtra("role") ?: "borrower"

        binding.btnFund.setOnClickListener { viewModel.fund(uuid) }
        binding.btnRepay.setOnClickListener {
            val amt = binding.etRepayAmount.text.toString().toDoubleOrNull()
            if (amt == null || amt <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            } else viewModel.repay(uuid, amt)
        }

        observe()
        viewModel.loadDetail(uuid)
    }

    private fun observe() {
        viewModel.loading.observe(this) {
            binding.progressDetail.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) {
            it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }
        viewModel.message.observe(this) {
            it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); viewModel.clearMessage() }
        }
        viewModel.detail.observe(this) { it?.let { loan -> bind(loan) } }
        viewModel.actionOk.observe(this) { loan ->
            loan?.let { viewModel.clearAction(); viewModel.loadDetail(uuid) }
        }
    }

    private fun bind(loan: Loan) {
        binding.tvDetailReference.text = loan.reference
        binding.tvDetailStatus.text = loan.status.replaceFirstChar { it.uppercase() }

        binding.breakdownContainer.removeAllViews()
        row("Principal", "${loan.currency} ${loan.principal ?: "0"}")
        row("Term", loan.term?.replace("_", " ") ?: "-")
        row("Interest rate", "${loan.interest_rate ?: "0"}%")
        row("Interest", "${loan.currency} ${loan.interest_amount ?: "0"}")
        row("Handling fee", "${loan.currency} ${loan.handling_fee ?: "0"}")
        row("Total repayment", "${loan.currency} ${loan.total_repayment ?: "0"}")
        loan.outstanding?.let { row("Outstanding", "${loan.currency} %.2f".format(it)) }
        loan.due_date?.let { row("Due", it.take(10)) }
        (loan.borrower?.name ?: loan.lender?.name)?.let {
            row(if (loan.borrower != null) "Borrower" else "Lender", it)
        }
        loan.purpose?.let { row("Purpose", it) }

        binding.repaymentsContainer.removeAllViews()
        val reps = loan.repayments
        if (reps.isNullOrEmpty()) {
            val tv = TextView(this).apply {
                text = "No repayments yet"
                setTextColor(Color.parseColor("#9CA3AF"))
            }
            binding.repaymentsContainer.addView(tv)
        } else {
            reps.forEach { r ->
                val tv = TextView(this).apply {
                    text = "${loan.currency} ${r.amount ?: "0"}  •  ${r.source ?: ""}  •  ${r.paid_at?.take(10) ?: ""}"
                    setTextColor(Color.parseColor("#E5E7EB"))
                    setPadding(0, 8, 0, 8)
                }
                binding.repaymentsContainer.addView(tv)
            }
        }

        val outstanding = loan.outstanding ?: (loan.totalValue - (loan.amount_repaid ?: 0.0))
        val canRepay = role == "borrower" && loan.status in listOf("active", "funded", "defaulted") && outstanding > 0
        val canFund = role == "available" && loan.status == "requested"
        binding.repaySection.visibility = if (canRepay) View.VISIBLE else View.GONE
        binding.btnFund.visibility = if (canFund) View.VISIBLE else View.GONE
    }

    private fun row(label: String, value: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 6, 0, 6) }
        }
        val l = TextView(this).apply {
            text = label
            setTextColor(Color.parseColor("#9CA3AF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val v = TextView(this).apply {
            text = value
            setTextColor(Color.parseColor("#FFFFFF"))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        container.addView(l); container.addView(v)
        binding.breakdownContainer.addView(container)
    }
}
package co.afrivest.ui.loans

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.LoanTerm
import co.afrivest.databinding.ActivityRequestLoanBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RequestLoanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestLoanBinding
    private val viewModel: LoanViewModel by viewModels()
    private var terms: List<LoanTerm> = emptyList()
    private var selectedTerm: LoanTerm? = null

    @Inject lateinit var securePreferences: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLoanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loadTerms()

        binding.spinnerTerm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedTerm = terms.getOrNull(pos)
                updateEstimate()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.etAmount.addTextChangedListener(SimpleWatcher { updateEstimate() })

        binding.btnSubmitLoan.setOnClickListener { submit() }

        observe()
    }

    private fun observe() {
        viewModel.terms.observe(this) { list ->
            terms = list
            val labels = list.map { "${it.term.replace("_", " ")}  (${it.interest_rate}%)" }
            binding.spinnerTerm.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, labels
            )
            if (list.isNotEmpty()) { selectedTerm = list[0]; updateEstimate() }
        }
        viewModel.loading.observe(this) {
            binding.progressRequest.visibility = if (it) View.VISIBLE else View.GONE
            binding.btnSubmitLoan.isEnabled = !it
        }
        viewModel.error.observe(this) {
            it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }
        viewModel.actionOk.observe(this) { loan ->
            loan?.let {
                viewModel.clearAction()
                startActivity(Intent(this, LoanDetailActivity::class.java).apply {
                    putExtra("loan_uuid", it.uuid)
                    putExtra("role", "borrower")
                })
                finish()
            }
        }
    }

    private fun updateEstimate() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val rate = selectedTerm?.interest_rate?.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedTerm == null) {
            binding.tvEstimate.text = "Enter an amount to see estimated interest"
            return
        }
        val interest = amount * rate / 100.0
        binding.tvEstimate.text =
            "Estimated interest: %.2f  •  Repay approx: %.2f\nA handling fee applies and the exact breakdown is shown after you submit."
                .format(interest, amount + interest)
    }

    private fun submit() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val term = selectedTerm?.term
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show(); return
        }
        if (term == null) {
            Toast.makeText(this, "Select a term", Toast.LENGTH_SHORT).show(); return
        }
        val currency = securePreferences.getSelectedCurrency()
        val purpose = binding.etPurpose.text.toString().ifBlank { null }
        viewModel.requestLoan(amount, term, currency, purpose)
    }
}
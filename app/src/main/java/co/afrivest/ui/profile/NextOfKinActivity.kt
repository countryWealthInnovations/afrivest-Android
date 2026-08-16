package co.afrivest.ui.profile

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import co.afrivest.databinding.ActivityNextOfKinBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.utils.gone
import co.afrivest.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NextOfKinActivity : BaseActivity() {

    private lateinit var binding: ActivityNextOfKinBinding
    private val viewModel: NextOfKinViewModel by viewModels()
    private var selectedRelationshipKey: String = "spouse"

    private val countryCodes = listOf(
        "+256" to "🇺🇬 +256",
        "+254" to "🇰🇪 +254",
        "+255" to "🇹🇿 +255",
        "+250" to "🇷🇼 +250",
        "+1" to "🇺🇸 +1",
        "+44" to "🇬🇧 +44",
        "+971" to "🇦🇪 +971",
    )
    private var selectedCountryCode = "+256"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNextOfKinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val relationshipLabels = viewModel.relationships.map { it.second }
        val relAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, relationshipLabels)
        binding.spinnerRelationship.setAdapter(relAdapter)
        binding.spinnerRelationship.setText(relationshipLabels.first(), false)
        binding.spinnerRelationship.setOnItemClickListener { _, _, position, _ ->
            selectedRelationshipKey = viewModel.relationships[position].first
        }

        val codeLabels = countryCodes.map { it.second }
        val codeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, codeLabels)
        binding.spinnerCountryCode.setAdapter(codeAdapter)
        binding.spinnerCountryCode.setText(countryCodes.first().second, false)
        binding.spinnerCountryCode.setOnItemClickListener { _, _, position, _ ->
            selectedCountryCode = countryCodes[position].first
        }

        binding.btnSave.setOnClickListener {
            val rawPhone = binding.etPhone.text.toString().trim()
            val fullPhone = if (rawPhone.startsWith("+")) rawPhone else "$selectedCountryCode$rawPhone"
            viewModel.save(
                binding.etName.text.toString().trim(),
                selectedRelationshipKey,
                fullPhone,
                binding.etEmail.text.toString().trim()
            )
        }

        binding.btnRemove.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Remove Next of Kin")
                .setMessage("They will be notified that they've been removed. Continue?")
                .setPositiveButton("Remove") { _, _ -> viewModel.delete() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.kin.observe(this) { kin ->
            if (kin != null) {
                binding.etName.setText(kin.name)
                val idx = viewModel.relationships.indexOfFirst { it.first == kin.relationship }
                if (idx >= 0) {
                    selectedRelationshipKey = kin.relationship
                    binding.spinnerRelationship.setText(relationshipLabels[idx], false)
                }
                val matchedCode = countryCodes.firstOrNull { kin.phone_number.startsWith(it.first) }
                if (matchedCode != null) {
                    selectedCountryCode = matchedCode.first
                    binding.spinnerCountryCode.setText(matchedCode.second, false)
                    binding.etPhone.setText(kin.phone_number.removePrefix(matchedCode.first))
                } else {
                    binding.etPhone.setText(kin.phone_number)
                }
                binding.etEmail.setText(kin.email)
                binding.btnRemove.visible()
                binding.btnSave.text = "Update Next of Kin"
            } else {
                binding.btnRemove.gone()
                binding.btnSave.text = "Save Next of Kin"
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            if (loading) binding.loadingOverlay.root.visible() else binding.loadingOverlay.root.gone()
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Snackbar.make(binding.root, "Saved. They'll receive an email and SMS.", Snackbar.LENGTH_LONG).show()
                finish()
            }
        }

        viewModel.load()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Next of Kin"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
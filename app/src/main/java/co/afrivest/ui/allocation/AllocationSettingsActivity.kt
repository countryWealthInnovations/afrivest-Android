package co.afrivest.ui.allocation

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.databinding.ActivityAllocationSettingsBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllocationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllocationSettingsBinding
    private val viewModel: AllocationViewModel by viewModels()
    private val valueViews = HashMap<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllocationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveAllocation.setOnClickListener { viewModel.save() }

        observe()
        viewModel.load()
    }

    private fun observe() {
        viewModel.targets.observe(this) { buildRows(it) }

        viewModel.allocation.observe(this) { map ->
            map.forEach { (t, v) -> valueViews[t]?.text = "$v%" }
        }

        viewModel.total.observe(this) { total ->
            binding.tvTotal.text = "Total: $total%"
            val ok = total == 100
            binding.tvTotal.setTextColor(
                if (ok) Color.parseColor("#10b981") else Color.parseColor("#EFBF04")
            )
            binding.btnSaveAllocation.isEnabled = ok
        }

        viewModel.loading.observe(this) {
            binding.progressAllocation.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) {
            it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }

        viewModel.saved.observe(this) {
            if (it) { Toast.makeText(this, "Allocation saved", Toast.LENGTH_SHORT).show(); finish() }
        }
    }

    private fun buildRows(targets: List<String>) {
        binding.allocationContainer.removeAllViews()
        valueViews.clear()

        targets.forEach { target ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }

            val label = TextView(this).apply {
                text = labelFor(target)
                setTextColor(Color.parseColor("#FFFFFF"))
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val minus = stepButton("−") { viewModel.adjust(target, -5) }
            val value = TextView(this).apply {
                text = "0%"
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#EFBF04"))
                textSize = 16f
                minWidth = 120
            }
            val plus = stepButton("+") { viewModel.adjust(target, +5) }

            valueViews[target] = value

            row.addView(label)
            row.addView(minus)
            row.addView(value)
            row.addView(plus)
            binding.allocationContainer.addView(row)
        }
    }

    private fun stepButton(text: String, onClick: () -> Unit): MaterialButton {
        return MaterialButton(this).apply {
            this.text = text
            textSize = 18f
            setBackgroundColor(Color.parseColor("#2F2F2F"))
            setTextColor(Color.parseColor("#EFBF04"))
            insetTop = 0
            insetBottom = 0
            layoutParams = LinearLayout.LayoutParams(120, 120)
            setOnClickListener { onClick() }
        }
    }

    private fun labelFor(target: String): String = when (target) {
        "wallet" -> "Wallet (spendable)"
        "savings" -> "Savings"
        "investment" -> "Investment"
        "p2p" -> "Send to family (P2P)"
        else -> target.replaceFirstChar { it.uppercase() }
    }
}
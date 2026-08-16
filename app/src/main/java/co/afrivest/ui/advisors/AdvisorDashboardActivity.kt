package co.afrivest.ui.advisors

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.model.AdvisorDashboard
import co.afrivest.data.model.AvailabilitySlot
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvisorDashboardActivity : AppCompatActivity() {
    private val viewModel: AdvisorDashboardViewModel by viewModels()
    private lateinit var root: LinearLayout
    private lateinit var progress: ProgressBar
    private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val working = mutableListOf<AvailabilitySlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = android.widget.ScrollView(this).apply { setBackgroundColor(Color.parseColor("#1A1A1A")) }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 64, 48, 64) }
        progress = ProgressBar(this).apply { visibility = View.GONE }
        root.addView(progress)
        scroll.addView(root); setContentView(scroll)

        viewModel.loading.observe(this) { progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(this) { it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() } }
        viewModel.message.observe(this) { it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); viewModel.clearMessage() } }
        viewModel.dashboard.observe(this) { it?.let { d -> render(d) } }
        viewModel.load()
    }

    private fun tv(t: String, c: String = "#FFFFFF", size: Float = 14f, bold: Boolean = false, top: Int = 0) =
        TextView(this).apply { text = t; setTextColor(Color.parseColor(c)); textSize = size; setPadding(0, top, 0, 0); if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD) }

    private fun render(d: AdvisorDashboard) {
        root.removeAllViews(); root.addView(progress)
        working.clear(); working.addAll(d.availability)

        root.addView(tv("Advisor Dashboard", size = 22f, bold = true))
        root.addView(tv(d.profile.display_name, "#9CA3AF", 14f, top = 4))
        root.addView(tv("Fee ${d.profile.booking_fee_currency ?: ""} ${d.profile.booking_fee ?: "0"} • ${d.profile.session_duration_minutes ?: 30} min", "#EFBF04", 13f, top = 4))

        root.addView(tv("Upcoming bookings", size = 16f, bold = true, top = 28))
        if (d.upcoming_bookings.isEmpty()) root.addView(tv("None", "#9CA3AF", 13f, top = 8))
        else d.upcoming_bookings.forEach { b ->
            root.addView(tv("${b.scheduled_at.take(16).replace("T", " ")}  •  ${b.client_name ?: "Client"}", "#E5E7EB", 13f, top = 8))
        }

        root.addView(tv("Weekly availability", size = 16f, bold = true, top = 28))
        renderAvailabilityEditor()

        root.addView(MaterialButton(this).apply {
            text = "Save Availability"; setBackgroundColor(Color.parseColor("#EFBF04")); setTextColor(Color.parseColor("#1A1A1A"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.topMargin = 24; layoutParams = lp
            setOnClickListener { viewModel.saveAvailability(working.toList()) }
        })
    }

    private val editorHolder by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL } }

    private fun renderAvailabilityEditor() {
        if (editorHolder.parent == null) root.addView(editorHolder)
        editorHolder.removeAllViews()
        for (dow in 0..6) {
            val dayRow = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 16, 0, 0) }
            val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            head.addView(tv(days[dow], "#FFFFFF", 15f, bold = true).apply { layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
            head.addView(TextView(this).apply {
                text = "+ Add"; setTextColor(Color.parseColor("#EFBF04"))
                setOnClickListener {
                    working.add(AvailabilitySlot(day_of_week = dow, start_time = "09:00", end_time = "10:00"))
                    renderAvailabilityEditor()
                }
            })
            dayRow.addView(head)

            working.filter { it.day_of_week == dow }.forEach { slot ->
                val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 8, 0, 0) }
                val startEt = EditText(this).apply { setText(slot.start_time); setTextColor(Color.WHITE); hint = "HH:mm" }
                val endEt = EditText(this).apply { setText(slot.end_time); setTextColor(Color.WHITE); hint = "HH:mm" }
                line.addView(startEt, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                line.addView(tv(" to ", "#9CA3AF"))
                line.addView(endEt, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                line.addView(TextView(this).apply {
                    text = "✕"; setTextColor(Color.parseColor("#EF4444")); setPadding(24, 0, 0, 0)
                    setOnClickListener { working.remove(slot); renderAvailabilityEditor() }
                })
                // Keep edits in the working model
                startEt.setOnFocusChangeListener { _, has -> if (!has) updateSlot(slot, startEt.text.toString(), endEt.text.toString()) }
                endEt.setOnFocusChangeListener { _, has -> if (!has) updateSlot(slot, startEt.text.toString(), endEt.text.toString()) }
                dayRow.addView(line)
            }
            editorHolder.addView(dayRow)
        }
    }

    private fun updateSlot(slot: AvailabilitySlot, start: String, end: String) {
        val idx = working.indexOf(slot)
        if (idx >= 0) working[idx] = slot.copy(start_time = start, end_time = end)
    }
}
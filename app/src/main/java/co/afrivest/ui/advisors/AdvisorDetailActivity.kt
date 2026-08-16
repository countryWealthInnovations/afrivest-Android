package co.afrivest.ui.advisors

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.model.Advisor
import co.afrivest.data.model.AdvisorSlot
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvisorDetailActivity : AppCompatActivity() {
    private val viewModel: AdvisorViewModel by viewModels()
    private var advisorId: Int = -1
    private var selected: AdvisorSlot? = null
    private lateinit var root: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var slotsContainer: LinearLayout
    private lateinit var bookButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        advisorId = intent.getIntExtra("advisor_id", -1)
        if (advisorId < 0) { finish(); return }

        val scroll = android.widget.ScrollView(this).apply { setBackgroundColor(Color.parseColor("#1A1A1A")) }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 64, 48, 48) }
        progress = ProgressBar(this).apply { visibility = View.GONE }
        slotsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        bookButton = MaterialButton(this).apply {
            text = "Book Session"; isEnabled = false
            setBackgroundColor(Color.parseColor("#EFBF04")); setTextColor(Color.parseColor("#1A1A1A"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = 32; layoutParams = lp
            setOnClickListener { confirmBook() }
        }
        root.addView(progress)
        scroll.addView(root)
        setContentView(scroll)

        observe()
        viewModel.loadDetail(advisorId)
    }

    private fun observe() {
        viewModel.loading.observe(this) { progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(this) { it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() } }
        viewModel.detail.observe(this) { it?.let { a -> render(a) } }
        viewModel.booked.observe(this) { b ->
            b?.let {
                viewModel.clearBooked()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Booking confirmed")
                    .setMessage("Your session is confirmed.\nMeeting link:\n${it.meeting_link ?: ""}")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
        }
    }

    private fun render(a: Advisor) {
        root.removeAllViews(); root.addView(progress)
        fun label(t: String, c: String = "#FFFFFF", size: Float = 14f, bold: Boolean = false, topPad: Int = 0) =
            TextView(this).apply { text = t; setTextColor(Color.parseColor(c)); textSize = size; setPadding(0, topPad, 0, 0); if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD) }

        root.addView(label(a.display_name, size = 22f, bold = true))
        a.title?.let { root.addView(label(it, "#9CA3AF", 14f, topPad = 4)) }
        a.bio?.let { root.addView(label(it, "#E5E7EB", 14f, topPad = 16)) }
        root.addView(label("Fee: ${a.booking_fee_currency ?: ""} ${a.booking_fee ?: "0"} • ${a.session_duration_minutes ?: 30} min", "#EFBF04", 14f, topPad = 16))
        root.addView(label("Available slots", "#FFFFFF", 16f, bold = true, topPad = 24))

        slotsContainer.removeAllViews()
        val slots = a.slots ?: emptyList()
        if (slots.isEmpty()) {
            slotsContainer.addView(label("No open slots in the next two weeks", "#9CA3AF", 13f, topPad = 8))
        } else {
            slots.groupBy { it.date }.forEach { (date, daySlots) ->
                slotsContainer.addView(label(date, "#9CA3AF", 12f, topPad = 16))
                // Wrap slot chips across rows of 3
                daySlots.chunked(3).forEach { rowSlots ->
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.topMargin = 12
                        layoutParams = lp
                    }
                    rowSlots.forEach { slot ->
                        val chip = MaterialButton(this).apply {
                            text = slot.start
                            setTextColor(Color.parseColor("#EFBF04"))
                            setBackgroundColor(Color.parseColor("#2F2F2F"))
                            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            lp.marginEnd = 16
                            layoutParams = lp
                            setOnClickListener {
                                selected = slot
                                bookButton.isEnabled = true
                                bookButton.text = "Book ${slot.date} ${slot.start}"
                            }
                        }
                        row.addView(chip)
                    }
                    // Pad short rows so widths stay even
                    repeat(3 - rowSlots.size) {
                        row.addView(View(this).apply {
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        })
                    }
                    slotsContainer.addView(row)
                }
            }
        }
        root.addView(slotsContainer)
        root.addView(bookButton)
    }

    private fun confirmBook() {
        val slot = selected ?: return
        viewModel.book(advisorId, slot.datetime, null)
    }
}
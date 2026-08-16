package co.afrivest.ui.advisors

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.model.AdvisorBookingDto
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyBookingsActivity : AppCompatActivity() {
    private val viewModel: AdvisorViewModel by viewModels()
    private lateinit var container: LinearLayout
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = android.widget.ScrollView(this).apply { setBackgroundColor(Color.parseColor("#1A1A1A")) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 64, 48, 48) }
        root.addView(TextView(this).apply { text = "My Bookings"; setTextColor(Color.WHITE); textSize = 22f; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        progress = ProgressBar(this).apply { visibility = View.GONE }
        container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 24, 0, 0) }
        root.addView(progress); root.addView(container)
        scroll.addView(root); setContentView(scroll)

        viewModel.loading.observe(this) { progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.bookings.observe(this) { render(it) }
        viewModel.loadMyBookings()
    }

    private fun render(list: List<AdvisorBookingDto>) {
        container.removeAllViews()
        if (list.isEmpty()) {
            container.addView(TextView(this).apply { text = "No bookings yet"; setTextColor(Color.parseColor("#9CA3AF")); gravity = Gravity.CENTER })
            return
        }
        list.forEach { b ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#2F2F2F")); setPadding(40, 36, 40, 36)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.bottomMargin = 24; layoutParams = lp
            }
            card.addView(TextView(this).apply { text = b.advisor?.display_name ?: "Advisor session"; setTextColor(Color.WHITE); textSize = 16f; setTypeface(typeface, android.graphics.Typeface.BOLD) })
            card.addView(TextView(this).apply { text = "${b.scheduled_at.take(16).replace("T", " ")}  •  ${b.status}"; setTextColor(Color.parseColor("#9CA3AF")); textSize = 13f; setPadding(0, 8, 0, 0) })
            b.meeting_link?.let { link ->
                card.addView(MaterialButton(this).apply {
                    text = "Join meeting"; setBackgroundColor(Color.parseColor("#EFBF04")); setTextColor(Color.parseColor("#1A1A1A"))
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.topMargin = 16; layoutParams = lp
                    setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                })
            }
            container.addView(card)
        }
    }
}
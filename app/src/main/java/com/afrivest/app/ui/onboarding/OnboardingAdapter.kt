package com.afrivest.app.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(
    private val pages: List<OnboardingPage>,
    private val onSkip: () -> Unit,
    private val onNext: (Int) -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private var currentPage = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position], position, pages.size)
    }

    override fun getItemCount(): Int = pages.size

    fun updateCurrentPage(page: Int) {
        currentPage = page
        notifyDataSetChanged()
    }

    inner class OnboardingViewHolder(
        private val binding: ItemOnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPage, position: Int, totalPages: Int) {
            // Set image
            binding.ivHero.setImageResource(page.imageRes)

            // Set title and description
            binding.tvTitle.text = page.title
            binding.tvDescription.text = page.description

            // Setup page indicators
            setupIndicators(position, totalPages)

            // Show/hide skip button
            binding.btnSkip.visibility = if (position < totalPages - 1) View.VISIBLE else View.GONE
            binding.btnSkip.setOnClickListener { onSkip() }

            // Setup CTA button
            binding.btnCta.text = if (position == totalPages - 1) "Get Started" else "Next"
            binding.btnCta.setOnClickListener { onNext(position) }
        }

        private fun setupIndicators(currentPosition: Int, totalPages: Int) {
            binding.indicatorContainer.removeAllViews()

            for (i in 0 until totalPages) {
                val indicator = View(binding.root.context).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(
                        if (i == currentPosition) 24 else 8,
                        8
                    ).apply {
                        marginEnd = 8
                    }
                    setBackgroundResource(
                        if (i == currentPosition)
                            com.afrivest.app.R.drawable.bg_indicator_active
                        else
                            com.afrivest.app.R.drawable.bg_indicator_inactive
                    )
                }
                binding.indicatorContainer.addView(indicator)
            }
        }
    }
}
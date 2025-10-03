package com.afrivest.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityOnboardingBinding
import com.afrivest.app.ui.auth.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()
    private lateinit var adapter: OnboardingAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var autoSlideRunnable: Runnable? = null
    private val AUTO_SLIDE_DELAY = 5000L // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        observeNavigation()
        startAutoSlide()
    }

    private fun setupViewPager() {
        val pages = listOf(
            OnboardingPage(
                imageRes = R.drawable.onboarding_real_estate,
                title = getString(R.string.onboarding_title_1),
                description = getString(R.string.onboarding_desc_1)
            ),
            OnboardingPage(
                imageRes = R.drawable.onboarding_insurance,
                title = getString(R.string.onboarding_title_2),
                description = getString(R.string.onboarding_desc_2)
            ),
            OnboardingPage(
                imageRes = R.drawable.onboarding_gold,
                title = getString(R.string.onboarding_title_3),
                description = getString(R.string.onboarding_desc_3)
            )
        )

        adapter = OnboardingAdapter(
            pages = pages,
            onSkip = {
                stopAutoSlide()
                viewModel.skipOnboarding()
            },
            onNext = { currentPage ->
                stopAutoSlide()
                if (currentPage < pages.size - 1) {
                    binding.viewPager.currentItem = currentPage + 1
                    startAutoSlide()
                } else {
                    viewModel.completeOnboarding()
                }
            }
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapter.updateCurrentPage(position)

                // Restart auto-slide timer when page changes
                stopAutoSlide()
                if (position < pages.size - 1) {
                    startAutoSlide()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                // Pause auto-slide when user is manually swiping
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopAutoSlide()
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val currentPosition = binding.viewPager.currentItem
                    if (currentPosition < pages.size - 1) {
                        startAutoSlide()
                    }
                }
            }
        })
    }

    private fun startAutoSlide() {
        autoSlideRunnable = Runnable {
            val currentItem = binding.viewPager.currentItem
            val pageCount = adapter.itemCount

            if (currentItem < pageCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                // Last page reached, complete onboarding
                viewModel.completeOnboarding()
            }
        }
        handler.postDelayed(autoSlideRunnable!!, AUTO_SLIDE_DELAY)
    }

    private fun stopAutoSlide() {
        autoSlideRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun observeNavigation() {
        viewModel.shouldNavigateToAuth.observe(this) { navigate ->
            if (navigate) {
                navigateToAuth()
            }
        }
    }

    private fun navigateToAuth() {
        // Navigate to Register screen (user can go to Login from there)
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onPause() {
        super.onPause()
        stopAutoSlide()
    }

    override fun onResume() {
        super.onResume()
        val currentPosition = binding.viewPager.currentItem
        val pageCount = adapter.itemCount
        if (currentPosition < pageCount - 1) {
            startAutoSlide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoSlide()
    }
}

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)
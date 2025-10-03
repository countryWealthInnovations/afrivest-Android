package com.afrivest.app.ui.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afrivest.app.databinding.ActivitySplashBinding
import com.afrivest.app.ui.auth.LoginActivity
import com.afrivest.app.ui.main.MainActivity
import com.afrivest.app.ui.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        observeNavigation()

        // Check auth status after animation
        lifecycleScope.launch {
            delay(2500) // Wait for animation
            viewModel.checkAuthStatus()
        }
    }

    private fun setupAnimations() {
        // Money bag scale animation
        binding.ivMoneyBag.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
        }

        // App name fade animation
        binding.tvAppName.alpha = 0f

        // Footer fade animation
        binding.tvFooter.alpha = 0f

        // Animate money bag
        ObjectAnimator.ofFloat(binding.ivMoneyBag, "scaleX", 0.8f, 1.0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(binding.ivMoneyBag, "scaleY", 0.8f, 1.0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(binding.ivMoneyBag, "alpha", 0f, 1f).apply {
            duration = 600
            start()
        }

        // Animate app name
        lifecycleScope.launch {
            delay(300)
            ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f).apply {
                duration = 500
                start()
            }
        }

        // Animate footer
        lifecycleScope.launch {
            delay(500)
            ObjectAnimator.ofFloat(binding.tvFooter, "alpha", 0f, 1f).apply {
                duration = 500
                start()
            }
        }
    }

    private fun observeNavigation() {
        viewModel.navigateTo.observe(this) { destination ->
            destination?.let {
                when (it) {
                    SplashViewModel.NavigationDestination.ONBOARDING -> {
                        navigateToOnboarding()
                    }
                    SplashViewModel.NavigationDestination.LOGIN -> {
                        navigateToLogin()
                    }
                    SplashViewModel.NavigationDestination.DASHBOARD -> {
                        navigateToDashboard()
                    }
                }
            }
        }
    }

    private fun navigateToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
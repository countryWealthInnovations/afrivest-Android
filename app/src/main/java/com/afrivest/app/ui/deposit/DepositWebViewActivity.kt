package com.afrivest.app.ui.deposit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.databinding.ActivityDepositWebviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositWebviewBinding
    private var transactionId: Int = 0
    private var reference: String = ""
    private var amount: String = ""
    private var currency: String = ""
    private var network: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get extras
        transactionId = intent.getIntExtra("TRANSACTION_ID", 0)
        reference = intent.getStringExtra("REFERENCE") ?: ""
        amount = intent.getStringExtra("AMOUNT") ?: ""
        currency = intent.getStringExtra("CURRENCY") ?: ""
        network = intent.getStringExtra("NETWORK") ?: ""
        val paymentUrl = intent.getStringExtra("PAYMENT_URL") ?: ""

        setupToolbar()
        setupWebView(paymentUrl)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Complete Payment"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(paymentUrl: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                    android.util.Log.d("WebViewDebug", "Page started loading: $url")
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()
                    android.util.Log.d("WebViewDebug", "Navigating to: $url")

                    // Check if this is the return URL
                    if (url.contains("/api/deposits/return")) {
                        // Payment completed, show processing message
                        navigateToProcessingScreen()
                        return true
                    }

                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                    android.util.Log.d("WebViewDebug", "Page finished loading: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    android.util.Log.e("WebViewDebug", "Error loading page: ${error?.description}")
                }
            }

            loadUrl(paymentUrl)
        }
    }

    private fun navigateToProcessingScreen() {
        val intent = Intent(this, DepositProcessingActivity::class.java).apply {
            putExtra("REFERENCE", reference)
            putExtra("AMOUNT", amount)
            putExtra("CURRENCY", currency)
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            // User cancelled
            navigateToProcessingScreen()
        }
    }
}
package co.afrivest.ui.kyc

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import co.afrivest.R
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.didit.sdk.DiditSdk
import me.didit.sdk.DiditSdkState
import me.didit.sdk.VerificationResult
import me.didit.sdk.VerificationStatus

@AndroidEntryPoint
class KycActivity : AppCompatActivity() {

    private val viewModel: KycViewModel by viewModels()

    private lateinit var statusIcon: ImageView
    private lateinit var statusTitle: TextView
    private lateinit var statusSubtitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var retryButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc)

        statusIcon = findViewById(R.id.statusIcon)
        statusTitle = findViewById(R.id.statusTitle)
        statusSubtitle = findViewById(R.id.statusSubtitle)
        progressBar = findViewById(R.id.progressBar)
        retryButton = findViewById(R.id.retryButton)

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        retryButton.setOnClickListener { viewModel.createSession() }

        observe()

        // Auto-start. The banner already brought the user here, no extra button needed.
        viewModel.createSession()
    }

    private fun observe() {
        viewModel.phase.observe(this) { render(it) }
        viewModel.errorMessage.observe(this) { msg ->
            msg?.let { statusSubtitle.text = it; statusSubtitle.visibility = View.VISIBLE }
        }
        viewModel.sessionToken.observe(this) { token -> token?.let { startSdk(it) } }
    }

    private fun startSdk(token: String) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                DiditSdk.state.collect { state ->
                    when (state) {
                        is DiditSdkState.Ready -> DiditSdk.launchVerificationUI(this@KycActivity)
                        is DiditSdkState.Error -> viewModel.onSdkError(state.message)
                        else -> {}
                    }
                }
            }
        }
        DiditSdk.startVerification(token = token) { result -> handleResult(result) }
        viewModel.clearToken()
    }

    private fun handleResult(result: VerificationResult) {
        runOnUiThread {
            when (result) {
                is VerificationResult.Completed -> when (result.session.status) {
                    VerificationStatus.APPROVED -> viewModel.onSdkApproved()
                    VerificationStatus.DECLINED -> viewModel.onSdkDeclined()
                    VerificationStatus.PENDING -> viewModel.onSdkPending()
                }
                is VerificationResult.Cancelled -> viewModel.onSdkCancelled()
                is VerificationResult.Failed -> viewModel.onSdkError(result.error.message)
            }
        }
    }

    private fun render(phase: KycViewModel.Phase) {
        val busy = phase == KycViewModel.Phase.CREATING_SESSION || phase == KycViewModel.Phase.VERIFYING
        progressBar.visibility = if (busy) View.VISIBLE else View.GONE

        val showRetry = phase == KycViewModel.Phase.DECLINED ||
                phase == KycViewModel.Phase.CANCELLED ||
                phase == KycViewModel.Phase.ERROR
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE

        if (phase != KycViewModel.Phase.ERROR) statusSubtitle.visibility = View.GONE

        when (phase) {
            KycViewModel.Phase.IDLE, KycViewModel.Phase.CREATING_SESSION ->
                statusTitle.text = "Starting verification"
            KycViewModel.Phase.VERIFYING -> statusTitle.text = "Verification in progress"
            KycViewModel.Phase.APPROVED -> {
                statusTitle.text = "Identity Verified"
                setResult(RESULT_OK)
                finish()
            }
            KycViewModel.Phase.DECLINED -> {
                statusTitle.text = "Verification Declined"
                statusSubtitle.text = "We could not verify your identity. Try again or contact support."
                statusSubtitle.visibility = View.VISIBLE
                retryButton.text = "Try Again"
            }
            KycViewModel.Phase.PENDING -> {
                statusTitle.text = "Under Review"
                statusSubtitle.text = "Our compliance team is reviewing your submission."
                statusSubtitle.visibility = View.VISIBLE
            }
            KycViewModel.Phase.CANCELLED -> {
                statusTitle.text = "Verification Cancelled"
                statusSubtitle.text = "You closed the flow before finishing."
                statusSubtitle.visibility = View.VISIBLE
                retryButton.text = "Try Again"
            }
            KycViewModel.Phase.ERROR -> {
                statusTitle.text = "Something Went Wrong"
                statusSubtitle.visibility = View.VISIBLE
                retryButton.text = "Try Again"
            }
        }
    }
}
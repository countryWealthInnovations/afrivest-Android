package com.afrivest.app.services.payment

import android.app.Activity
import android.content.Intent
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants

class FlutterwaveSDKManager {

    companion object {
        const val RAVE_REQUEST_CODE = RaveConstants.RAVE_REQUEST_CODE
    }

    fun initiatePayment(
        activity: Activity,
        amount: Double,
        currency: String,
        txRef: String,
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        publicKey: String,
        encryptionKey: String,
        isStaging: Boolean = false
    ) {
        // Create meta data
        val meta = listOf(
            Meta("sdk", "android"),
            Meta("platform", "mobile")
        )

        RaveUiManager(activity)
            .setAmount(amount)
            .setCurrency(currency)
            .setEmail(email)
            .setfName(firstName)
            .setlName(lastName)
            .setNarration("AfriVest Deposit")
            .setPublicKey(publicKey)
            .setEncryptionKey(encryptionKey)
            .setTxRef(txRef)
            .setPhoneNumber(phoneNumber, true)
            // Enable ALL payment methods
            .acceptAccountPayments(true)
            .acceptCardPayments(true)
            .acceptMpesaPayments(true)
            .acceptAchPayments(true)
            .acceptGHMobileMoneyPayments(true)
            .acceptUgMobileMoneyPayments(true)
            .acceptZmMobileMoneyPayments(true)
            .acceptRwfMobileMoneyPayments(true)
            .acceptSaBankPayments(true)
            .acceptUkPayments(true)
            .acceptBankTransferPayments(true)
            .acceptUssdPayments(true)
            .acceptBarterPayments(true)
            .acceptFrancMobileMoneyPayments(true, "UG") // For Uganda
            .allowSaveCardFeature(true)
            .onStagingEnv(isStaging)
            .setMeta(meta)
            .shouldDisplayFee(true)
            .showStagingLabel(isStaging)
            .isPreAuth(false)
            .initialize()
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: (PaymentResult) -> Unit
    ) {
        if (requestCode == RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")

            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    callback(PaymentResult.Success(message ?: ""))
                }
                RavePayActivity.RESULT_ERROR -> {
                    callback(PaymentResult.Failed(message ?: "Payment failed"))
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    callback(PaymentResult.Cancelled)
                }
            }
        }
    }

    sealed class PaymentResult {
        data class Success(val flwRef: String) : PaymentResult()
        data class Failed(val message: String) : PaymentResult()
        object Cancelled : PaymentResult()
    }
}
package com.fingerprint.auth

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

@TargetApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val context: Context) : FingerprintManager.AuthenticationCallback() {

    fun startAuth(
        fingerprintManager: FingerprintManager,
        cryptoObject: FingerprintManager.CryptoObject?
    ) {
        val cancellationSignal = CancellationSignal()
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    override fun onAuthenticationError(
        errorCode: Int,
        errString: CharSequence
    ) {
        update("There was an Auth Error. $errString", false)
    }

    override fun onAuthenticationFailed() {
        update("Auth Failed. ", false)
    }

    override fun onAuthenticationHelp(
        helpCode: Int,
        helpString: CharSequence
    ) {
        update("Error: $helpString", false)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        update("You can now access the app.", true)
    }

    private fun update(s: String, b: Boolean) {
        val paraLabel =
            (context as Activity).findViewById<View>(R.id.text) as TextView
        val imageView: ImageView =
            context.findViewById<View>(R.id.fingerprintImg) as ImageView
        paraLabel.text = s
        if (b == false) {
            paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            imageView.setImageResource(R.mipmap.ic_check_circle_black_24dp)
        }
    }



}
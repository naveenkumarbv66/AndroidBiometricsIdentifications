package com.test.fringerprints.latest.lib.biometricscreen

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

object BiometricUtility {
    private const val TAG = "BiometricUtility"
    private const val BIOMETRIC_SUCCESS = "Biometric Success."
    private const val HARDWARE_UNAVAILABLE_TRY_AGAIN =
        "The hardware is unavailable. Try again later."
    private const val DOES_NOT_HAVE_BIOMETRIC_ENROLLED =
        "The user does not have any biometrics enrolled."
    private const val NOT_ABLE_TO_AUTHENTICATE_TRY_LATER =
        "Not able to authenticate. Try again later."

    fun createPromptInfo(
        title: String,
        subTitle: String,
        description: String,
        negativeButtonText: String
    ): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subTitle)
            setDescription(description)
            setConfirmationRequired(false)
            setNegativeButtonText(negativeButtonText)
        }.build()

    //on thread => ContextCompat.getMainExecutor(activity)
    //On main thread => ContextCompat.getMainExecutor(this)
    fun createBiometricPrompt(
        activity: AppCompatActivity,
        processSuccess: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                if (errCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Log.d(TAG, "user clicked negative button.")
                } else {
                    Log.d(
                        TAG,
                        "Called when an unrecoverable error has been encountered and the operation is complete."
                    )
                }
                Log.d(TAG, "errCode is $errCode and errString is: $errString")
                processSuccess.onAuthenticationError(errCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "User biometric is valid but not recognized or rejected.")
                processSuccess.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "User biometric authentication was successful")
                processSuccess.onAuthenticationSucceeded(result)
            }
        })

    fun deviceBiometricStatus(
        context: Context,
        deviceBiometricStatusCallBack: DeviceBiometricStatusCallBack
    ) {
        when (getDeviceBiometricStatus(context)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, BIOMETRIC_SUCCESS)
                deviceBiometricStatusCallBack.onAuthenticateSuccess()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, HARDWARE_UNAVAILABLE_TRY_AGAIN)
                deviceBiometricStatusCallBack.onError(HARDWARE_UNAVAILABLE_TRY_AGAIN)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, DOES_NOT_HAVE_BIOMETRIC_ENROLLED)
                deviceBiometricStatusCallBack.onErrorNoBiometric(DOES_NOT_HAVE_BIOMETRIC_ENROLLED)
                /*
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, 99)
                 */
            }

            else -> {
                Log.d(TAG, NOT_ABLE_TO_AUTHENTICATE_TRY_LATER)
                deviceBiometricStatusCallBack.onError(NOT_ABLE_TO_AUTHENTICATE_TRY_LATER)
            }
        }
    }

    private fun getDeviceBiometricStatus(context: Context) = BiometricManager.from(context)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

    interface DeviceBiometricStatusCallBack {
        fun onAuthenticateSuccess()
        fun onError(errorMessage: String)
        fun onErrorNoBiometric(errorMessage: String)
    }
}
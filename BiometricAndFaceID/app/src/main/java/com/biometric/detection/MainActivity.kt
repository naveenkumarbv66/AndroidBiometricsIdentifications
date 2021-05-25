package com.biometric.detection

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var PRIVATE_MODE = 0
    private val IS_BIOMETRIC_ENABLED = "isBiometricEnabled"
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(IS_BIOMETRIC_ENABLED, PRIVATE_MODE)

        enableBiometricOption.setOnClickListener {
            BiometricUtility.deviceBiometricStatus(application,
                    object : BiometricUtility.DeviceBiometricStatusCallBack {
                        override fun onAuthenticateSuccess() {
                            enableBiometricOption.isClickable = true
                            if (sharedPref.getBoolean(
                                            IS_BIOMETRIC_ENABLED,
                                            false
                                    )
                            ) enableBiometricOption.text = "Turn off Biometric option"
                            else enableBiometricOption.text = "Turn on Biometric option"
                            BiometricUtility.createBiometricPrompt(
                                    this@MainActivity,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationError(
                                                errorCode: Int,
                                                errString: CharSequence
                                        ) {
                                            super.onAuthenticationError(errorCode, errString)
                                            Toast.makeText(
                                                    application,
                                                    "User canceled.",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                            Toast.makeText(
                                                    application,
                                                    "onAuthenticationFailed",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            Toast.makeText(
                                                    application,
                                                    "onAuthenticationSucceeded",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                            sharedPref.edit().putBoolean(
                                                    IS_BIOMETRIC_ENABLED,
                                                    !sharedPref.getBoolean(IS_BIOMETRIC_ENABLED, false)
                                            ).apply()

                                            if (sharedPref.getBoolean(
                                                            IS_BIOMETRIC_ENABLED,
                                                            false
                                                    )
                                            ) enableBiometricOption.text = "Turn off Biometric option"
                                            else enableBiometricOption.text = "Turn on Biometric option"
                                        }
                                    }).authenticate(
                                    BiometricUtility.createPromptInfo(
                                            "App Name.",
                                            "Please authenticate to enable biometric login",
                                            "Please authenticate to enable biometric login",
                                            "Cancel"
                                    )
                            )
                        }

                        override fun onError(errorMessage: String) {
                            enableBiometricOption.isClickable = false
                            enableBiometricOption.text = errorMessage
                        }

                        override fun onErrorNoBiometric(errorMessage: String) {
                            enableBiometricOption.isClickable = false
                            enableBiometricOption.text = errorMessage
                        }

                    })
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedPref.getBoolean(IS_BIOMETRIC_ENABLED, false)) {
            enableBiometricOption.text = "Turn off Biometric option"
            biometricAuthentication()
        } else enableBiometricOption.text = "Turn on Biometric option"
    }

    private fun biometricAuthentication() {
        BiometricUtility.createBiometricPrompt(
                this,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        var localErrorMessage: String
                        when (errorCode) {
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                localErrorMessage = "The user pressed the negative button."
                                enableBiometricOption.isClickable = true
                            }
                            BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                                localErrorMessage = "The user does not have any biometrics enrolled."
                                enableBiometricOption.isClickable = true
                            }
                            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                                localErrorMessage = "The device does not have pin, pattern, or password set up."
                                enableBiometricOption.isClickable = false
                            }
                            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                                localErrorMessage = "The device does not have the required authentication hardware."
                                enableBiometricOption.isClickable = false
                            }
                            BiometricPrompt.ERROR_USER_CANCELED -> {
                                localErrorMessage = "The user canceled the operation."
                                enableBiometricOption.isClickable = true
                            }
                            else -> {
                                localErrorMessage = "The sensor was unable to process the current image, Try later"
                                enableBiometricOption.isClickable = true
                            }
                        }
                        enableBiometricOption.text = localErrorMessage
                        Toast.makeText(application, localErrorMessage, Toast.LENGTH_LONG).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(application, "onAuthenticationFailed", Toast.LENGTH_LONG).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(application, "onAuthenticationSucceeded", Toast.LENGTH_LONG)
                                .show()
                    }
                }).authenticate(
                BiometricUtility.createPromptInfo(
                        "App Name.", "Place your finger on the fingerprint scanner to login.",
                        "Set the description to display", "Use Passcode"
                )
        )
    }
}
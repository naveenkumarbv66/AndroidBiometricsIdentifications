package com.test.fringerprints.latest.lib.biometricscreen

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.test.fringerprints.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivityTest : AppCompatActivity() {
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
                        BiometricUtility.createBiometricPrompt(
                            this@MainActivityTest,
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
                        Toast.makeText(
                            application,
                            "Error : ".plus(errorMessage),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onErrorNoBiometric(errorMessage: String) {
                        Toast.makeText(
                            application,
                            "Error : ".plus(errorMessage),
                            Toast.LENGTH_LONG
                        ).show()
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
                    Toast.makeText(application, "User canceled.", Toast.LENGTH_LONG).show()
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
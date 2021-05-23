package com.test.fringerprints

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.test.fringerprints.biometric.BiometricPromptUtils
import com.test.fringerprints.biometric.CIPHERTEXT_WRAPPER
import com.test.fringerprints.biometric.CryptographyManager
import com.test.fringerprints.biometric.SHARED_PREFS_FILENAME
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var biometricPrompt: BiometricPrompt
    private val cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when(BiometricManager.from(applicationContext).canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                Log.d("Naveen","No error detected. => BIOMETRIC_SUCCESS")
                Toast.makeText(applicationContext, "No error detected. => BIOMETRIC_SUCCESS", Toast.LENGTH_LONG).show()

                enableBiometricOption.visibility = View.VISIBLE

                if (ciphertextWrapper != null) {
                    showBiometricPromptForDecryption()
                } else {
                    //Stay in Login activity
                    //startActivity(Intent(this, EnableBiometricActivity::class.java))
                }

            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Log.d("Naveen","The hardware is unavailable. Try again later.")
                Toast.makeText(applicationContext, "The hardware is unavailable. Try again later.", Toast.LENGTH_LONG).show()
                enableBiometricOption.visibility = View.GONE
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                Log.d("Naveen"," The user does not have any biometrics enrolled.")
                Toast.makeText(applicationContext, "The user does not have any biometrics enrolled.", Toast.LENGTH_LONG).show()
                enableBiometricOption.visibility = View.VISIBLE
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Log.d("Naveen","There is no biometric hardware.")
                Toast.makeText(applicationContext, "There is no biometric hardware.", Toast.LENGTH_LONG).show()
                enableBiometricOption.visibility = View.GONE
            }

            else ->{
                Log.d("Naveen","Issue in canAuthenticate()")
                Toast.makeText(applicationContext, "Issue in canAuthenticate().", Toast.LENGTH_LONG).show()
                enableBiometricOption.visibility = View.GONE
            }
        }

        if (ciphertextWrapper == null) {
           // setupForLoginWithPassword()
        }
    }

    override fun onResume() {
        super.onResume()
        enableBiometricOption.setOnClickListener{
            startActivity(Intent(this,  EnableBiometricActivity::class.java))
        }

        if (ciphertextWrapper != null) {
            showBiometricPromptForDecryption()
          /*  if (SampleAppUser.fakeToken == null) {
          //SampleAppUser is singletone object. Which Object class
                showBiometricPromptForDecryption()
            } else {
                // The user has already logged in, so proceed to the rest of the app
                // this is a todo for you, the developer
                updateApp(getString(R.string.already_signedin))
            }*/
        }
    }

    // BIOMETRICS SECTION

    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )
            biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                    this,
                    ::decryptServerTokenFromStorage
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
               // SampleAppUser.fakeToken = plaintext
                // Now that you have the token, you can query server for everything else
                // the only reason we call this fakeToken is because we didn't really get it from
                // the server. In your case, you will have gotten it from the server the first time
                // and therefore, it's a real token.

                updateApp(plaintext)
            }
        }
    }

    private fun updateApp(successMsg: String) {
        success.text = successMsg
    }
}
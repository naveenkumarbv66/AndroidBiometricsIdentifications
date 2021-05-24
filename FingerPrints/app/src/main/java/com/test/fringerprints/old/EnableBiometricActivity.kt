package com.test.fringerprints.old

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.test.fringerprints.R
import com.test.fringerprints.old.biometric.BiometricPromptUtils
import com.test.fringerprints.old.biometric.CIPHERTEXT_WRAPPER
import com.test.fringerprints.old.biometric.CryptographyManager
import com.test.fringerprints.old.biometric.SHARED_PREFS_FILENAME
import kotlinx.android.synthetic.main.enable_biometric_activity.*

class EnableBiometricActivity : AppCompatActivity() {
    private lateinit var cryptographyManager: CryptographyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enable_biometric_activity)
    }

    override fun onResume() {
        super.onResume()
        enableBiometricOption.setOnClickListener{
            if(!serverTokenByUser.text.toString().isNullOrEmpty()){
                showBiometricPromptForEncryption()
            }else Toast.makeText(applicationContext, "Please enter the fake server token", Toast.LENGTH_LONG).show()
        }
    }

    private fun showBiometricPromptForEncryption() {
      //  val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        when(BiometricManager.from(applicationContext).canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                Log.d("Naveen","No error detected. => BIOMETRIC_SUCCESS")
                Toast.makeText(applicationContext, "No error detected. => BIOMETRIC_SUCCESS", Toast.LENGTH_LONG).show()
                val secretKeyName = getString(R.string.secret_key_name)
                cryptographyManager = CryptographyManager()
                val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
                val biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
                val promptInfo = BiometricPromptUtils.createPromptInfo(this)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Log.d("Naveen","The hardware is unavailable. Try again later.")
                Toast.makeText(applicationContext, "The hardware is unavailable. Try again later.", Toast.LENGTH_LONG).show()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                Log.d("Naveen"," The user does not have any biometrics enrolled.")
                Toast.makeText(applicationContext, "The user does not have any biometrics enrolled.", Toast.LENGTH_LONG).show()

                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, 99)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Log.d("Naveen","There is no biometric hardware.")
                Toast.makeText(applicationContext, "There is no biometric hardware.", Toast.LENGTH_LONG).show()
            }

            else ->{
                Log.d("Naveen","Issue in canAuthenticate()")
                Toast.makeText(applicationContext, "Issue in canAuthenticate().", Toast.LENGTH_LONG).show()
            }
        }
        /*if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }*/
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            serverTokenByUser.text.toString().let { token ->
                Log.d("Naveen","The token from server is $token")
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            showBiometricPromptForEncryption()
        }else{
            Toast.makeText(applicationContext, "User canceled.", Toast.LENGTH_LONG).show()
        }
    }
}
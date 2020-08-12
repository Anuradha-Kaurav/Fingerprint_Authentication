package com.fingerprint.auth

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    lateinit var fingerImg: ImageView
    lateinit var text: TextView
    lateinit var fingerprintManager: FingerprintManager
    lateinit var keyguardManager: KeyguardManager

    lateinit var keyStore: KeyStore
    lateinit var cipher: Cipher
    private val KEY_NAME = "AndroidKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fingerImg = findViewById(R.id.fingerprintImg)
        text = findViewById(R.id.text)

        // Check 1: Android version should be greater or equal to Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Have permission to use fingerprint scanner in the app
        // Check 4: Lock screen is secured with atleast 1 type of lock
        // Check 5: Atleast 1 Fingerprint is registered

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            fingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
            keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

            if(!fingerprintManager.isHardwareDetected()){
                text.text= "Fingerprint Scanner not detected in Device"
            }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                text.text = "Permission not granted to use Fingerprint Scanner"
            }else if(!keyguardManager.isKeyguardSecure()){
                text.text = "Add Lock to your Phone in Settings"
            }else if(!fingerprintManager.hasEnrolledFingerprints()){
                text.text = "You should add atleast 1 Fingerprint to use this Feature"
            }else{
                text.text = "Place your finger on Scanner to access the app"

                generateKey()

                if(cipherInit()){
                    val cryptoObject: FingerprintManager.CryptoObject = FingerprintManager.CryptoObject(cipher)
                    val fingerprintHandler = FingerprintHandler(this)
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject)
                }
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey(){
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            var keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore.load(null)

            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )

            keyGenerator.generateKey()
        }catch (e: KeyStoreException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }catch (e: CertificateException){
            e.printStackTrace()
        }catch (e: NoSuchAlgorithmException){
            e.printStackTrace()
        }catch (e: InvalidAlgorithmParameterException){
            e.printStackTrace()
        }catch (e: NoSuchProviderException){
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public fun cipherInit(): Boolean{
        cipher = try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try{
            keyStore.load(null)
           val key: SecretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return  true
        }catch (e: KeyPermanentlyInvalidatedException){
            return false
        }
        catch (e: KeyStoreException){
            throw RuntimeException("Failed to init Cipher", e)
        }catch (e: IOException){
            throw RuntimeException("Failed to init Cipher", e)
        }catch (e: CertificateException){
            throw RuntimeException("Failed to init Cipher", e)
        }catch (e: NoSuchAlgorithmException){
            throw RuntimeException("Failed to init Cipher", e)
        }catch (e: InvalidKeyException){
            throw RuntimeException("Failed to init Cipher", e)
        }catch (e: UnrecoverableKeyException){
            throw RuntimeException("Failed to init Cipher", e)
        }
    }
}

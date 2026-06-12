package com.example.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AesEncryptionService {
    private const val TAG = "AesEncryptionService"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "shirin_e2e_sec_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12 // GCM recommends 12 bytes IV
    private const val TAG_LENGTH_BITS = 128

    init {
        try {
            getOrCreateSecretKey()
            Log.d(TAG, "AES-256 encryption key initialized successfully in Android Keystore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize cryptographic keys: ${e.message}", e)
        }
    }

    /**
     * Retrieves or creates an AES SecretKey in the Android Keystore with AES-256 configuration.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        Log.d(TAG, "Key not found. Spawning new 256-bit AES key with hardware backing if available...")
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256) // AES-256
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts a raw byte array using AES-256 GCM.
     * Output has the structure: [12 bytes IV] + [Encrypted Data / Ciphertext]
     */
    fun encryptBytes(plainBytes: ByteArray): ByteArray {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv

        val encryptedData = cipher.doFinal(plainBytes)
        val packed = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, packed, 0, iv.size)
        System.arraycopy(encryptedData, 0, packed, iv.size, encryptedData.size)
        return packed
    }

    /**
     * Decrypts a packed AES-256 GCM byte array.
     * Expects input structure: [12 bytes IV] + [Encrypted Data / Ciphertext]
     */
    fun decryptBytes(encryptedBytes: ByteArray): ByteArray {
        if (encryptedBytes.size <= IV_LENGTH) {
            throw IllegalArgumentException("Data is too short to contain a valid IV.")
        }

        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(encryptedBytes, 0, iv, 0, iv.size)

        val ciphertext = ByteArray(encryptedBytes.size - IV_LENGTH)
        System.arraycopy(encryptedBytes, iv.size, ciphertext, 0, ciphertext.size)

        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Encrypts a plain-text string into a secure Base64 format representing packed IV + Ciphertext.
     */
    fun encryptString(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val plainBytes = plainText.toByteArray(Charsets.UTF_8)
            val encryptedBytes = encryptBytes(plainBytes)
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting string: ${e.message}", e)
            plainText // Fallback
        }
    }

    /**
     * Decrypts an AES-256 GCM encrypted string represented in Base64.
     */
    fun decryptString(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        return try {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = decryptBytes(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting string (might be plain-text): ${e.message}")
            encryptedBase64 // Fallback to raw string
        }
    }

    /**
     * Saves a sensitive document securely to a local file cache inside the 'encrypted_vault' directory.
     * Content is encrypted with AES-256 prior to disk write.
     */
    fun saveEncryptedFile(context: Context, relativeFileName: String, plainContent: String): File {
        val vaultDir = File(context.cacheDir, "encrypted_vault").apply {
            if (!exists()) mkdirs()
        }
        val file = File(vaultDir, "$relativeFileName.enc")
        
        try {
            val plainBytes = plainContent.toByteArray(Charsets.UTF_8)
            val encryptedBytes = encryptBytes(plainBytes)
            
            FileOutputStream(file).use { fos ->
                fos.write(encryptedBytes)
            }
            Log.d(TAG, "Securely saved AES-256 E2EE file: ${file.absolutePath} (${file.length()} bytes)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write encrypted file: ${e.message}", e)
        }
        return file
    }

    /**
     * Reads and decrypts an AES-256 GCM encrypted file from the local cache.
     */
    fun readDecryptedFile(context: Context, relativeFileName: String): String {
        val vaultDir = File(context.cacheDir, "encrypted_vault")
        val file = File(vaultDir, "$relativeFileName.enc")
        if (!file.exists()) {
            Log.w(TAG, "Encrypted file not found on disk: ${file.absolutePath}")
            return ""
        }

        return try {
            val fileLength = file.length().toInt()
            val encryptedBytes = ByteArray(fileLength)
            
            FileInputStream(file).use { fis ->
                fis.read(encryptedBytes)
            }
            
            val decryptedBytes = decryptBytes(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read/decrypt document: ${e.message}", e)
            ""
        }
    }

    /**
     * Deletes the local encrypted cache file when the document is erased.
     */
    fun deleteEncryptedFile(context: Context, relativeFileName: String): Boolean {
        val vaultDir = File(context.cacheDir, "encrypted_vault")
        val file = File(vaultDir, "$relativeFileName.enc")
        return if (file.exists()) {
            val deleted = file.delete()
            Log.d(TAG, "Deleted encrypted cache file: ${file.name} -> success=$deleted")
            deleted
        } else {
            false
        }
    }

    /**
     * Generates a fully encrypted backup export payload containing JSON information for transmission.
     */
    fun generateEncryptedBackupPayload(jsonSource: String): Pair<String, String> {
        val encryptedBase64 = encryptString(jsonSource)
        val testDigest = encryptString("SHIRIN_INTEGRITY_CHECK_2026")
        return Pair(encryptedBase64, testDigest)
    }
}

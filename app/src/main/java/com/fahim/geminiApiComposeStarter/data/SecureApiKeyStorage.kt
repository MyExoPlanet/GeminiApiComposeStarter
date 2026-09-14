package com.fahim.geminiApiComposeStarter.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val KEY_ALIAS = "gemini_api_key_aes_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"

private val Context.secureApiKeyDataStore by preferencesDataStore(
    name = "secure_api_key"
)

private val ENCRYPTED_API_KEY = stringPreferencesKey(
    "encrypted_gemini_api_key"
)

class SecureApiKeyStorage(
    private val context: Context,
) {

    suspend fun <T> withApiKey(
        initialApiKey: String,
        block: suspend (String) -> T,
    ): T? {

        val storedCiphertext =
            context.secureApiKeyDataStore.data.first()[ENCRYPTED_API_KEY]

        val ciphertext = if (storedCiphertext != null) {
            storedCiphertext
        } else {
            if (initialApiKey.isBlank()) {
                return null
            }

            val encrypted = encrypt(initialApiKey)

            context.secureApiKeyDataStore.edit { preferences ->
                preferences[ENCRYPTED_API_KEY] = encrypted
            }

            encrypted
        }

        val apiKey = decrypt(ciphertext)

        return block(apiKey)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey(),
        )

        val iv = cipher.iv

        val encryptedBytes = cipher.doFinal(
            plainText.toByteArray(Charsets.UTF_8)
        )

        val combined = iv + encryptedBytes

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP,
        )
    }

    private fun decrypt(ciphertext: String): String {
        val combined = Base64.decode(
            ciphertext,
            Base64.NO_WRAP,
        )

        val ivSize = 12

        val iv = combined.copyOfRange(
            0,
            ivSize,
        )

        val encryptedBytes = combined.copyOfRange(
            ivSize,
            combined.size,
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, iv),
        )

        return String(
            cipher.doFinal(encryptedBytes),
            Charsets.UTF_8,
        )
    }
}
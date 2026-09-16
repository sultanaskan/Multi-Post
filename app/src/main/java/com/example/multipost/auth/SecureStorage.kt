package com.example.multipost.auth

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val preferences =
        appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val secretKey: SecretKey
        get() = getOrCreateSecretKey()

    fun save(
        key: String,
        value: String
    ) {

        val encryptedValue =
            encrypt(value)

        preferences
            .edit()
            .putString(
                key,
                encryptedValue
            )
            .apply()
    }

    fun get(
        key: String
    ): String? {

        val encryptedValue =
            preferences.getString(
                key,
                null
            )
                ?: return null

        return try {
            decrypt(encryptedValue)
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun contains(
        key: String
    ): Boolean {

        return preferences.contains(key)
    }

    fun delete(
        key: String
    ) {

        preferences
            .edit()
            .remove(key)
            .apply()
    }

    fun clear() {

        preferences
            .edit()
            .clear()
            .apply()
    }

    private fun encrypt(
        value: String
    ): String {

        val cipher =
            Cipher.getInstance(
                TRANSFORMATION
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey
        )

        val iv =
            cipher.iv

        val encryptedBytes =
            cipher.doFinal(
                value.toByteArray(
                    StandardCharsets.UTF_8
                )
            )

        val combined =
            ByteArray(
                iv.size +
                        encryptedBytes.size
            )

        System.arraycopy(
            iv,
            0,
            combined,
            0,
            iv.size
        )

        System.arraycopy(
            encryptedBytes,
            0,
            combined,
            iv.size,
            encryptedBytes.size
        )

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    private fun decrypt(
        encryptedValue: String
    ): String {

        val combined =
            Base64.decode(
                encryptedValue,
                Base64.NO_WRAP
            )

        if (
            combined.size <=
            GCM_IV_LENGTH
        ) {
            throw IllegalArgumentException(
                "Invalid encrypted data."
            )
        }

        val iv =
            combined.copyOfRange(
                0,
                GCM_IV_LENGTH
            )

        val encryptedBytes =
            combined.copyOfRange(
                GCM_IV_LENGTH,
                combined.size
            )

        val cipher =
            Cipher.getInstance(
                TRANSFORMATION
            )

        val parameterSpec =
            GCMParameterSpec(
                GCM_TAG_LENGTH,
                iv
            )

        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            parameterSpec
        )

        val decryptedBytes =
            cipher.doFinal(
                encryptedBytes
            )

        return String(
            decryptedBytes,
            StandardCharsets.UTF_8
        )
    }

    private fun getOrCreateSecretKey(): SecretKey {

        val keyStore =
            java.security.KeyStore.getInstance(
                ANDROID_KEYSTORE
            )

        keyStore.load(null)

        val existingKey =
            keyStore.getKey(
                KEY_ALIAS,
                null
            )

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                "AES",
                ANDROID_KEYSTORE
            )

        val keyGenParameterSpec =
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .build()

        keyGenerator.init(
            keyGenParameterSpec
        )

        return keyGenerator.generateKey()
    }

    companion object {

        private const val PREFS_NAME =
            "multipost_secure_storage"

        private const val KEY_ALIAS =
            "multipost_secure_key"

        private const val ANDROID_KEYSTORE =
            "AndroidKeyStore"

        private const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        private const val GCM_IV_LENGTH =
            12

        private const val GCM_TAG_LENGTH =
            128
    }

}

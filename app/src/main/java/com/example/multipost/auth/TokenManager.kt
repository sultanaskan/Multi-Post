package com.example.multipost.auth

import android.content.Context

class TokenManager(
    context: Context
) {

    private val secureStorage =
        SecureStorage(context)

    fun saveAccessToken(
        accountId: String,
        accessToken: String
    ) {
        secureStorage.save(
            accessTokenKey(accountId),
            accessToken
        )
    }

    fun getAccessToken(
        accountId: String
    ): String? {
        return secureStorage.get(
            accessTokenKey(accountId)
        )
    }

    fun saveRefreshToken(
        accountId: String,
        refreshToken: String
    ) {
        secureStorage.save(
            refreshTokenKey(accountId),
            refreshToken
        )
    }

    fun getRefreshToken(
        accountId: String
    ): String? {
        return secureStorage.get(
            refreshTokenKey(accountId)
        )
    }

    fun saveTokenExpiry(
        accountId: String,
        expiresAt: Long
    ) {
        secureStorage.save(
            expiryKey(accountId),
            expiresAt.toString()
        )
    }

    fun getTokenExpiry(
        accountId: String
    ): Long {
        return secureStorage
            .get(expiryKey(accountId))
            ?.toLongOrNull()
            ?: 0L
    }

    fun isTokenExpired(
        accountId: String
    ): Boolean {

        val expiresAt =
            getTokenExpiry(accountId)

        if (expiresAt <= 0L) {
            return false
        }

        return System.currentTimeMillis() >=
                expiresAt
    }

    fun hasAccessToken(
        accountId: String
    ): Boolean {
        return secureStorage.contains(
            accessTokenKey(accountId)
        )
    }

    fun hasRefreshToken(
        accountId: String
    ): Boolean {
        return secureStorage.contains(
            refreshTokenKey(accountId)
        )
    }

    fun deleteTokens(
        accountId: String
    ) {

        secureStorage.delete(
            accessTokenKey(accountId)
        )

        secureStorage.delete(
            refreshTokenKey(accountId)
        )

        secureStorage.delete(
            expiryKey(accountId)
        )
    }

    private fun accessTokenKey(
        accountId: String
    ): String {
        return "access_token_$accountId"
    }

    private fun refreshTokenKey(
        accountId: String
    ): String {
        return "refresh_token_$accountId"
    }

    private fun expiryKey(
        accountId: String
    ): String {
        return "token_expiry_$accountId"
    }
}
package com.example.multipost.repository

import android.content.ContentValues
import android.content.Context
import com.example.multipost.auth.TokenManager
import com.example.multipost.database.DatabaseHelper
import com.example.multipost.model.SocialAccount

class AccountRepository(
    context: Context
) {

    private val database =
        DatabaseHelper.getInstance(context)

    private val tokenManager =
        TokenManager(context)

    fun insertAccount(
        account: SocialAccount
    ): Long {

        val now =
            System.currentTimeMillis()

        val values =
            ContentValues().apply {

                put(
                    "platform",
                    account.platform
                )

                put(
                    "account_id",
                    account.accountId
                )

                put(
                    "account_name",
                    account.accountName
                )

                // Tokens are intentionally NOT stored in SQLite.

                put(
                    "access_token",
                    ""
                )

                put(
                    "refresh_token",
                    ""
                )

                put(
                    "token_expires_at",
                    0L
                )

                put(
                    "created_at",
                    account.createdAt
                )

                put(
                    "updated_at",
                    now
                )
            }

        val accountId =
            database
                .writableDatabase
                .insert(
                    "social_accounts",
                    null,
                    values
                )

        if (accountId != -1L) {

            saveTokens(
                accountId = account.accountId,
                accessToken = account.accessToken,
                refreshToken = account.refreshToken,
                expiresAt = account.tokenExpiresAt
            )
        }

        return accountId
    }

    fun getAccountById(
        id: Long
    ): SocialAccount? {

        val cursor =
            database
                .readableDatabase
                .query(
                    "social_accounts",
                    null,
                    "id = ?",
                    arrayOf(id.toString()),
                    null,
                    null,
                    null
                )

        cursor.use {

            if (!it.moveToFirst()) {
                return null
            }

            return cursorToAccount(it)
        }
    }

    fun getAccountByAccountId(
        accountId: String
    ): SocialAccount? {

        val cursor =
            database
                .readableDatabase
                .query(
                    "social_accounts",
                    null,
                    "account_id = ?",
                    arrayOf(accountId),
                    null,
                    null,
                    null
                )

        cursor.use {

            if (!it.moveToFirst()) {
                return null
            }

            return cursorToAccount(it)
        }
    }

    fun getAllAccounts(): List<SocialAccount> {

        val accounts =
            mutableListOf<SocialAccount>()

        val cursor =
            database
                .readableDatabase
                .query(
                    "social_accounts",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "created_at DESC"
                )

        cursor.use {

            while (it.moveToNext()) {
                accounts.add(
                    cursorToAccount(it)
                )
            }
        }

        return accounts
    }

    fun updateAccount(
        account: SocialAccount
    ): Int {

        val values =
            ContentValues().apply {

                put(
                    "platform",
                    account.platform
                )

                put(
                    "account_id",
                    account.accountId
                )

                put(
                    "account_name",
                    account.accountName
                )

                put(
                    "updated_at",
                    System.currentTimeMillis()
                )
            }

        val updatedRows =
            database
                .writableDatabase
                .update(
                    "social_accounts",
                    values,
                    "id = ?",
                    arrayOf(account.id.toString())
                )

        if (updatedRows > 0) {

            saveTokens(
                accountId = account.accountId,
                accessToken = account.accessToken,
                refreshToken = account.refreshToken,
                expiresAt = account.tokenExpiresAt
            )
        }

        return updatedRows
    }

    fun deleteAccount(
        id: Long
    ): Int {

        val account =
            getAccountById(id)

        if (account != null) {

            tokenManager.deleteTokens(
                account.accountId
            )
        }

        return database
            .writableDatabase
            .delete(
                "social_accounts",
                "id = ?",
                arrayOf(id.toString())
            )
    }

    fun saveAccountTokens(
        accountId: String,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: Long
    ) {

        saveTokens(
            accountId = accountId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )

        updateTokenExpiryInDatabase(
            accountId,
            expiresAt
        )
    }

    fun getAccessToken(
        accountId: String
    ): String? {

        return tokenManager.getAccessToken(
            accountId
        )
    }

    fun getRefreshToken(
        accountId: String
    ): String? {

        return tokenManager.getRefreshToken(
            accountId
        )
    }

    fun getTokenExpiry(
        accountId: String
    ): Long {

        return tokenManager.getTokenExpiry(
            accountId
        )
    }

    fun isTokenExpired(
        accountId: String
    ): Boolean {

        return tokenManager.isTokenExpired(
            accountId
        )
    }

    fun deleteAccountTokens(
        accountId: String
    ) {

        tokenManager.deleteTokens(
            accountId
        )
    }

    private fun saveTokens(
        accountId: String,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: Long
    ) {

        if (!accessToken.isNullOrEmpty()) {

            tokenManager.saveAccessToken(
                accountId,
                accessToken
            )
        }

        if (!refreshToken.isNullOrEmpty()) {

            tokenManager.saveRefreshToken(
                accountId,
                refreshToken
            )
        }

        if (expiresAt > 0L) {

            tokenManager.saveTokenExpiry(
                accountId,
                expiresAt
            )
        }
    }

    private fun updateTokenExpiryInDatabase(
        accountId: String,
        expiresAt: Long
    ) {

        val values =
            ContentValues().apply {
                put(
                    "token_expires_at",
                    expiresAt
                )

                put(
                    "updated_at",
                    System.currentTimeMillis()
                )
            }

        database
            .writableDatabase
            .update(
                "social_accounts",
                values,
                "account_id = ?",
                arrayOf(accountId)
            )
    }

    private fun cursorToAccount(
        cursor: android.database.Cursor
    ): SocialAccount {

        val id =
            cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    "id"
                )
            )

        val platform =
            cursor.getString(
                cursor.getColumnIndexOrThrow(
                    "platform"
                )
            )

        val accountId =
            cursor.getString(
                cursor.getColumnIndexOrThrow(
                    "account_id"
                )
            )

        val accountName =
            cursor.getString(
                cursor.getColumnIndexOrThrow(
                    "account_name"
                )
            )

        val createdAt =
            cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    "created_at"
                )
            )

        val updatedAt =
            cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    "updated_at"
                )
            )

        val accessToken =
            tokenManager.getAccessToken(
                accountId
            )

        val refreshToken =
            tokenManager.getRefreshToken(
                accountId
            )

        val tokenExpiresAt =
            tokenManager.getTokenExpiry(
                accountId
            )

        return SocialAccount(
            id = id,
            platform = platform,
            accountId = accountId,
            accountName = accountName,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = tokenExpiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun close() {
        database.close()
    }
}
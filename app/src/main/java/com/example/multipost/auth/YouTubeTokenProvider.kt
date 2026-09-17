package com.example.multipost.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class YouTubeTokenProvider(
    context: Context
) {

    companion object {

        const val YOUTUBE_ACCOUNT_ID =
            "youtube_default"

        private const val YOUTUBE_UPLOAD_SCOPE =
            "https://www.googleapis.com/auth/youtube.upload"

        private const val YOUTUBE_READONLY_SCOPE =
            "https://www.googleapis.com/auth/youtube.readonly"
    }

    private val appContext =
        context.applicationContext

    private val tokenManager =
        TokenManager(appContext)

    fun getStoredAccessToken(): String? {

        return tokenManager.getAccessToken(
            YOUTUBE_ACCOUNT_ID
        )
    }

    fun saveAccessToken(
        accessToken: String
    ) {

        tokenManager.saveAccessToken(
            accountId = YOUTUBE_ACCOUNT_ID,
            accessToken = accessToken
        )
    }

    fun createAuthorizationRequest():
            AuthorizationRequest {

        val scopes =
            listOf(
                Scope(
                    YOUTUBE_UPLOAD_SCOPE
                ),
                Scope(
                    YOUTUBE_READONLY_SCOPE
                )
            )

        return AuthorizationRequest
            .builder()
            .setRequestedScopes(
                scopes
            )
            .build()
    }

    /**
     * Attempts to obtain a fresh YouTube access token
     * without opening a user-facing authorization screen.
     *
     * Returns:
     *
     * Success(token)
     * ResolutionRequired
     * Failed(message)
     */
    suspend fun authorizeSilently():
            SilentAuthorizationResult {

        return suspendCancellableCoroutine { continuation ->

            Identity
                .getAuthorizationClient(
                    appContext
                )
                .authorize(
                    createAuthorizationRequest()
                )
                .addOnSuccessListener { result ->

                    if (
                        result.hasResolution()
                    ) {

                        if (
                            continuation.isActive
                        ) {

                            continuation.resume(
                                SilentAuthorizationResult
                                    .ResolutionRequired
                            )
                        }

                        return@addOnSuccessListener
                    }

                    val accessToken =
                        result.accessToken

                    if (
                        accessToken.isNullOrBlank()
                    ) {

                        if (
                            continuation.isActive
                        ) {

                            continuation.resume(
                                SilentAuthorizationResult
                                    .Failed(
                                        "Google authorization returned no access token."
                                    )
                            )
                        }

                        return@addOnSuccessListener
                    }

                    saveAccessToken(
                        accessToken
                    )

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            SilentAuthorizationResult
                                .Success(
                                    accessToken
                                )
                        )
                    }
                }
                .addOnFailureListener { exception ->

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            SilentAuthorizationResult
                                .Failed(
                                    exception.message
                                        ?: "Silent YouTube authorization failed."
                                )
                        )
                    }
                }
        }
    }

    fun handleAuthorizationResult(
        result: AuthorizationResult,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val accessToken =
            result.accessToken

        if (
            accessToken.isNullOrBlank()
        ) {

            onFailure(
                IllegalStateException(
                    "Authorization completed without an access token."
                )
            )

            return
        }

        saveAccessToken(
            accessToken
        )

        onSuccess(
            accessToken
        )
    }

    sealed class SilentAuthorizationResult {

        data class Success(
            val accessToken: String
        ) : SilentAuthorizationResult()

        data object ResolutionRequired :
            SilentAuthorizationResult()

        data class Failed(
            val message: String
        ) : SilentAuthorizationResult()
    }
}
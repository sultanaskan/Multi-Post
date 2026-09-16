package com.example.multipost.auth

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.security.SecureRandom
import java.util.Base64

class OAuthManager {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createState(): String {

        val bytes =
            ByteArray(32)

        SecureRandom().nextBytes(bytes)

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    fun buildAuthorizationUrl(
        authorizationEndpoint: String,
        clientId: String,
        redirectUri: String,
        scope: String,
        state: String
    ): String {

        return Uri.parse(
            authorizationEndpoint
        )
            .buildUpon()
            .appendQueryParameter(
                "response_type",
                "code"
            )
            .appendQueryParameter(
                "client_id",
                clientId
            )
            .appendQueryParameter(
                "redirect_uri",
                redirectUri
            )
            .appendQueryParameter(
                "scope",
                scope
            )
            .appendQueryParameter(
                "state",
                state
            )
            .build()
            .toString()
    }

    fun parseCallback(
        callbackUri: Uri,
        expectedState: String
    ): OAuthResult {

        val error =
            callbackUri.getQueryParameter(
                "error"
            )

        if (!error.isNullOrEmpty()) {

            val errorDescription =
                callbackUri.getQueryParameter(
                    "error_description"
                )

            return OAuthResult.Error(
                message =
                    errorDescription
                        ?: error
            )
        }

        val returnedState =
            callbackUri.getQueryParameter(
                "state"
            )

        if (
            returnedState.isNullOrEmpty() ||
            returnedState != expectedState
        ) {

            return OAuthResult.Error(
                message = "Invalid OAuth state."
            )
        }

        val code =
            callbackUri.getQueryParameter(
                "code"
            )

        if (code.isNullOrEmpty()) {

            return OAuthResult.Error(
                message =
                    "Authorization code not found."
            )
        }

        return OAuthResult.Success(
            authorizationCode = code
        )
    }

    sealed class OAuthResult {

        data class Success(
            val authorizationCode: String
        ) : OAuthResult()

        data class Error(
            val message: String
        ) : OAuthResult()
    }
}
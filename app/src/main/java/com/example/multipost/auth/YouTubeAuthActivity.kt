package com.example.multipost.auth

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.multipost.repository.AccountRepository
import com.example.multipost.upload.UploadManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

class YouTubeAuthActivity : AppCompatActivity() {

    companion object {

        private const val YOUTUBE_UPLOAD_SCOPE =
            "https://www.googleapis.com/auth/youtube.upload"

        private const val YOUTUBE_READONLY_SCOPE =
            "https://www.googleapis.com/auth/youtube.readonly"

        private const val YOUTUBE_ACCOUNT_ID =
            "youtube_default"

        private const val EXTRA_AUTH_REQUIRED =
            "auth_required"

        private const val EXTRA_JOB_ID =
            "job_id"
    }

    private lateinit var statusText: TextView
    private lateinit var authorizeButton: Button

    private lateinit var accountRepository: AccountRepository
    private lateinit var uploadManager: UploadManager

    private var pendingJobId: Long = 0L

    private val authorizationLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (
                result.resultCode != RESULT_OK
            ) {

                showError(
                    "YouTube authorization was cancelled."
                )

                return@registerForActivityResult
            }

            try {

                val authorizationResult =
                    Identity
                        .getAuthorizationClient(this)
                        .getAuthorizationResultFromIntent(
                            result.data
                        )

                handleAuthorizationResult(
                    authorizationResult
                )

            } catch (exception: Exception) {

                exception.printStackTrace()

                showError(
                    exception.message
                        ?: "Failed to process YouTube authorization."
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        accountRepository =
            AccountRepository(this)

        uploadManager =
            UploadManager(this)

        pendingJobId =
            intent.getLongExtra(
                EXTRA_JOB_ID,
                0L
            )

        createUi()
    }

    private fun createUi() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    48,
                    64,
                    48,
                    48
                )
            }

        val title =
            TextView(this).apply {

                text =
                    "Connect YouTube"

                textSize =
                    24f
            }

        statusText =
            TextView(this).apply {

                text =
                    if (
                        pendingJobId > 0L
                    ) {
                        "Your YouTube authorization is required to continue the upload."
                    } else {
                        "Connect your Google account to publish videos to YouTube."
                    }

                textSize =
                    16f

                setPadding(
                    0,
                    32,
                    0,
                    32
                )
            }

        authorizeButton =
            Button(this).apply {

                text =
                    "Connect YouTube"

                setOnClickListener {

                    startAuthorization()
                }
            }

        root.addView(title)
        root.addView(statusText)
        root.addView(authorizeButton)

        setContentView(root)
    }

    private fun startAuthorization() {

        authorizeButton.isEnabled =
            false

        statusText.text =
            "Requesting YouTube authorization..."

        val requestedScopes =
            listOf(
                Scope(
                    YOUTUBE_UPLOAD_SCOPE
                ),
                Scope(
                    YOUTUBE_READONLY_SCOPE
                )
            )

        val authorizationRequest =
            AuthorizationRequest
                .builder()
                .setRequestedScopes(
                    requestedScopes
                )
                .build()

        Identity
            .getAuthorizationClient(this)
            .authorize(
                authorizationRequest
            )
            .addOnSuccessListener { result ->

                handleAuthorizationStartResult(
                    result
                )
            }
            .addOnFailureListener { exception ->

                exception.printStackTrace()

                showError(
                    exception.message
                        ?: "Unable to start YouTube authorization."
                )
            }
    }

    private fun handleAuthorizationStartResult(
        result: AuthorizationResult
    ) {

        if (
            result.hasResolution()
        ) {

            val pendingIntent =
                result.pendingIntent

            if (
                pendingIntent == null
            ) {

                showError(
                    "Google authorization requires user approval, but no authorization request was provided."
                )

                return
            }

            statusText.text =
                "Please authorize YouTube access..."

            val intentSenderRequest =
                IntentSenderRequest.Builder(
                    pendingIntent.intentSender
                ).build()

            authorizationLauncher.launch(
                intentSenderRequest
            )

            return
        }

        handleAuthorizationResult(
            result
        )
    }

    private fun handleAuthorizationResult(
        result: AuthorizationResult
    ) {

        val accessToken =
            result.accessToken

        if (
            accessToken.isNullOrBlank()
        ) {

            showError(
                "Authorization completed, but no access token was returned."
            )

            return
        }

        saveYouTubeAccount(
            accessToken
        )
    }

    private fun saveYouTubeAccount(
        accessToken: String
    ) {

        try {

            val existingAccount =
                accountRepository
                    .getAccountByAccountId(
                        YOUTUBE_ACCOUNT_ID
                    )

            if (
                existingAccount == null
            ) {

                val account =
                    com.example.multipost.model.SocialAccount(
                        id = 0L,
                        platform = "youtube",
                        accountId =
                            YOUTUBE_ACCOUNT_ID,
                        accountName =
                            "YouTube",
                        accessToken =
                            accessToken,
                        refreshToken =
                            null,
                        tokenExpiresAt =
                            0L,
                        createdAt =
                            System.currentTimeMillis(),
                        updatedAt =
                            System.currentTimeMillis()
                    )

                val insertedId =
                    accountRepository
                        .insertAccount(
                            account
                        )

                if (
                    insertedId == -1L
                ) {

                    showError(
                        "YouTube account could not be saved."
                    )

                    return
                }

            } else {

                accountRepository.updateAccount(
                    existingAccount.copy(
                        accessToken =
                            accessToken,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                )

                accountRepository.saveAccountTokens(
                    accountId =
                        existingAccount.accountId,
                    accessToken =
                        accessToken,
                    refreshToken =
                        existingAccount.refreshToken,
                    expiresAt =
                        0L
                )
            }

            handleAuthenticationSuccess()

        } catch (exception: Exception) {

            exception.printStackTrace()

            showError(
                exception.message
                    ?: "YouTube account could not be saved."
            )
        }
    }

    private fun handleAuthenticationSuccess() {

        if (
            pendingJobId > 0L
        ) {

            statusText.text =
                "YouTube connected. Retrying your upload..."

            authorizeButton.isEnabled =
                false

            retryPendingUpload()

        } else {

            showSuccess()
        }
    }

    private fun retryPendingUpload() {

        try {

            val account =
                accountRepository
                    .getAccountByAccountId(
                        YOUTUBE_ACCOUNT_ID
                    )

            if (
                account == null
            ) {

                showError(
                    "YouTube account could not be found after authorization."
                )

                return
            }

            uploadManager.retryUpload(
                jobId =
                    pendingJobId,
                platform =
                    "youtube"
            )

            Toast.makeText(
                this,
                "YouTube authorization successful. Upload restarted.",
                Toast.LENGTH_LONG
            ).show()

            finish()

        } catch (exception: Exception) {

            exception.printStackTrace()

            showError(
                exception.message
                    ?: "Failed to retry the upload."
            )
        }
    }

    private fun showSuccess() {

        authorizeButton.isEnabled =
            true

        statusText.text =
            """
            YouTube authorization successful.

            Account:
            YouTube

            Access token:
            Saved securely.
            """.trimIndent()

        Toast.makeText(
            this,
            "YouTube connected successfully.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showError(
        message: String
    ) {

        authorizeButton.isEnabled =
            true

        statusText.text =
            message

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {

        accountRepository.close()

        super.onDestroy()
    }
}
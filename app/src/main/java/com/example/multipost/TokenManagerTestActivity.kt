package com.example.multipost

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.multipost.auth.TokenManager

class TokenManagerTestActivity : AppCompatActivity() {

    companion object {
        private const val YOUTUBE_ACCOUNT_ID =
            "youtube_default"
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var statusText: TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        tokenManager =
            TokenManager(this)

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
                    "YouTube Token Test"

                textSize =
                    24f
            }

        statusText =
            TextView(this).apply {

                text =
                    getTokenStatus()

                textSize =
                    16f

                setPadding(
                    0,
                    32,
                    0,
                    32
                )
            }

        val invalidateButton =
            Button(this).apply {

                text =
                    "Use Invalid YouTube Token"

                setOnClickListener {

                    invalidateToken()
                }
            }

        root.addView(title)
        root.addView(statusText)
        root.addView(invalidateButton)

        setContentView(root)
    }

    private fun invalidateToken() {

        tokenManager.saveAccessToken(
            accountId =
                YOUTUBE_ACCOUNT_ID,
            accessToken =
                "INVALID_YOUTUBE_TEST_TOKEN"
        )

        statusText.text =
            """
            YouTube access token replaced.

            The next YouTube upload should return HTTP 401.

            Restore authorization from
            YouTubeAuthActivity after testing.
            """.trimIndent()

        Toast.makeText(
            this,
            "Invalid test token saved.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getTokenStatus(): String {

        val token =
            tokenManager.getAccessToken(
                YOUTUBE_ACCOUNT_ID
            )

        return if (
            token.isNullOrBlank()
        ) {
            "No YouTube access token found."
        } else {
            "YouTube access token is currently available."
        }
    }
}
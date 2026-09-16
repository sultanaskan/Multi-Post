package com.example.multipost.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OAuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val resultText =
            TextView(this).apply {
                textSize = 18f
                setPadding(
                    40,
                    60,
                    40,
                    40
                )
            }

        setContentView(resultText)

        handleCallback(
            intent,
            resultText
        )
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        val resultText =
            TextView(this).apply {
                textSize = 18f
                setPadding(
                    40,
                    60,
                    40,
                    40
                )
            }

        setContentView(resultText)

        handleCallback(
            intent,
            resultText
        )
    }

    @SuppressLint("SetTextI18n")
    private fun handleCallback(
        intent: Intent?,
        resultText: TextView
    ) {

        val callbackUri =
            intent?.data

        if (callbackUri == null) {

            resultText.text =
                """
                OAuth Callback

                RESULT:
                ERROR

                Callback URI not found.
                """.trimIndent()

            return
        }

        resultText.text =
            """
            OAuth Callback

            URI RECEIVED: PASS

            Scheme:
            ${callbackUri.scheme}

            Host:
            ${callbackUri.host}

            Path:
            ${callbackUri.path}

            Code:
            ${callbackUri.getQueryParameter("code")}

            State:
            ${callbackUri.getQueryParameter("state")}

            RESULT:
            SUCCESS
            """.trimIndent()
    }
}
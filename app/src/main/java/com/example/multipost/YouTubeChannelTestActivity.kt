package com.example.multipost

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.multipost.network.YouTubeApiService
import com.example.multipost.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class YouTubeChannelTestActivity :
    AppCompatActivity() {

    private lateinit var accountRepository:
            AccountRepository

    private lateinit var statusText:
            TextView

    private lateinit var testButton:
            Button

    private val activityScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        accountRepository =
            AccountRepository(this)

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
                    "YouTube API Test"

                textSize =
                    24f
            }

        statusText =
            TextView(this).apply {

                text =
                    "Ready to test YouTube API connection."

                textSize =
                    16f

                setPadding(
                    0,
                    32,
                    0,
                    32
                )
            }

        testButton =
            Button(this).apply {

                text =
                    "Test YouTube API"

                setOnClickListener {

                    testYouTubeApi()
                }
            }

        root.addView(title)
        root.addView(statusText)
        root.addView(testButton)

        setContentView(root)
    }

    private fun testYouTubeApi() {

        testButton.isEnabled =
            false

        statusText.text =
            "Connecting to YouTube API..."

        activityScope.launch {

            try {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {
                        fetchChannel()
                    }

                statusText.text =
                    result

            } catch (exception: Exception) {

                exception.printStackTrace()

                statusText.text =
                    """
                    YouTube API request failed.

                    ${exception.message}
                    """.trimIndent()
            }

            testButton.isEnabled =
                true
        }
    }

    private fun fetchChannel(): String {

        val account =
            accountRepository
                .getAccountByAccountId(
                    "youtube_default"
                )
                ?: return (
                        "YouTube account is not connected."
                        )

        val accessToken =
            accountRepository
                .getAccessToken(
                    account.accountId
                )

        if (accessToken.isNullOrBlank()) {

            return (
                    "YouTube access token was not found."
                    )
        }

        val apiService =
            YouTubeApiService(this)

        val response =
            apiService.getMyChannel(
                accessToken
            )

        return parseChannelResponse(
            response
        )
    }

    private fun parseChannelResponse(
        response: String
    ): String {

        val root =
            JSONObject(response)

        val items =
            root.optJSONArray("items")

        if (
            items == null ||
            items.length() == 0
        ) {

            return """
                YouTube API request succeeded.

                But no YouTube channel was found.
            """.trimIndent()
        }

        val channel =
            items.getJSONObject(0)

        val channelId =
            channel.optString(
                "id",
                "Unknown"
            )

        val snippet =
            channel.optJSONObject(
                "snippet"
            )

        val statistics =
            channel.optJSONObject(
                "statistics"
            )

        val channelTitle =
            snippet?.optString(
                "title",
                "Unknown"
            ) ?: "Unknown"

        val subscribers =
            statistics?.optString(
                "subscriberCount",
                "Unknown"
            ) ?: "Unknown"

        val videos =
            statistics?.optString(
                "videoCount",
                "Unknown"
            ) ?: "Unknown"

        return """
            YouTube API: SUCCESS

            Channel:
            $channelTitle

            Channel ID:
            $channelId

            Subscribers:
            $subscribers

            Videos:
            $videos
        """.trimIndent()
    }

    override fun onDestroy() {

        activityScope.cancel()

        accountRepository.close()

        super.onDestroy()
    }
}
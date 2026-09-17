package com.example.multipost.network

import android.content.Context
import com.example.multipost.model.YouTubeChannelInfo
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class YouTubeApiService(
    private val context: Context
) {

    companion object {

        private const val CHANNEL_URL =
            "https://www.googleapis.com/youtube/v3/channels"
    }

    fun getMyChannel(
        accessToken: String
    ): String {

        require(
            accessToken.isNotBlank()
        ) {
            "YouTube access token is empty."
        }

        val url = URL(
            "$CHANNEL_URL?part=snippet,statistics&mine=true"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "GET"

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode =
                connection.responseCode

            val stream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response =
                stream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""

            if (responseCode !in 200..299) {

                throw Exception(
                    "YouTube API HTTP $responseCode\n\n$response"
                )
            }

            return response

        } finally {

            connection.disconnect()
        }
    }

    fun getMyChannelInfo(
        accessToken: String
    ): YouTubeChannelInfo {

        val response =
            getMyChannel(
                accessToken
            )

        val root =
            JSONObject(response)

        val items =
            root.optJSONArray("items")

        if (
            items == null ||
            items.length() == 0
        ) {
            throw Exception(
                "No YouTube channel was found."
            )
        }

        val channel =
            items.getJSONObject(0)

        val channelId =
            channel.optString(
                "id",
                ""
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

        val subscriberCount =
            statistics?.optString(
                "subscriberCount",
                "0"
            ) ?: "0"

        val videoCount =
            statistics?.optString(
                "videoCount",
                "0"
            ) ?: "0"

        if (channelId.isBlank()) {

            throw Exception(
                "YouTube channel ID was not returned."
            )
        }

        return YouTubeChannelInfo(
            channelId = channelId,
            channelTitle = channelTitle,
            subscriberCount = subscriberCount,
            videoCount = videoCount
        )
    }
}
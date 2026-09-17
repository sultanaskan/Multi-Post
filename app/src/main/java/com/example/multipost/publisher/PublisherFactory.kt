package com.example.multipost.publisher

import android.content.Context

object PublisherFactory {

    fun create(
        context: Context,
        platform: String
    ): SocialPublisher {

        return when (
            platform.lowercase()
        ) {

            "youtube" ->
                YouTubePublisher(
                    context
                )

            "tiktok" ->
                SimulatedPublisher()

            "instagram" ->
                SimulatedPublisher()

            "facebook" ->
                SimulatedPublisher()

            "x" ->
                SimulatedPublisher()

            "linkedin" ->
                SimulatedPublisher()

            else ->
                SimulatedPublisher()
        }
    }
}
package com.example.multipost.publisher

object PublisherFactory {

    fun create(
        platform: String
    ): SocialPublisher {

        return when (
            platform.lowercase()
        ) {

            "youtube" ->
                SimulatedPublisher()

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
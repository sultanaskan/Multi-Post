package com.example.multipost.utils

import com.example.multipost.model.YouTubeVideoMetadata

object YouTubeMetadataBuilder {

    fun build(
        title: String,
        description: String,
        hashtags: String,
        privacyStatus: YouTubeVideoMetadata.PrivacyStatus,
        categoryId: String = "22"
    ): YouTubeVideoMetadata {

        val cleanTitle =
            title.trim()

        val cleanDescription =
            description.trim()

        val tags =
            parseHashtags(
                hashtags
            )

        val metadata =
            YouTubeVideoMetadata(
                title = cleanTitle,
                description = cleanDescription,
                tags = tags,
                privacyStatus = privacyStatus,
                categoryId = categoryId
            )

        metadata.validate()

        return metadata
    }

    private fun parseHashtags(
        hashtags: String
    ): List<String> {

        if (hashtags.isBlank()) {
            return emptyList()
        }

        return hashtags
            .split(
                Regex("[,\\s]+")
            )
            .map {
                it.trim()
                    .removePrefix("#")
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
    }
}
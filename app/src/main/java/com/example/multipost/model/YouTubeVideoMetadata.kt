package com.example.multipost.model

data class YouTubeVideoMetadata(

    val title: String,

    val description: String,

    val tags: List<String>,

    val privacyStatus: PrivacyStatus,

    val categoryId: String = "22"
) {

    enum class PrivacyStatus {
        PUBLIC,
        UNLISTED,
        PRIVATE
    }

    fun validate() {

        require(
            title.isNotBlank()
        ) {
            "YouTube video title cannot be empty."
        }

        require(
            title.length <= 100
        ) {
            "YouTube video title cannot exceed 100 characters."
        }

        require(
            description.length <= 5000
        ) {
            "YouTube video description cannot exceed 5000 characters."
        }

        require(
            tags.size <= 500
        ) {
            "Too many YouTube tags."
        }

        require(
            categoryId.isNotBlank()
        ) {
            "YouTube category ID cannot be empty."
        }
    }
}
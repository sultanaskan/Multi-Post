package com.example.multipost.model

data class PublishJob(

    val id: Long = 0,

    val videoId: Long? = null,

    val platform: String,

    val caption: String = "",

    val youtubeTitle: String = "",

    val youtubePrivacy: String = "Private",

    val hashtags: String = "",

    val status: String = "pending",

    val progress: Int = 0,

    val remotePostId: String? = null,

    val errorMessage: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
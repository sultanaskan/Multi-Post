package com.example.multipost.model

data class DraftModel(
    val id: Long = 0,
    val videoId: Long? = null,
    val caption: String = "",
    val hashtags: String = "",
    val platforms: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
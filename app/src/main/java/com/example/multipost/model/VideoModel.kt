package com.example.multipost.model

data class VideoModel(
    val id: Long = 0,
    val uri: String,
    val fileName: String,
    val duration: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val fileSize: Long = 0,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
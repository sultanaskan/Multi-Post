package com.example.multipost.upload

data class UploadProgress(
    val jobId: Long,
    val platform: String,
    val status: String,
    val progress: Int,
    val errorMessage: String? = null
)
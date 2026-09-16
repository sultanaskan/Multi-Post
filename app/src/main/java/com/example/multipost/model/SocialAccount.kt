package com.example.multipost.model

data class SocialAccount(
    val id: Long = 0,
    val platform: String,
    val accountId: String,
    val accountName: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenExpiresAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
package com.example.multipost.publisher

import com.example.multipost.model.PublishJob

interface SocialPublisher {

    suspend fun publish(
        job: PublishJob,
        progressCallback: suspend (Int) -> Unit
    ): PublishResult
}

sealed class PublishResult {

    data class Success(
        val remotePostId: String? = null
    ) : PublishResult()

    data class Failure(
        val message: String
    ) : PublishResult()

    data class AuthenticationRequired(
        val message: String
    ) : PublishResult()
}
package com.example.multipost.publisher

import com.example.multipost.model.PublishJob
import kotlinx.coroutines.delay

class SimulatedPublisher : SocialPublisher {

    override suspend fun publish(
        job: PublishJob,
        progressCallback: suspend (Int) -> Unit
    ): PublishResult {

        for (progress in 0..100 step 10) {

            delay(300)

            progressCallback(
                progress
            )
        }

        return PublishResult.Success(
            remotePostId =
                "simulated_${job.id}"
        )
    }
}
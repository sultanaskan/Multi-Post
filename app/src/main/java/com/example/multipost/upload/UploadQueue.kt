package com.example.multipost.upload

import android.content.Context
import com.example.multipost.model.PublishJob
import com.example.multipost.repository.PublishRepository

class UploadQueue(
    context: Context
) {

    private val publishRepository =
        PublishRepository(context)

    fun getPendingJobs(): List<PublishJob> {

        return publishRepository
            .getAllJobs()
            .filter {
                it.status == "pending" ||
                        it.status == "failed"
            }
    }

    fun getActiveJobs(): List<PublishJob> {

        return publishRepository
            .getAllJobs()
            .filter {
                it.status == "uploading"
            }
    }

    fun getPendingCount(): Int {

        return getPendingJobs().size
    }

    fun getActiveCount(): Int {

        return getActiveJobs().size
    }
}
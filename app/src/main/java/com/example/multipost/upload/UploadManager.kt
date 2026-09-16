package com.example.multipost.upload

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class UploadManager(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val workManager =
        WorkManager.getInstance(
            appContext
        )

    private val uploadQueue =
        UploadQueue(
            appContext
        )

    fun enqueueUpload(
        jobId: Long,
        platform: String
    ) {

        if (jobId <= 0L) {
            return
        }

        if (platform.isBlank()) {
            return
        }

        val inputData =
            workDataOf(
                UploadWorker.INPUT_JOB_ID to jobId,
                UploadWorker.INPUT_PLATFORM to platform
            )

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        workManager.enqueueUniqueWork(
            getWorkName(jobId),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelUpload(
        jobId: Long
    ) {

        workManager.cancelUniqueWork(
            getWorkName(jobId)
        )
    }

    fun retryUpload(
        jobId: Long,
        platform: String
    ) {

        cancelUpload(jobId)

        enqueueUpload(
            jobId = jobId,
            platform = platform
        )
    }

    fun enqueuePendingJobs() {

        val pendingJobs =
            uploadQueue.getPendingJobs()

        pendingJobs.forEach { job ->

            enqueueUpload(
                jobId = job.id,
                platform = job.platform
            )
        }
    }

    fun getPendingCount(): Int {

        return uploadQueue.getPendingCount()
    }

    fun getActiveCount(): Int {

        return uploadQueue.getActiveCount()
    }

    private fun getWorkName(
        jobId: Long
    ): String {

        return "upload_job_$jobId"
    }
}
package com.example.multipost.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.multipost.publisher.PublisherFactory
import com.example.multipost.publisher.PublishResult
import com.example.multipost.repository.PublishRepository

class UploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    companion object {

        const val INPUT_JOB_ID =
            "job_id"

        const val INPUT_PLATFORM =
            "platform"

        const val OUTPUT_JOB_ID =
            "job_id"

        const val OUTPUT_PLATFORM =
            "platform"

        const val OUTPUT_PROGRESS =
            "progress"

        const val OUTPUT_STATUS =
            "status"
    }

    private val publishRepository =
        PublishRepository(
            appContext
        )

    private val notificationHelper =
        UploadNotificationHelper(
            appContext
        )

    override suspend fun doWork(): Result {

        val jobId =
            inputData.getLong(
                INPUT_JOB_ID,
                -1L
            )

        val platform =
            inputData.getString(
                INPUT_PLATFORM
            ).orEmpty()

        if (
            jobId <= 0L ||
            platform.isBlank()
        ) {

            return Result.failure(
                workDataOf(
                    OUTPUT_STATUS to
                            "invalid_input"
                )
            )
        }

        val job =
            publishRepository.getJobById(
                jobId
            )

        if (job == null) {

            return Result.failure(
                workDataOf(
                    OUTPUT_JOB_ID to jobId,
                    OUTPUT_PLATFORM to platform,
                    OUTPUT_STATUS to
                            "job_not_found"
                )
            )
        }

        return try {

            setForeground(
                createForegroundInfo(
                    jobId = jobId,
                    platform = platform,
                    progress = 0
                )
            )

            publishRepository.updateJobStatus(
                id = jobId,
                status = "uploading",
                progress = 0
            )

            setProgress(
                workDataOf(
                    OUTPUT_JOB_ID to jobId,
                    OUTPUT_PLATFORM to platform,
                    OUTPUT_PROGRESS to 0,
                    OUTPUT_STATUS to
                            "uploading"
                )
            )

            val publisher =
                PublisherFactory.create(
                    platform
                )

            val result =
                publisher.publish(
                    job = job
                ) { progress ->

                    if (isStopped) {
                        return@publish
                    }

                    publishRepository.updateJobStatus(
                        id = jobId,
                        status = "uploading",
                        progress = progress
                    )

                    setForeground(
                        createForegroundInfo(
                            jobId = jobId,
                            platform = platform,
                            progress = progress
                        )
                    )

                    setProgress(
                        workDataOf(
                            OUTPUT_JOB_ID to
                                    jobId,
                            OUTPUT_PLATFORM to
                                    platform,
                            OUTPUT_PROGRESS to
                                    progress,
                            OUTPUT_STATUS to
                                    "uploading"
                        )
                    )
                }

            if (isStopped) {

                publishRepository.updateJobStatus(
                    id = jobId,
                    status = "cancelled",
                    progress = job.progress
                )

                return Result.failure(
                    workDataOf(
                        OUTPUT_JOB_ID to jobId,
                        OUTPUT_PLATFORM to platform,
                        OUTPUT_PROGRESS to job.progress,
                        OUTPUT_STATUS to
                                "cancelled"
                    )
                )
            }

            when (result) {

                is PublishResult.Success -> {

                    publishRepository.updateJobStatus(
                        id = jobId,
                        status = "completed",
                        progress = 100
                    )

                    showCompletedNotification(
                        jobId = jobId,
                        platform = platform
                    )

                    Result.success(
                        workDataOf(
                            OUTPUT_JOB_ID to
                                    jobId,
                            OUTPUT_PLATFORM to
                                    platform,
                            OUTPUT_PROGRESS to
                                    100,
                            OUTPUT_STATUS to
                                    "completed"
                        )
                    )
                }

                is PublishResult.Failure -> {

                    publishRepository.updateJobStatus(
                        id = jobId,
                        status = "failed",
                        progress = job.progress,
                        errorMessage =
                            result.message
                    )

                    showFailedNotification(
                        jobId = jobId,
                        platform = platform
                    )

                    Result.failure(
                        workDataOf(
                            OUTPUT_JOB_ID to
                                    jobId,
                            OUTPUT_PLATFORM to
                                    platform,
                            OUTPUT_PROGRESS to
                                    job.progress,
                            OUTPUT_STATUS to
                                    "failed"
                        )
                    )
                }
            }

        } catch (exception: Exception) {

            exception.printStackTrace()

            publishRepository.updateJobStatus(
                id = jobId,
                status = "failed",
                progress = 0,
                errorMessage =
                    exception.message
            )

            showFailedNotification(
                jobId = jobId,
                platform = platform
            )

            Result.failure(
                workDataOf(
                    OUTPUT_JOB_ID to
                            jobId,
                    OUTPUT_PLATFORM to
                            platform,
                    OUTPUT_PROGRESS to
                            0,
                    OUTPUT_STATUS to
                            "failed"
                )
            )
        }
    }

    private fun createForegroundInfo(
        jobId: Long,
        platform: String,
        progress: Int
    ): ForegroundInfo {

        val notification =
            notificationHelper
                .buildProgressNotification(
                    jobId = jobId,
                    platform = platform,
                    progress = progress
                )

        return ForegroundInfo(
            notificationHelper.getNotificationId(
                jobId
            ),
            notification
        )
    }

    private fun showCompletedNotification(
        jobId: Long,
        platform: String
    ) {

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        manager.notify(
            notificationHelper.getNotificationId(
                jobId
            ),
            notificationHelper
                .buildCompletedNotification(
                    jobId = jobId,
                    platform = platform
                )
        )
    }

    private fun showFailedNotification(
        jobId: Long,
        platform: String
    ) {

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        manager.notify(
            notificationHelper.getNotificationId(
                jobId
            ),
            notificationHelper
                .buildFailedNotification(
                    jobId = jobId,
                    platform = platform
                )
        )
    }
}
package com.example.multipost.repository

import android.content.ContentValues
import android.content.Context
import com.example.multipost.database.DatabaseHelper
import com.example.multipost.model.PublishJob

class PublishRepository(
    context: Context
) {

    private val database =
        DatabaseHelper.getInstance(context)

    fun insertJob(
        job: PublishJob
    ): Long {

        val values =
            ContentValues().apply {

                putNullable(
                    "video_id",
                    job.videoId
                )

                put(
                    "platform",
                    job.platform
                )

                put(
                    "caption",
                    job.caption
                )

                put(
                    "status",
                    job.status
                )

                put(
                    "progress",
                    job.progress
                )

                putNullable(
                    "remote_post_id",
                    job.remotePostId
                )

                putNullable(
                    "error_message",
                    job.errorMessage
                )

                put(
                    "created_at",
                    job.createdAt
                )

                put(
                    "updated_at",
                    job.updatedAt
                )
            }

        return database
            .writableDatabase
            .insert(
                "publish_jobs",
                null,
                values
            )
    }

    fun getJobById(
        id: Long
    ): PublishJob? {

        val cursor =
            database.readableDatabase.query(
                "publish_jobs",
                null,
                "id = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

        cursor.use {

            if (!it.moveToFirst()) {
                return null
            }

            return PublishJob(
                id = it.getLong(
                    it.getColumnIndexOrThrow("id")
                ),
                videoId = it.getNullableLong(
                    "video_id"
                ),
                platform = it.getString(
                    it.getColumnIndexOrThrow("platform")
                ),
                caption = it.getString(
                    it.getColumnIndexOrThrow("caption")
                ),
                status = it.getString(
                    it.getColumnIndexOrThrow("status")
                ),
                progress = it.getInt(
                    it.getColumnIndexOrThrow("progress")
                ),
                remotePostId = it.getString(
                    it.getColumnIndexOrThrow("remote_post_id")
                ),
                errorMessage = it.getString(
                    it.getColumnIndexOrThrow("error_message")
                ),
                createdAt = it.getLong(
                    it.getColumnIndexOrThrow("created_at")
                ),
                updatedAt = it.getLong(
                    it.getColumnIndexOrThrow("updated_at")
                )
            )
        }
    }

    fun getAllJobs(): List<PublishJob> {

        val jobs = mutableListOf<PublishJob>()

        val cursor =
            database.readableDatabase.query(
                "publish_jobs",
                null,
                null,
                null,
                null,
                null,
                "created_at DESC"
            )

        cursor.use {

            while (it.moveToNext()) {

                jobs.add(
                    PublishJob(
                        id = it.getLong(
                            it.getColumnIndexOrThrow("id")
                        ),
                        videoId = it.getNullableLong(
                            "video_id"
                        ),
                        platform = it.getString(
                            it.getColumnIndexOrThrow("platform")
                        ),
                        caption = it.getString(
                            it.getColumnIndexOrThrow("caption")
                        ),
                        status = it.getString(
                            it.getColumnIndexOrThrow("status")
                        ),
                        progress = it.getInt(
                            it.getColumnIndexOrThrow("progress")
                        ),
                        remotePostId = it.getString(
                            it.getColumnIndexOrThrow("remote_post_id")
                        ),
                        errorMessage = it.getString(
                            it.getColumnIndexOrThrow("error_message")
                        ),
                        createdAt = it.getLong(
                            it.getColumnIndexOrThrow("created_at")
                        ),
                        updatedAt = it.getLong(
                            it.getColumnIndexOrThrow("updated_at")
                        )
                    )
                )
            }
        }

        return jobs
    }

    fun updateJobStatus(
        id: Long,
        status: String,
        progress: Int,
        errorMessage: String? = null
    ): Int {

        val values =
            ContentValues().apply {

                put(
                    "status",
                    status
                )

                put(
                    "progress",
                    progress
                )

                putNullable(
                    "error_message",
                    errorMessage
                )

                put(
                    "updated_at",
                    System.currentTimeMillis()
                )
            }

        return database
            .writableDatabase
            .update(
                "publish_jobs",
                values,
                "id = ?",
                arrayOf(id.toString())
            )
    }

    fun deleteJob(
        id: Long
    ): Int {

        return database
            .writableDatabase
            .delete(
                "publish_jobs",
                "id = ?",
                arrayOf(id.toString())
            )
    }

    private fun ContentValues.putNullable(
        key: String,
        value: Long?
    ) {

        if (value == null) {
            putNull(key)
        } else {
            put(key, value)
        }
    }

    private fun ContentValues.putNullable(
        key: String,
        value: String?
    ) {

        if (value == null) {
            putNull(key)
        } else {
            put(key, value)
        }
    }

    private fun android.database.Cursor.getNullableLong(
        columnName: String
    ): Long? {

        val index =
            getColumnIndexOrThrow(columnName)

        return if (isNull(index)) {
            null
        } else {
            getLong(index)
        }
    }
}
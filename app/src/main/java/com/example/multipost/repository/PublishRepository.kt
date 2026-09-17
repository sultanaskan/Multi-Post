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
                    "youtube_title",
                    job.youtubeTitle
                )

                put(
                    "youtube_privacy",
                    job.youtubePrivacy
                )

                put(
                    "hashtags",
                    job.hashtags
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

            return cursorToJob(it)
        }
    }

    fun getAllJobs(): List<PublishJob> {

        val jobs =
            mutableListOf<PublishJob>()

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
                    cursorToJob(it)
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

    fun updateRemotePostId(
        id: Long,
        remotePostId: String
    ): Int {

        val values =
            ContentValues().apply {

                put(
                    "remote_post_id",
                    remotePostId
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

    private fun cursorToJob(
        cursor: android.database.Cursor
    ): PublishJob {

        return PublishJob(

            id =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        "id"
                    )
                ),

            videoId =
                cursor.getNullableLong(
                    "video_id"
                ),

            platform =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "platform"
                    )
                ),

            caption =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "caption"
                    )
                ),

            youtubeTitle =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "youtube_title"
                    )
                ),

            youtubePrivacy =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "youtube_privacy"
                    )
                ),

            hashtags =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "hashtags"
                    )
                ),

            status =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "status"
                    )
                ),

            progress =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        "progress"
                    )
                ),

            remotePostId =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "remote_post_id"
                    )
                ),

            errorMessage =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        "error_message"
                    )
                ),

            createdAt =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        "created_at"
                    )
                ),

            updatedAt =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        "updated_at"
                    )
                )
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
            getColumnIndexOrThrow(
                columnName
            )

        return if (isNull(index)) {
            null
        } else {
            getLong(index)
        }
    }
}
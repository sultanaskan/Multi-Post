package com.example.multipost.repository

import android.content.ContentValues
import android.content.Context
import com.example.multipost.database.DatabaseHelper
import com.example.multipost.model.VideoModel

class VideoRepository(
    context: Context
) {

    private val database =
        DatabaseHelper.getInstance(context)

    fun insertVideo(
        video: VideoModel
    ): Long {

        val values =
            ContentValues().apply {
                put("uri", video.uri)
                put("file_name", video.fileName)
                put("duration", video.duration)
                put("width", video.width)
                put("height", video.height)
                put("file_size", video.fileSize)
                put("thumbnail_path", video.thumbnailPath)
                put("created_at", video.createdAt)
            }

        return database
            .writableDatabase
            .insert(
                "videos",
                null,
                values
            )
    }

    fun getVideoById(
        id: Long
    ): VideoModel? {

        val cursor =
            database.readableDatabase.query(
                "videos",
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

            return cursorToVideo(it)
        }
    }

    fun getVideoByUri(
        uri: String
    ): VideoModel? {

        val cursor =
            database.readableDatabase.query(
                "videos",
                null,
                "uri = ?",
                arrayOf(uri),
                null,
                null,
                "id DESC",
                "1"
            )

        cursor.use {

            if (!it.moveToFirst()) {
                return null
            }

            return cursorToVideo(it)
        }
    }

    fun getAllVideos(): List<VideoModel> {

        val videos = mutableListOf<VideoModel>()

        val cursor =
            database.readableDatabase.query(
                "videos",
                null,
                null,
                null,
                null,
                null,
                "created_at DESC"
            )

        cursor.use {

            while (it.moveToNext()) {
                videos.add(
                    cursorToVideo(it)
                )
            }
        }

        return videos
    }

    fun deleteVideo(
        id: Long
    ): Int {

        return database
            .writableDatabase
            .delete(
                "videos",
                "id = ?",
                arrayOf(id.toString())
            )
    }

    private fun cursorToVideo(
        cursor: android.database.Cursor
    ): VideoModel {

        return VideoModel(
            id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
            ),
            uri = cursor.getString(
                cursor.getColumnIndexOrThrow("uri")
            ),
            fileName = cursor.getString(
                cursor.getColumnIndexOrThrow("file_name")
            ),
            duration = cursor.getLong(
                cursor.getColumnIndexOrThrow("duration")
            ),
            width = cursor.getInt(
                cursor.getColumnIndexOrThrow("width")
            ),
            height = cursor.getInt(
                cursor.getColumnIndexOrThrow("height")
            ),
            fileSize = cursor.getLong(
                cursor.getColumnIndexOrThrow("file_size")
            ),
            thumbnailPath = cursor.getString(
                cursor.getColumnIndexOrThrow("thumbnail_path")
            ),
            createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")
            )
        )
    }

    fun close() {
        database.close()
    }
}
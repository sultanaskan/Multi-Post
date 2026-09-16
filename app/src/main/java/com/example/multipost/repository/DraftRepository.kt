package com.example.multipost.repository

import android.content.ContentValues
import android.content.Context
import com.example.multipost.database.DatabaseHelper
import com.example.multipost.model.DraftModel

class DraftRepository(
    context: Context
) {

    private val database =
        DatabaseHelper.getInstance(context)

    fun insertDraft(
        draft: DraftModel
    ): Long {

        val values =
            ContentValues().apply {

                putNullable(
                    "video_id",
                    draft.videoId
                )

                put(
                    "caption",
                    draft.caption
                )

                put(
                    "hashtags",
                    draft.hashtags
                )

                put(
                    "platforms",
                    draft.platforms
                )

                put(
                    "created_at",
                    draft.createdAt
                )

                put(
                    "updated_at",
                    draft.updatedAt
                )
            }

        return database
            .writableDatabase
            .insert(
                "drafts",
                null,
                values
            )
    }

    fun getDraftById(
        id: Long
    ): DraftModel? {

        val cursor =
            database.readableDatabase.query(
                "drafts",
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

            return DraftModel(
                id = it.getLong(
                    it.getColumnIndexOrThrow("id")
                ),
                videoId = it.getNullableLong(
                    "video_id"
                ),
                caption = it.getString(
                    it.getColumnIndexOrThrow("caption")
                ),
                hashtags = it.getString(
                    it.getColumnIndexOrThrow("hashtags")
                ),
                platforms = it.getString(
                    it.getColumnIndexOrThrow("platforms")
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

    fun getAllDrafts(): List<DraftModel> {

        val drafts = mutableListOf<DraftModel>()

        val cursor =
            database.readableDatabase.query(
                "drafts",
                null,
                null,
                null,
                null,
                null,
                "updated_at DESC"
            )

        cursor.use {

            while (it.moveToNext()) {

                drafts.add(
                    DraftModel(
                        id = it.getLong(
                            it.getColumnIndexOrThrow("id")
                        ),
                        videoId = it.getNullableLong(
                            "video_id"
                        ),
                        caption = it.getString(
                            it.getColumnIndexOrThrow("caption")
                        ),
                        hashtags = it.getString(
                            it.getColumnIndexOrThrow("hashtags")
                        ),
                        platforms = it.getString(
                            it.getColumnIndexOrThrow("platforms")
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

        return drafts
    }

    fun deleteDraft(
        id: Long
    ): Int {

        return database
            .writableDatabase
            .delete(
                "drafts",
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
package com.example.multipost.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(
        db: SQLiteDatabase
    ) {

        db.execSQL(
            """
            CREATE TABLE videos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uri TEXT NOT NULL,
                file_name TEXT NOT NULL,
                duration INTEGER DEFAULT 0,
                width INTEGER DEFAULT 0,
                height INTEGER DEFAULT 0,
                file_size INTEGER DEFAULT 0,
                thumbnail_path TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE social_accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                platform TEXT NOT NULL,
                account_id TEXT NOT NULL,
                account_name TEXT NOT NULL,
                access_token TEXT,
                refresh_token TEXT,
                token_expires_at INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE drafts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                video_id INTEGER,
                caption TEXT,
                hashtags TEXT,
                platforms TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(video_id)
                    REFERENCES videos(id)
                    ON DELETE SET NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE publish_jobs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                video_id INTEGER,
                platform TEXT NOT NULL,
                caption TEXT,
                youtube_title TEXT DEFAULT '',
                youtube_privacy TEXT DEFAULT 'Private',
                hashtags TEXT DEFAULT '',
                status TEXT NOT NULL,
                progress INTEGER DEFAULT 0,
                remote_post_id TEXT,
                error_message TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(video_id)
                    REFERENCES videos(id)
                    ON DELETE SET NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 2) {

            db.execSQL(
                """
                ALTER TABLE publish_jobs
                ADD COLUMN youtube_title TEXT DEFAULT ''
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE publish_jobs
                ADD COLUMN youtube_privacy TEXT DEFAULT 'Private'
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE publish_jobs
                ADD COLUMN hashtags TEXT DEFAULT ''
                """.trimIndent()
            )
        }
    }

    companion object {

        private const val DATABASE_NAME =
            "multipost.db"

        private const val DATABASE_VERSION =
            2

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(
            context: Context
        ): DatabaseHelper {

            return INSTANCE
                ?: synchronized(this) {

                    INSTANCE
                        ?: DatabaseHelper(
                            context.applicationContext
                        ).also {
                            INSTANCE = it
                        }
                }
        }
    }
}
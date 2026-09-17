package com.example.multipost.upload

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.multipost.MainActivity
import com.example.multipost.auth.YouTubeAuthActivity

class UploadNotificationHelper(
    private val context: Context
) {

    companion object {

        const val CHANNEL_ID =
            "multipost_uploads"

        const val CHANNEL_NAME =
            "Upload Progress"

        const val NOTIFICATION_ID_BASE =
            5000

        const val EXTRA_OPEN_HISTORY =
            "open_history"

        const val EXTRA_AUTH_REQUIRED =
            "auth_required"
    }

    init {
        createChannel()
    }

    private fun createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {

                    description =
                        "Shows MultiPost upload progress"
                }

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createHistoryPendingIntent(
        jobId: Long
    ): PendingIntent {

        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                putExtra(
                    EXTRA_OPEN_HISTORY,
                    true
                )

                putExtra(
                    "job_id",
                    jobId
                )

                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            context,
            getNotificationId(jobId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createYouTubeAuthPendingIntent(
        jobId: Long
    ): PendingIntent {

        val intent =
            Intent(
                context,
                YouTubeAuthActivity::class.java
            ).apply {

                putExtra(
                    EXTRA_AUTH_REQUIRED,
                    true
                )

                putExtra(
                    "job_id",
                    jobId
                )

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            context,
            getNotificationId(jobId) + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildProgressNotification(
        jobId: Long,
        platform: String,
        progress: Int
    ) =
        NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.stat_sys_upload
            )
            .setContentTitle(
                "Uploading $platform"
            )
            .setContentText(
                "$progress% completed"
            )
            .setProgress(
                100,
                progress.coerceIn(0, 100),
                false
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(
                createHistoryPendingIntent(
                    jobId
                )
            )
            .build()

    fun buildCompletedNotification(
        jobId: Long,
        platform: String
    ) =
        NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.stat_sys_upload_done
            )
            .setContentTitle(
                "Upload completed"
            )
            .setContentText(
                "$platform upload completed successfully"
            )
            .setAutoCancel(true)
            .setContentIntent(
                createHistoryPendingIntent(
                    jobId
                )
            )
            .build()

    fun buildAuthenticationRequiredNotification(
        jobId: Long,
        platform: String
    ) =
        NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.stat_notify_error
            )
            .setContentTitle(
                "$platform authorization required"
            )
            .setContentText(
                "Your $platform authorization has expired. Reconnect your account."
            )
            .setAutoCancel(true)
            .setContentIntent(
                createYouTubeAuthPendingIntent(
                    jobId
                )
            )
            .addAction(
                android.R.drawable.ic_menu_manage,
                "Reconnect",
                createYouTubeAuthPendingIntent(
                    jobId
                )
            )
            .build()

    fun buildFailedNotification(
        jobId: Long,
        platform: String
    ) =
        NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.stat_notify_error
            )
            .setContentTitle(
                "Upload failed"
            )
            .setContentText(
                "$platform upload failed"
            )
            .setAutoCancel(true)
            .setContentIntent(
                createHistoryPendingIntent(
                    jobId
                )
            )
            .build()

    fun getNotificationId(
        jobId: Long
    ): Int {

        return NOTIFICATION_ID_BASE +
                jobId.toInt()
    }
}
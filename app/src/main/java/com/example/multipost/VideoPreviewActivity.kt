package com.example.multipost

import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import java.util.Locale
import androidx.core.net.toUri

@UnstableApi
class VideoPreviewActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var backButton: ImageButton
    private lateinit var continueButton: MaterialButton
    private lateinit var videoFileName: TextView
    private lateinit var videoDetails: TextView
    private lateinit var videoThumbnail: ImageView

    private var player: ExoPlayer? = null
    private var videoUri: Uri? = null
    private var videoFileSize = 0L
    private var isPlayerReleased = false

    companion object {
        const val EXTRA_VIDEO_URI = "video_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_preview)

        initializeViews()

        val videoUriString =
            intent.getStringExtra(EXTRA_VIDEO_URI)

        if (videoUriString.isNullOrEmpty()) {
            showErrorAndFinish("Video could not be opened.")
            return
        }

        videoUri =
            videoUriString.toUri()

        setupButtons()

        loadVideoInformation()

        generateThumbnail()

        initializePlayer(videoUri!!)
    }

    private fun initializeViews() {

        playerView =
            findViewById(R.id.playerView)

        backButton =
            findViewById(R.id.backButton)

        continueButton =
            findViewById(R.id.continueButton)

        videoFileName =
            findViewById(R.id.videoFileName)

        videoDetails =
            findViewById(R.id.videoDetails)

        videoThumbnail =
            findViewById(R.id.videoThumbnail)
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        continueButton.setOnClickListener {

            val uri =
                videoUri ?: return@setOnClickListener

            val intent =
                android.content.Intent(
                    this,
                    PostComposerActivity::class.java
                )

            intent.putExtra(
                PostComposerActivity.EXTRA_VIDEO_URI,
                uri.toString()
            )

            startActivity(intent)
        }
    }

    private fun initializePlayer(
        uri: Uri
    ) {

        try {

            player =
                ExoPlayer.Builder(this)
                    .build()

            playerView.player =
                player

            val mediaItem =
                MediaItem.fromUri(uri)

            player?.setMediaItem(
                mediaItem
            )

            player?.addListener(
                object : Player.Listener {

                    override fun onPlaybackStateChanged(
                        playbackState: Int
                    ) {

                        when (playbackState) {

                            Player.STATE_READY -> {
                                updateVideoDetails()
                            }

                            Player.STATE_BUFFERING -> {
                                // Media3 handles buffering UI.
                            }

                            Player.STATE_ENDED -> {
                                // Video finished.
                            }

                            Player.STATE_IDLE -> {
                                // Player is idle.
                            }
                        }
                    }

                    override fun onPlayerError(
                        error: PlaybackException
                    ) {

                        showPlaybackError()
                    }
                }
            )

            player?.prepare()

            player?.playWhenReady =
                false

        } catch (exception: Exception) {

            exception.printStackTrace()

            showErrorAndFinish(
                "Unable to load this video."
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadVideoInformation() {

        val uri =
            videoUri ?: return

        val resolver =
            contentResolver

        var fileName =
            "Video"

        try {

            val cursor: Cursor? =
                resolver.query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME,
                        OpenableColumns.SIZE
                    ),
                    null,
                    null,
                    null
                )

            cursor?.use {

                if (it.moveToFirst()) {

                    val nameIndex =
                        it.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    val sizeIndex =
                        it.getColumnIndex(
                            OpenableColumns.SIZE
                        )

                    if (nameIndex >= 0) {

                        fileName =
                            it.getString(
                                nameIndex
                            )
                    }

                    if (sizeIndex >= 0) {

                        videoFileSize =
                            it.getLong(
                                sizeIndex
                            )
                    }
                }
            }

        } catch (exception: Exception) {

            exception.printStackTrace()
        }

        videoFileName.text =
            fileName

        videoDetails.text =
            "Loading video information..."
    }

    private fun generateThumbnail() {

        val uri =
            videoUri ?: return

        Thread {

            var bitmap: Bitmap? = null

            val retriever =
                MediaMetadataRetriever()

            try {

                retriever.setDataSource(
                    this,
                    uri
                )

                bitmap =
                    retriever.getFrameAtTime(
                        1_000_000L,
                        MediaMetadataRetriever
                            .OPTION_CLOSEST_SYNC
                    )

            } catch (exception: Exception) {

                exception.printStackTrace()

            } finally {

                try {
                    retriever.release()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

            runOnUiThread {

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                if (bitmap != null) {

                    videoThumbnail.setImageBitmap(
                        bitmap
                    )
                }
            }

        }.start()
    }

    @SuppressLint("SetTextI18n")
    @androidx.annotation.OptIn(UnstableApi::class)
    private fun updateVideoDetails() {

        val currentPlayer =
            player ?: return

        val duration =
            currentPlayer.duration

        val width =
            currentPlayer
                .videoFormat
                ?.width ?: 0

        val height =
            currentPlayer
                .videoFormat
                ?.height ?: 0

        val durationText =
            formatDuration(
                duration
            )

        val resolutionText =
            if (
                width > 0 &&
                height > 0
            ) {

                "$width × $height"

            } else {

                "Unknown resolution"
            }

        val formattedSize =
            formatFileSize(
                videoFileSize
            )

        videoDetails.text =
            "$resolutionText • " +
                    "$durationText • " +
                    formattedSize
    }

    @SuppressLint("SetTextI18n")
    private fun showPlaybackError() {

        if (
            isFinishing ||
            isDestroyed
        ) {
            return
        }

        videoDetails.text =
            "Unable to play this video"

        Toast.makeText(
            this,
            "This video cannot be played.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showErrorAndFinish(
        message: String
    ) {

        if (
            isFinishing ||
            isDestroyed
        ) {
            return
        }

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    private fun formatFileSize(
        bytes: Long
    ): String {

        if (bytes <= 0) {
            return "Unknown size"
        }

        val kb =
            bytes / 1024.0

        if (kb < 1024) {

            return String.format(
                Locale.US,
                "%.1f KB",
                kb
            )
        }

        val mb =
            kb / 1024.0

        if (mb < 1024) {

            return String.format(
                Locale.US,
                "%.1f MB",
                mb
            )
        }

        val gb =
            mb / 1024.0

        return String.format(
            Locale.US,
            "%.2f GB",
            gb
        )
    }

    private fun formatDuration(
        durationMs: Long
    ): String {

        if (durationMs <= 0) {
            return "00:00"
        }

        val totalSeconds =
            durationMs / 1000

        val hours =
            totalSeconds / 3600

        val minutes =
            (totalSeconds % 3600) / 60

        val seconds =
            totalSeconds % 60

        return if (hours > 0) {

            String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        } else {

            String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds
            )
        }
    }

    override fun onStart() {

        super.onStart()

        if (
            player == null &&
            videoUri != null &&
            !isFinishing
        ) {

            isPlayerReleased = false

            initializePlayer(
                videoUri!!
            )
        }
    }

    override fun onStop() {

        super.onStop()

        releasePlayer()
    }

    private fun releasePlayer() {

        if (isPlayerReleased) {
            return
        }

        isPlayerReleased = true

        player?.release()

        player = null

        playerView.player = null
    }
}
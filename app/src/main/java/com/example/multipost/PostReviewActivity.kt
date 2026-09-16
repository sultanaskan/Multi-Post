package com.example.multipost

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.multipost.model.DraftModel
import com.example.multipost.model.PublishJob
import com.example.multipost.model.VideoModel
import com.example.multipost.repository.DraftRepository
import com.example.multipost.repository.PublishRepository
import com.example.multipost.repository.VideoRepository
import com.example.multipost.upload.UploadManager
import com.google.android.material.button.MaterialButton

class PostReviewActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var videoThumbnail: ImageView
    private lateinit var captionText: TextView
    private lateinit var hashtagsText: TextView
    private lateinit var platformsText: TextView

    private lateinit var youtubeSettingsText: TextView
    private lateinit var instagramSettingsText: TextView
    private lateinit var facebookSettingsText: TextView
    private lateinit var tiktokSettingsText: TextView

    private lateinit var publishButton: MaterialButton
    private lateinit var saveDraftButton: MaterialButton

    private lateinit var videoRepository: VideoRepository
    private lateinit var draftRepository: DraftRepository
    private lateinit var publishRepository: PublishRepository
    private lateinit var uploadManager: UploadManager

    private var videoUri: Uri? = null

    companion object {

        const val EXTRA_VIDEO_URI =
            "video_uri"

        const val EXTRA_CAPTION =
            "caption"

        const val EXTRA_HASHTAGS =
            "hashtags"

        const val EXTRA_PLATFORMS =
            "platforms"

        const val EXTRA_YOUTUBE_TITLE =
            "youtube_title"

        const val EXTRA_YOUTUBE_PRIVACY =
            "youtube_privacy"

        const val EXTRA_INSTAGRAM_CONTENT_TYPE =
            "instagram_content_type"

        const val EXTRA_FACEBOOK_CONTENT_TYPE =
            "facebook_content_type"

        const val EXTRA_TIKTOK_PRIVACY =
            "tiktok_privacy"
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_post_review
        )

        initializeViews()
        initializeRepositories()
        loadPostData()
        setupButtons()
        generateThumbnail()
    }

    private fun initializeViews() {

        backButton =
            findViewById(
                R.id.backButton
            )

        videoThumbnail =
            findViewById(
                R.id.videoThumbnail
            )

        captionText =
            findViewById(
                R.id.captionText
            )

        hashtagsText =
            findViewById(
                R.id.hashtagsText
            )

        platformsText =
            findViewById(
                R.id.platformsText
            )

        youtubeSettingsText =
            findViewById(
                R.id.youtubeSettingsText
            )

        instagramSettingsText =
            findViewById(
                R.id.instagramSettingsText
            )

        facebookSettingsText =
            findViewById(
                R.id.facebookSettingsText
            )

        tiktokSettingsText =
            findViewById(
                R.id.tiktokSettingsText
            )

        publishButton =
            findViewById(
                R.id.publishButton
            )

        saveDraftButton =
            findViewById(
                R.id.saveDraftButton
            )
    }

    private fun initializeRepositories() {

        videoRepository =
            VideoRepository(this)

        draftRepository =
            DraftRepository(this)

        publishRepository =
            PublishRepository(this)

        uploadManager =
            UploadManager(this)
    }

    private fun loadPostData() {

        val videoUriString =
            intent.getStringExtra(
                EXTRA_VIDEO_URI
            )

        if (
            videoUriString.isNullOrEmpty()
        ) {

            Toast.makeText(
                this,
                "Video not found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        videoUri =
            Uri.parse(
                videoUriString
            )

        val caption =
            intent.getStringExtra(
                EXTRA_CAPTION
            ).orEmpty()

        val hashtags =
            intent.getStringExtra(
                EXTRA_HASHTAGS
            ).orEmpty()

        val platforms =
            intent.getStringExtra(
                EXTRA_PLATFORMS
            ).orEmpty()

        val youtubeTitle =
            intent.getStringExtra(
                EXTRA_YOUTUBE_TITLE
            ).orEmpty()

        val youtubePrivacy =
            intent.getStringExtra(
                EXTRA_YOUTUBE_PRIVACY
            ).orEmpty()

        val instagramContentType =
            intent.getStringExtra(
                EXTRA_INSTAGRAM_CONTENT_TYPE
            ).orEmpty()

        val facebookContentType =
            intent.getStringExtra(
                EXTRA_FACEBOOK_CONTENT_TYPE
            ).orEmpty()

        val tiktokPrivacy =
            intent.getStringExtra(
                EXTRA_TIKTOK_PRIVACY
            ).orEmpty()

        captionText.text =
            caption

        hashtagsText.text =
            if (hashtags.isEmpty()) {
                "No hashtags"
            } else {
                hashtags
            }

        platformsText.text =
            platforms

        youtubeSettingsText.text =
            buildYouTubeSettings(
                youtubeTitle,
                youtubePrivacy
            )

        instagramSettingsText.text =
            buildInstagramSettings(
                instagramContentType
            )

        facebookSettingsText.text =
            buildFacebookSettings(
                facebookContentType
            )

        tiktokSettingsText.text =
            buildTikTokSettings(
                tiktokPrivacy
            )

        updateSettingsVisibility(
            platforms
        )
    }

    private fun buildYouTubeSettings(
        title: String,
        privacy: String
    ): String {

        return "Title: $title\nPrivacy: $privacy"
    }

    private fun buildInstagramSettings(
        contentType: String
    ): String {

        return "Content Type: $contentType"
    }

    private fun buildFacebookSettings(
        contentType: String
    ): String {

        return "Content Type: $contentType"
    }

    private fun buildTikTokSettings(
        privacy: String
    ): String {

        return "Privacy: $privacy"
    }

    private fun updateSettingsVisibility(
        platforms: String
    ) {

        youtubeSettingsText.visibility =
            if (
                platforms.contains("YouTube")
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        instagramSettingsText.visibility =
            if (
                platforms.contains("Instagram")
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        facebookSettingsText.visibility =
            if (
                platforms.contains("Facebook")
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        tiktokSettingsText.visibility =
            if (
                platforms.contains("TikTok")
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        publishButton.setOnClickListener {
            publishPost()
        }

        saveDraftButton.setOnClickListener {
            saveDraft()
        }
    }

    private fun saveDraft() {

        val uri =
            videoUri

        if (uri == null) {

            Toast.makeText(
                this,
                "Video not found.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        saveDraftButton.isEnabled =
            false

        Thread {

            try {

                val videoId =
                    getOrCreateVideo(uri)

                val draft =
                    DraftModel(
                        videoId = videoId,
                        caption =
                            intent.getStringExtra(
                                EXTRA_CAPTION
                            ).orEmpty(),
                        hashtags =
                            intent.getStringExtra(
                                EXTRA_HASHTAGS
                            ).orEmpty(),
                        platforms =
                            intent.getStringExtra(
                                EXTRA_PLATFORMS
                            ).orEmpty()
                    )

                val draftId =
                    draftRepository.insertDraft(
                        draft
                    )

                runOnUiThread {

                    saveDraftButton.isEnabled =
                        true

                    if (draftId > 0) {

                        Toast.makeText(
                            this,
                            "Draft saved successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            "Failed to save draft.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (exception: Exception) {

                exception.printStackTrace()

                runOnUiThread {

                    saveDraftButton.isEnabled =
                        true

                    Toast.makeText(
                        this,
                        "Draft save failed.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    private fun publishPost() {

        val uri =
            videoUri

        if (uri == null) {

            Toast.makeText(
                this,
                "Video not found.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val platforms =
            intent.getStringExtra(
                EXTRA_PLATFORMS
            )
                .orEmpty()
                .split(",")
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotEmpty()
                }

        if (platforms.isEmpty()) {

            Toast.makeText(
                this,
                "No platform selected.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        publishButton.isEnabled =
            false

        Thread {

            try {

                val videoId =
                    getOrCreateVideo(uri)

                val caption =
                    intent.getStringExtra(
                        EXTRA_CAPTION
                    ).orEmpty()

                var createdJobs =
                    0

                platforms.forEach { platform ->

                    val job =
                        PublishJob(
                            videoId = videoId,
                            platform = platform,
                            caption = caption,
                            status = "pending",
                            progress = 0
                        )

                    val jobId =
                        publishRepository.insertJob(
                            job
                        )

                    if (jobId > 0) {

                        createdJobs++

                        uploadManager.enqueueUpload(
                            jobId = jobId,
                            platform = platform
                        )
                    }
                }

                uploadManager.enqueuePendingJobs()

                runOnUiThread {

                    publishButton.isEnabled =
                        true

                    if (
                        createdJobs ==
                        platforms.size
                    ) {

                        Toast.makeText(
                            this,
                            "$createdJobs upload job(s) queued successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        openHistory()

                    } else {

                        Toast.makeText(
                            this,
                            "Some upload jobs could not be created.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (exception: Exception) {

                exception.printStackTrace()

                runOnUiThread {

                    publishButton.isEnabled =
                        true

                    Toast.makeText(
                        this,
                        "Upload job creation failed.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    private fun openHistory() {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                putExtra(
                    "open_history",
                    true
                )

                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        startActivity(intent)

        finish()
    }

    private fun getOrCreateVideo(
        uri: Uri
    ): Long {

        val existingVideo =
            videoRepository.getVideoByUri(
                uri.toString()
            )

        if (existingVideo != null) {
            return existingVideo.id
        }

        val metadata =
            readVideoMetadata(uri)

        val video =
            VideoModel(
                uri = uri.toString(),
                fileName = metadata.fileName,
                duration = metadata.duration,
                width = metadata.width,
                height = metadata.height,
                fileSize = metadata.fileSize
            )

        return videoRepository.insertVideo(
            video
        )
    }

    private fun readVideoMetadata(
        uri: Uri
    ): VideoMetadata {

        var fileName =
            "video"

        var fileSize =
            0L

        try {

            contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                ),
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {

                    val nameIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    val sizeIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.SIZE
                        )

                    if (nameIndex >= 0) {

                        fileName =
                            cursor.getString(
                                nameIndex
                            )
                    }

                    if (
                        sizeIndex >= 0 &&
                        !cursor.isNull(sizeIndex)
                    ) {

                        fileSize =
                            cursor.getLong(
                                sizeIndex
                            )
                    }
                }
            }

        } catch (exception: Exception) {

            exception.printStackTrace()
        }

        var duration =
            0L

        var width =
            0

        var height =
            0

        val retriever =
            MediaMetadataRetriever()

        try {

            retriever.setDataSource(
                this,
                uri
            )

            duration =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull()
                    ?: 0L

            width =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                )?.toIntOrNull()
                    ?: 0

            height =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                )?.toIntOrNull()
                    ?: 0

        } catch (exception: Exception) {

            exception.printStackTrace()

        } finally {

            try {
                retriever.release()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        return VideoMetadata(
            fileName = fileName,
            duration = duration,
            width = width,
            height = height,
            fileSize = fileSize
        )
    }

    private fun generateThumbnail() {

        val uri =
            videoUri
                ?: return

        Thread {

            var bitmap: Bitmap? =
                null

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
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
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

    private data class VideoMetadata(
        val fileName: String,
        val duration: Long,
        val width: Int,
        val height: Int,
        val fileSize: Long
    )

}

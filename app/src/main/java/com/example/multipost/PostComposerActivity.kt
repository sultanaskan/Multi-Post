package com.example.multipost

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PostComposerActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton

    private lateinit var captionInput: TextInputEditText
    private lateinit var hashtagsInput: TextInputEditText

    private lateinit var youtubeButton: MaterialButton
    private lateinit var instagramButton: MaterialButton
    private lateinit var facebookButton: MaterialButton
    private lateinit var tiktokButton: MaterialButton

    private lateinit var continueButton: MaterialButton

    private lateinit var youtubeSettingsContainer: View
    private lateinit var instagramSettingsContainer: View
    private lateinit var facebookSettingsContainer: View
    private lateinit var tiktokSettingsContainer: View

    private lateinit var youtubeTitleInput: TextInputEditText
    private lateinit var youtubePrivacyButton: MaterialButton

    private lateinit var instagramContentTypeButton: MaterialButton

    private lateinit var facebookContentTypeButton: MaterialButton

    private lateinit var tiktokPrivacyButton: MaterialButton

    private var videoUri: Uri? = null

    private var youtubeSelected = false
    private var instagramSelected = false
    private var facebookSelected = false
    private var tiktokSelected = false

    private var youtubePrivacy = "Private"
    private var instagramContentType = "Reel"
    private var facebookContentType = "Reel"
    private var tiktokPrivacy = "Public"

    companion object {

        const val EXTRA_VIDEO_URI = "video_uri"
        const val EXTRA_CAPTION = "caption"
        const val EXTRA_HASHTAGS = "hashtags"
        const val EXTRA_PLATFORMS = "platforms"

        const val EXTRA_YOUTUBE_TITLE = "youtube_title"
        const val EXTRA_YOUTUBE_PRIVACY = "youtube_privacy"

        const val EXTRA_INSTAGRAM_CONTENT_TYPE =
            "instagram_content_type"

        const val EXTRA_FACEBOOK_CONTENT_TYPE =
            "facebook_content_type"

        const val EXTRA_TIKTOK_PRIVACY =
            "tiktok_privacy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_post_composer)

        initializeViews()
        loadVideoUri()
        setupButtons()

        updateAllPlatformButtons()
        updateAllSettingsVisibility()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        captionInput =
            findViewById(R.id.captionInput)

        hashtagsInput =
            findViewById(R.id.hashtagsInput)

        youtubeButton =
            findViewById(R.id.youtubeButton)

        instagramButton =
            findViewById(R.id.instagramButton)

        facebookButton =
            findViewById(R.id.facebookButton)

        tiktokButton =
            findViewById(R.id.tiktokButton)

        continueButton =
            findViewById(R.id.continueButton)

        youtubeSettingsContainer =
            findViewById(R.id.youtubeSettingsContainer)

        instagramSettingsContainer =
            findViewById(R.id.instagramSettingsContainer)

        facebookSettingsContainer =
            findViewById(R.id.facebookSettingsContainer)

        tiktokSettingsContainer =
            findViewById(R.id.tiktokSettingsContainer)

        youtubeTitleInput =
            findViewById(R.id.youtubeTitleInput)

        youtubePrivacyButton =
            findViewById(R.id.youtubePrivacyButton)

        instagramContentTypeButton =
            findViewById(
                R.id.instagramContentTypeButton
            )

        facebookContentTypeButton =
            findViewById(
                R.id.facebookContentTypeButton
            )

        tiktokPrivacyButton =
            findViewById(
                R.id.tiktokPrivacyButton
            )
    }

    private fun loadVideoUri() {

        val videoUriString =
            intent.getStringExtra(EXTRA_VIDEO_URI)

        if (videoUriString.isNullOrEmpty()) {

            Toast.makeText(
                this,
                "Video not found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        videoUri =
            Uri.parse(videoUriString)
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        youtubeButton.setOnClickListener {

            youtubeSelected =
                !youtubeSelected

            updatePlatformButton(
                youtubeButton,
                youtubeSelected
            )

            updateAllSettingsVisibility()
        }

        instagramButton.setOnClickListener {

            instagramSelected =
                !instagramSelected

            updatePlatformButton(
                instagramButton,
                instagramSelected
            )

            updateAllSettingsVisibility()
        }

        facebookButton.setOnClickListener {

            facebookSelected =
                !facebookSelected

            updatePlatformButton(
                facebookButton,
                facebookSelected
            )

            updateAllSettingsVisibility()
        }

        tiktokButton.setOnClickListener {

            tiktokSelected =
                !tiktokSelected

            updatePlatformButton(
                tiktokButton,
                tiktokSelected
            )

            updateAllSettingsVisibility()
        }

        youtubePrivacyButton.setOnClickListener {
            showYouTubePrivacyOptions()
        }

        instagramContentTypeButton.setOnClickListener {
            showInstagramContentTypeOptions()
        }

        facebookContentTypeButton.setOnClickListener {
            showFacebookContentTypeOptions()
        }

        tiktokPrivacyButton.setOnClickListener {
            showTikTokPrivacyOptions()
        }

        continueButton.setOnClickListener {
            openReviewScreen()
        }
    }

    private fun updateAllPlatformButtons() {

        updatePlatformButton(
            youtubeButton,
            youtubeSelected
        )

        updatePlatformButton(
            instagramButton,
            instagramSelected
        )

        updatePlatformButton(
            facebookButton,
            facebookSelected
        )

        updatePlatformButton(
            tiktokButton,
            tiktokSelected
        )
    }

    private fun updatePlatformButton(
        button: MaterialButton,
        selected: Boolean
    ) {

        if (selected) {

            button.alpha = 1.0f
            button.strokeWidth = 3

            button.text =
                "✓ ${getPlatformName(button)}"

        } else {

            button.alpha = 0.65f
            button.strokeWidth = 1

            button.text =
                getPlatformName(button)
        }
    }

    private fun getPlatformName(
        button: MaterialButton
    ): String {

        return when (button.id) {

            R.id.youtubeButton ->
                "YouTube"

            R.id.instagramButton ->
                "Instagram"

            R.id.facebookButton ->
                "Facebook"

            R.id.tiktokButton ->
                "TikTok"

            else ->
                ""
        }
    }

    private fun updateAllSettingsVisibility() {

        youtubeSettingsContainer.visibility =
            if (youtubeSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }

        instagramSettingsContainer.visibility =
            if (instagramSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }

        facebookSettingsContainer.visibility =
            if (facebookSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }

        tiktokSettingsContainer.visibility =
            if (tiktokSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun showYouTubePrivacyOptions() {

        val options =
            arrayOf(
                "Public",
                "Unlisted",
                "Private"
            )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("YouTube Privacy")
            .setSingleChoiceItems(
                options,
                options.indexOf(youtubePrivacy)
            ) { dialog, which ->

                youtubePrivacy =
                    options[which]

                youtubePrivacyButton.text =
                    youtubePrivacy

                dialog.dismiss()
            }
            .show()
    }

    private fun showInstagramContentTypeOptions() {

        val options =
            arrayOf(
                "Reel",
                "Post"
            )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Instagram Content Type")
            .setSingleChoiceItems(
                options,
                options.indexOf(instagramContentType)
            ) { dialog, which ->

                instagramContentType =
                    options[which]

                instagramContentTypeButton.text =
                    instagramContentType

                dialog.dismiss()
            }
            .show()
    }

    private fun showFacebookContentTypeOptions() {

        val options =
            arrayOf(
                "Reel",
                "Video"
            )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Facebook Content Type")
            .setSingleChoiceItems(
                options,
                options.indexOf(facebookContentType)
            ) { dialog, which ->

                facebookContentType =
                    options[which]

                facebookContentTypeButton.text =
                    facebookContentType

                dialog.dismiss()
            }
            .show()
    }

    private fun showTikTokPrivacyOptions() {

        val options =
            arrayOf(
                "Public",
                "Friends",
                "Private"
            )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("TikTok Privacy")
            .setSingleChoiceItems(
                options,
                options.indexOf(tiktokPrivacy)
            ) { dialog, which ->

                tiktokPrivacy =
                    options[which]

                tiktokPrivacyButton.text =
                    tiktokPrivacy

                dialog.dismiss()
            }
            .show()
    }

    private fun openReviewScreen() {

        val caption =
            captionInput.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val hashtags =
            hashtagsInput.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (caption.isEmpty()) {

            captionInput.error =
                "Enter a caption"

            captionInput.requestFocus()

            return
        }

        if (!hasSelectedPlatform()) {

            Toast.makeText(
                this,
                "Select at least one platform.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (youtubeSelected) {

            val youtubeTitle =
                youtubeTitleInput.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()

            if (youtubeTitle.isEmpty()) {

                youtubeTitleInput.error =
                    "Enter a YouTube title"

                youtubeTitleInput.requestFocus()

                return
            }
        }

        val intent =
            Intent(
                this,
                PostReviewActivity::class.java
            )

        intent.putExtra(
            EXTRA_VIDEO_URI,
            videoUri.toString()
        )

        intent.putExtra(
            EXTRA_CAPTION,
            caption
        )

        intent.putExtra(
            EXTRA_HASHTAGS,
            hashtags
        )

        intent.putExtra(
            EXTRA_PLATFORMS,
            getSelectedPlatforms()
        )

        intent.putExtra(
            EXTRA_YOUTUBE_TITLE,
            youtubeTitleInput.text
                ?.toString()
                ?.trim()
                .orEmpty()
        )

        intent.putExtra(
            EXTRA_YOUTUBE_PRIVACY,
            youtubePrivacy
        )

        intent.putExtra(
            EXTRA_INSTAGRAM_CONTENT_TYPE,
            instagramContentType
        )

        intent.putExtra(
            EXTRA_FACEBOOK_CONTENT_TYPE,
            facebookContentType
        )

        intent.putExtra(
            EXTRA_TIKTOK_PRIVACY,
            tiktokPrivacy
        )

        startActivity(intent)
    }

    private fun hasSelectedPlatform(): Boolean {

        return youtubeSelected ||
                instagramSelected ||
                facebookSelected ||
                tiktokSelected
    }

    private fun getSelectedPlatforms(): String {

        val platforms =
            mutableListOf<String>()

        if (youtubeSelected) {
            platforms.add("YouTube")
        }

        if (instagramSelected) {
            platforms.add("Instagram")
        }

        if (facebookSelected) {
            platforms.add("Facebook")
        }

        if (tiktokSelected) {
            platforms.add("TikTok")
        }

        return platforms.joinToString(", ")
    }

}

package com.example.multipost.publisher

import android.content.Context
import android.net.Uri
import com.example.multipost.auth.YouTubeTokenProvider
import com.example.multipost.model.PublishJob
import com.example.multipost.model.YouTubeVideoMetadata
import com.example.multipost.network.YouTubeUploadService
import com.example.multipost.repository.AccountRepository
import com.example.multipost.repository.VideoRepository

class YouTubePublisher(
    private val context: Context
) : SocialPublisher {

    private val accountRepository =
        AccountRepository(context)

    private val videoRepository =
        VideoRepository(context)

    private val uploadService =
        YouTubeUploadService(context)

    private val tokenProvider =
        YouTubeTokenProvider(context)

    override suspend fun publish(
        job: PublishJob,
        progressCallback: suspend (Int) -> Unit
    ): PublishResult {

        return try {

            progressCallback(0)

            val account =
                accountRepository
                    .getAccountByAccountId(
                        YouTubeTokenProvider.YOUTUBE_ACCOUNT_ID
                    )
                    ?: return PublishResult.Failure(
                        "YouTube account is not connected."
                    )

            var accessToken =
                tokenProvider.getStoredAccessToken()

            if (
                accessToken.isNullOrBlank()
            ) {

                return handleSilentAuthorization(
                    job = job,
                    progressCallback = progressCallback
                )
            }

            val videoId =
                job.videoId
                    ?: return PublishResult.Failure(
                        "Video ID is missing."
                    )

            val video =
                videoRepository
                    .getVideoById(videoId)
                    ?: return PublishResult.Failure(
                        "Video record was not found."
                    )

            val videoUri =
                Uri.parse(video.uri)

            val videoSize =
                video.fileSize

            if (videoSize <= 0L) {

                return PublishResult.Failure(
                    "Video file size is invalid."
                )
            }

            val mimeType =
                context.contentResolver
                    .getType(videoUri)
                    ?: "video/mp4"

            if (!mimeType.startsWith("video/")) {

                return PublishResult.Failure(
                    "Invalid video MIME type: $mimeType"
                )
            }

            val metadata =
                YouTubeVideoMetadata(
                    title =
                        job.youtubeTitle.ifBlank {
                            job.caption.take(100)
                        },
                    description =
                        job.caption,
                    tags =
                        parseHashtags(job.hashtags),
                    privacyStatus =
                        parsePrivacy(job.youtubePrivacy)
                )

            metadata.validate()

            progressCallback(5)

            try {

                val remotePostId =
                    uploadService.uploadVideo(
                        accessToken = accessToken,
                        videoUri = videoUri,
                        videoSize = videoSize,
                        mimeType = mimeType,
                        metadata = metadata
                    ) { progress ->

                        progressCallback(progress)
                    }

                return PublishResult.Success(
                    remotePostId = remotePostId
                )

            } catch (
                exception:
                YouTubeUploadService.YouTubeAuthenticationException
            ) {

                /*
                 * Stored access token failed with HTTP 401.
                 *
                 * Do not immediately ask the user to log in.
                 *
                 * First try Google's silent authorization.
                 */

                val silentResult =
                    tokenProvider.authorizeSilently()

                when (silentResult) {

                    is YouTubeTokenProvider
                    .SilentAuthorizationResult.Success -> {

                        accessToken =
                            silentResult.accessToken

                        /*
                         * Retry the same upload automatically
                         * using the newly obtained token.
                         */

                        val remotePostId =
                            uploadService.uploadVideo(
                                accessToken =
                                    accessToken,
                                videoUri =
                                    videoUri,
                                videoSize =
                                    videoSize,
                                mimeType =
                                    mimeType,
                                metadata =
                                    metadata
                            ) { progress ->

                                progressCallback(progress)
                            }

                        return PublishResult.Success(
                            remotePostId =
                                remotePostId
                        )
                    }

                    is YouTubeTokenProvider
                    .SilentAuthorizationResult
                    .ResolutionRequired -> {

                        return PublishResult.AuthenticationRequired(
                            "YouTube authorization requires user approval. Please reconnect your YouTube account."
                        )
                    }

                    is YouTubeTokenProvider
                    .SilentAuthorizationResult
                    .Failed -> {

                        return PublishResult.AuthenticationRequired(
                            silentResult.message
                        )
                    }
                }
            }

        } catch (
            exception:
            YouTubeUploadService.YouTubeAuthenticationException
        ) {

            exception.printStackTrace()

            PublishResult.AuthenticationRequired(
                exception.message
                    ?: "YouTube authorization has expired. Please reconnect your account."
            )

        } catch (
            exception: Exception
        ) {

            exception.printStackTrace()

            PublishResult.Failure(
                exception.message
                    ?: "YouTube upload failed."
            )

        } finally {

            accountRepository.close()
            videoRepository.close()
        }
    }

    private suspend fun handleSilentAuthorization(
        job: PublishJob,
        progressCallback: suspend (Int) -> Unit
    ): PublishResult {

        val silentResult =
            tokenProvider.authorizeSilently()

        return when (silentResult) {

            is YouTubeTokenProvider
            .SilentAuthorizationResult.Success -> {

                /*
                 * No stored token existed.
                 * Google silently supplied a new one.
                 */

                retryUploadWithToken(
                    job = job,
                    accessToken =
                        silentResult.accessToken,
                    progressCallback =
                        progressCallback
                )
            }

            is YouTubeTokenProvider
            .SilentAuthorizationResult
            .ResolutionRequired -> {

                PublishResult.AuthenticationRequired(
                    "YouTube authorization requires user approval. Please reconnect your YouTube account."
                )
            }

            is YouTubeTokenProvider
            .SilentAuthorizationResult
            .Failed -> {

                PublishResult.AuthenticationRequired(
                    silentResult.message
                )
            }
        }
    }

    private suspend fun retryUploadWithToken(
        job: PublishJob,
        accessToken: String,
        progressCallback: suspend (Int) -> Unit
    ): PublishResult {

        val videoId =
            job.videoId
                ?: return PublishResult.Failure(
                    "Video ID is missing."
                )

        val video =
            videoRepository
                .getVideoById(videoId)
                ?: return PublishResult.Failure(
                    "Video record was not found."
                )

        val videoUri =
            Uri.parse(video.uri)

        val videoSize =
            video.fileSize

        if (videoSize <= 0L) {

            return PublishResult.Failure(
                "Video file size is invalid."
            )
        }

        val mimeType =
            context.contentResolver
                .getType(videoUri)
                ?: "video/mp4"

        if (!mimeType.startsWith("video/")) {

            return PublishResult.Failure(
                "Invalid video MIME type: $mimeType"
            )
        }

        val metadata =
            YouTubeVideoMetadata(
                title =
                    job.youtubeTitle.ifBlank {
                        job.caption.take(100)
                    },
                description =
                    job.caption,
                tags =
                    parseHashtags(job.hashtags),
                privacyStatus =
                    parsePrivacy(job.youtubePrivacy)
            )

        metadata.validate()

        return try {

            val remotePostId =
                uploadService.uploadVideo(
                    accessToken =
                        accessToken,
                    videoUri =
                        videoUri,
                    videoSize =
                        videoSize,
                    mimeType =
                        mimeType,
                    metadata =
                        metadata
                ) { progress ->

                    progressCallback(progress)
                }

            PublishResult.Success(
                remotePostId =
                    remotePostId
            )

        } catch (
            exception:
            YouTubeUploadService.YouTubeAuthenticationException
        ) {

            PublishResult.AuthenticationRequired(
                "YouTube authorization is still invalid after silent renewal."
            )
        }
    }

    private fun parsePrivacy(
        privacy: String
    ): YouTubeVideoMetadata.PrivacyStatus {

        return when (
            privacy.lowercase()
        ) {

            "public" ->
                YouTubeVideoMetadata
                    .PrivacyStatus
                    .PUBLIC

            "unlisted" ->
                YouTubeVideoMetadata
                    .PrivacyStatus
                    .UNLISTED

            else ->
                YouTubeVideoMetadata
                    .PrivacyStatus
                    .PRIVATE
        }
    }

    private fun parseHashtags(
        hashtags: String
    ): List<String> {

        if (hashtags.isBlank()) {
            return emptyList()
        }

        return hashtags
            .split(
                Regex("[,\\s]+")
            )
            .map {
                it.trim()
                    .removePrefix("#")
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
    }
}
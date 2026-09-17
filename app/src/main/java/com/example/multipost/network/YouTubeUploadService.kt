package com.example.multipost.network

import android.content.Context
import android.net.Uri
import com.example.multipost.model.YouTubeVideoMetadata
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class YouTubeUploadService(
    private val context: Context
) {

    companion object {

        private const val UPLOAD_URL =
            "https://www.googleapis.com/upload/youtube/v3/videos"

        private const val UPLOAD_TYPE =
            "resumable"

        private const val CHUNK_SIZE =
            8 * 1024 * 1024

        private const val MAX_RETRIES =
            3
    }

    class YouTubeAuthenticationException(
        message: String
    ) : Exception(message)

    suspend fun uploadVideo(
        accessToken: String,
        videoUri: Uri,
        videoSize: Long,
        mimeType: String,
        metadata: YouTubeVideoMetadata,
        progressCallback: suspend (Int) -> Unit
    ): String {

        require(accessToken.isNotBlank()) {
            "YouTube access token is empty."
        }

        require(videoSize > 0L) {
            "Video file size is invalid."
        }

        require(mimeType.startsWith("video/")) {
            "Invalid video MIME type: $mimeType"
        }

        metadata.validate()

        val uploadUrl =
            createResumableUploadSession(
                accessToken = accessToken,
                videoSize = videoSize,
                mimeType = mimeType,
                metadata = metadata
            )

        val inputStream =
            context.contentResolver.openInputStream(
                videoUri
            ) ?: throw Exception(
                "Unable to open video file."
            )

        inputStream.use { input ->

            return uploadChunks(
                accessToken = accessToken,
                uploadUrl = uploadUrl,
                inputStream = input,
                videoSize = videoSize,
                mimeType = mimeType,
                progressCallback = progressCallback
            )
        }
    }

    private fun createResumableUploadSession(
        accessToken: String,
        videoSize: Long,
        mimeType: String,
        metadata: YouTubeVideoMetadata
    ): String {

        val url =
            URL(
                "$UPLOAD_URL?uploadType=$UPLOAD_TYPE&part=snippet,status"
            )

        val connection =
            url.openConnection() as HttpURLConnection

        try {

            connection.requestMethod =
                "POST"

            connection.doOutput =
                true

            connection.connectTimeout =
                30000

            connection.readTimeout =
                30000

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
            )

            connection.setRequestProperty(
                "X-Upload-Content-Length",
                videoSize.toString()
            )

            connection.setRequestProperty(
                "X-Upload-Content-Type",
                mimeType
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val body =
                JSONObject().apply {

                    put(
                        "snippet",
                        JSONObject().apply {

                            put(
                                "title",
                                metadata.title
                            )

                            put(
                                "description",
                                metadata.description
                            )

                            put(
                                "categoryId",
                                metadata.categoryId
                            )

                            put(
                                "tags",
                                org.json.JSONArray(
                                    metadata.tags
                                )
                            )
                        }
                    )

                    put(
                        "status",
                        JSONObject().apply {

                            put(
                                "privacyStatus",
                                metadata.privacyStatus
                                    .name
                                    .lowercase()
                            )
                        }
                    )
                }

            connection.outputStream.use { output ->

                output.write(
                    body.toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )
            }

            val responseCode =
                connection.responseCode

            if (responseCode == 401) {

                val response =
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        .orEmpty()

                throw YouTubeAuthenticationException(
                    "YouTube authorization has expired or is invalid. Please reconnect your YouTube account."
                )
            }

            if (
                responseCode !in 200..299
            ) {

                val response =
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        .orEmpty()

                throw Exception(
                    "YouTube upload session failed.\n" +
                            "HTTP $responseCode\n\n" +
                            response
                )
            }

            return connection
                .getHeaderField("Location")
                ?: throw Exception(
                    "YouTube upload session URL was not returned."
                )

        } finally {

            connection.disconnect()
        }
    }

    private suspend fun uploadChunks(
        accessToken: String,
        uploadUrl: String,
        inputStream: InputStream,
        videoSize: Long,
        mimeType: String,
        progressCallback: suspend (Int) -> Unit
    ): String {

        val buffer =
            ByteArray(CHUNK_SIZE)

        var uploadedBytes =
            0L

        var currentUploadUrl =
            uploadUrl

        while (
            uploadedBytes < videoSize
        ) {

            val remaining =
                videoSize - uploadedBytes

            val bytesToRead =
                minOf(
                    CHUNK_SIZE.toLong(),
                    remaining
                ).toInt()

            var totalRead =
                0

            while (
                totalRead < bytesToRead
            ) {

                val read =
                    inputStream.read(
                        buffer,
                        totalRead,
                        bytesToRead - totalRead
                    )

                if (read == -1) {
                    break
                }

                totalRead += read
            }

            if (totalRead <= 0) {
                break
            }

            val chunk =
                buffer.copyOf(
                    totalRead
                )

            var retryCount =
                0

            var chunkUploaded =
                false

            while (
                !chunkUploaded
            ) {

                try {

                    val response =
                        uploadChunk(
                            accessToken = accessToken,
                            uploadUrl = currentUploadUrl,
                            chunk = chunk,
                            startByte = uploadedBytes,
                            totalSize = videoSize,
                            mimeType = mimeType
                        )

                    when {

                        response.responseCode == 401 -> {

                            throw YouTubeAuthenticationException(
                                "YouTube authorization has expired or is invalid. Please reconnect your YouTube account."
                            )
                        }

                        response.responseCode == 200 ||
                                response.responseCode == 201 -> {

                            val videoId =
                                extractVideoId(
                                    response.body
                                )

                            progressCallback(
                                100
                            )

                            return videoId
                        }

                        response.responseCode == 308 -> {

                            val range =
                                response.range

                            val confirmedBytes =
                                parseUploadedBytes(
                                    range
                                )

                            uploadedBytes =
                                if (
                                    confirmedBytes >= 0
                                ) {
                                    confirmedBytes + 1
                                } else {
                                    uploadedBytes +
                                            totalRead
                                }

                            val progress =
                                (
                                        uploadedBytes * 100L /
                                                videoSize
                                        )
                                    .toInt()
                                    .coerceIn(
                                        0,
                                        99
                                    )

                            progressCallback(
                                progress
                            )

                            chunkUploaded =
                                true
                        }

                        response.responseCode in 500..599 -> {

                            retryCount++

                            if (
                                retryCount >
                                MAX_RETRIES
                            ) {

                                throw Exception(
                                    "YouTube upload failed after $MAX_RETRIES retries.\n" +
                                            "HTTP ${response.responseCode}"
                                )
                            }
                        }

                        else -> {

                            throw Exception(
                                "YouTube video upload failed.\n" +
                                        "HTTP ${response.responseCode}\n\n" +
                                        response.body
                            )
                        }
                    }

                } catch (
                    exception:
                    YouTubeAuthenticationException
                ) {

                    throw exception

                } catch (
                    exception:
                    Exception
                ) {

                    retryCount++

                    if (
                        retryCount >
                        MAX_RETRIES
                    ) {

                        throw exception
                    }
                }
            }
        }

        throw Exception(
            "YouTube upload ended before the video was completely uploaded."
        )
    }

    private fun uploadChunk(
        accessToken: String,
        uploadUrl: String,
        chunk: ByteArray,
        startByte: Long,
        totalSize: Long,
        mimeType: String
    ): UploadChunkResponse {

        val endByte =
            startByte +
                    chunk.size -
                    1

        val connection =
            URL(uploadUrl)
                .openConnection() as HttpURLConnection

        try {

            connection.requestMethod =
                "PUT"

            connection.doOutput =
                true

            connection.connectTimeout =
                30000

            connection.readTimeout =
                120000

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Content-Type",
                mimeType
            )

            connection.setRequestProperty(
                "Content-Length",
                chunk.size.toString()
            )

            connection.setRequestProperty(
                "Content-Range",
                "bytes $startByte-$endByte/$totalSize"
            )

            connection.outputStream.use { output ->

                output.write(chunk)
            }

            val responseCode =
                connection.responseCode

            val stream =
                if (
                    responseCode in 200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                    .orEmpty()

            return UploadChunkResponse(
                responseCode =
                    responseCode,
                body =
                    body,
                range =
                    connection.getHeaderField(
                        "Range"
                    )
            )

        } finally {

            connection.disconnect()
        }
    }

    private fun extractVideoId(
        responseBody: String
    ): String {

        if (
            responseBody.isBlank()
        ) {
            throw Exception(
                "YouTube upload completed but no response body was returned."
            )
        }

        val json =
            JSONObject(
                responseBody
            )

        val videoId =
            json.optString(
                "id",
                ""
            )

        if (
            videoId.isBlank()
        ) {
            throw Exception(
                "YouTube upload completed but video ID was not returned."
            )
        }

        return videoId
    }

    private fun parseUploadedBytes(
        range: String?
    ): Long {

        if (
            range.isNullOrBlank()
        ) {
            return -1L
        }

        val lastDash =
            range.lastIndexOf('-')

        if (
            lastDash == -1
        ) {
            return -1L
        }

        return range
            .substring(
                lastDash + 1
            )
            .toLongOrNull()
            ?: -1L
    }

    private data class UploadChunkResponse(
        val responseCode: Int,
        val body: String,
        val range: String?
    )
}
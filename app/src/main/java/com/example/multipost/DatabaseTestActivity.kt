package com.example.multipost

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.multipost.model.VideoModel
import com.example.multipost.repository.VideoRepository

class DatabaseTestActivity : AppCompatActivity() {

    private lateinit var resultText: TextView

    private lateinit var videoRepository: VideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultText = TextView(this)

        resultText.textSize = 18f

        resultText.setPadding(
            32,
            32,
            32,
            32
        )

        setContentView(resultText)

        videoRepository =
            VideoRepository(this)

        testDatabase()
    }

    @SuppressLint("SetTextI18n")
    private fun testDatabase() {

        try {

            // INSERT
            val videoId =
                videoRepository.insertVideo(
                    VideoModel(
                        uri = "content://test/video",
                        fileName = "test_video.mp4",
                        duration = 120000,
                        width = 1920,
                        height = 1080,
                        fileSize = 5242880
                    )
                )

            if (videoId <= 0) {
                resultText.text =
                    "INSERT failed."
                return
            }

            // SELECT
            val video =
                videoRepository.getVideoById(
                    videoId
                )

            if (video == null) {
                resultText.text =
                    "SELECT failed."
                return
            }

            resultText.text =
                """
                SQLite Database Working
                
                INSERT: SUCCESS
                SELECT: SUCCESS
                
                ID: ${video.id}
                File: ${video.fileName}
                Resolution: ${video.width} × ${video.height}
                Duration: ${video.duration} ms
                Size: ${video.fileSize} bytes
                URI: ${video.uri}
                """.trimIndent()

        } catch (exception: Exception) {

            exception.printStackTrace()

            resultText.text =
                """
                SQLite Database Test Failed
                
                ${exception.message}
                """.trimIndent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        videoRepository.close()
    }
}
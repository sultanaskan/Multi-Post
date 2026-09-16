package com.example.multipost.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.multipost.R
import com.example.multipost.VideoPreviewActivity
import com.example.multipost.ui.accounts.AccountsFragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    private var selectedVideoUri: Uri? = null

    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {

                selectedVideoUri = uri

                val fileName = getFileName(uri)

                updateSelectedVideo(fileName)

                openVideoPreview(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val selectVideoButton =
            view.findViewById<MaterialButton>(
                R.id.selectVideoButton
            )

        val selectVideoCard =
            view.findViewById<View>(
                R.id.selectVideoCard
            )

        val viewAccountsText =
            view.findViewById<View>(
                R.id.viewAccountsText
            )

        val settingsButton =
            view.findViewById<View>(
                R.id.settingsButton
            )

        selectVideoButton.setOnClickListener {

            openVideoPicker()
        }

        selectVideoCard.setOnClickListener {

            openVideoPicker()
        }

        viewAccountsText.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    AccountsFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        settingsButton.setOnClickListener {

            showMessage(
                "Settings will be added later"
            )
        }
    }

    private fun openVideoPicker() {

        videoPicker.launch(
            PickVisualMediaRequest(
                ActivityResultContracts
                    .PickVisualMedia
                    .VideoOnly
            )
        )
    }

    private fun openVideoPreview(
        uri: Uri
    ) {

        val intent =
            Intent(
                requireContext(),
                VideoPreviewActivity::class.java
            )

        intent.putExtra(
            VideoPreviewActivity.EXTRA_VIDEO_URI,
            uri.toString()
        )

        startActivity(intent)
    }

    private fun updateSelectedVideo(
        fileName: String
    ) {

        view?.findViewById<TextView>(
            R.id.selectedVideoName
        )?.apply {

            text = fileName

            visibility = View.VISIBLE
        }

        view?.findViewById<TextView>(
            R.id.selectVideoTitle
        )?.text = "Video Selected"
    }

    private fun getFileName(
        uri: Uri
    ): String {

        var fileName =
            "Selected Video"

        val cursor =
            requireContext()
                .contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )

        cursor?.use {

            if (it.moveToFirst()) {

                val index =
                    it.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (index >= 0) {

                    fileName =
                        it.getString(index)
                }
            }
        }

        return fileName
    }

    private fun showMessage(
        message: String
    ) {

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
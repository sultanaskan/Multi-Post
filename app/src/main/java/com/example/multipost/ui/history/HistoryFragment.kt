package com.example.multipost.ui.history

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multipost.R
import com.example.multipost.repository.PublishRepository
import com.example.multipost.upload.UploadManager

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private lateinit var publishRepository: PublishRepository
    private lateinit var uploadManager: UploadManager
    private lateinit var adapter: HistoryAdapter

    private val handler =
        Handler(Looper.getMainLooper())

    private val refreshRunnable =
        object : Runnable {

            override fun run() {

                if (
                    isAdded &&
                    !isDetached
                ) {

                    loadHistory()

                    handler.postDelayed(
                        this,
                        1000L
                    )
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_history,
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

        recyclerView =
            view.findViewById(
                R.id.historyRecyclerView
            )

        emptyText =
            view.findViewById(
                R.id.emptyHistoryText
            )

        publishRepository =
            PublishRepository(
                requireContext()
            )

        uploadManager =
            UploadManager(
                requireContext()
            )

        adapter =
            HistoryAdapter(
                emptyList(),
                uploadManager
            )

        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerView.adapter =
            adapter

        loadHistory()
    }

    override fun onResume() {

        super.onResume()

        if (
            ::publishRepository.isInitialized
        ) {

            startAutoRefresh()
        }
    }

    override fun onPause() {

        super.onPause()

        stopAutoRefresh()
    }

    private fun startAutoRefresh() {

        handler.removeCallbacks(
            refreshRunnable
        )

        handler.post(
            refreshRunnable
        )
    }

    private fun stopAutoRefresh() {

        handler.removeCallbacks(
            refreshRunnable
        )
    }

    private fun loadHistory() {

        Thread {

            try {

                val jobs =
                    publishRepository.getAllJobs()

                if (!isAdded) {
                    return@Thread
                }

                requireActivity().runOnUiThread {

                    if (
                        isAdded &&
                        !isDetached
                    ) {

                        adapter.updateData(
                            jobs
                        )

                        updateEmptyState(
                            jobs.isEmpty()
                        )
                    }
                }

            } catch (exception: Exception) {

                exception.printStackTrace()

                if (!isAdded) {
                    return@Thread
                }

                requireActivity().runOnUiThread {

                    if (
                        isAdded &&
                        !isDetached
                    ) {

                        updateEmptyState(
                            true
                        )
                    }
                }
            }

        }.start()
    }

    private fun updateEmptyState(
        isEmpty: Boolean
    ) {

        if (isEmpty) {

            recyclerView.visibility =
                View.GONE

            emptyText.visibility =
                View.VISIBLE

        } else {

            recyclerView.visibility =
                View.VISIBLE

            emptyText.visibility =
                View.GONE
        }
    }

    override fun onDestroyView() {

        stopAutoRefresh()

        super.onDestroyView()
    }
}
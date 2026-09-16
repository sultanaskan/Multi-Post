package com.example.multipost.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.multipost.R
import com.example.multipost.model.PublishJob
import com.example.multipost.upload.UploadManager
import com.google.android.material.button.MaterialButton

class HistoryAdapter(
    private var jobs: List<PublishJob>,
    private val uploadManager: UploadManager
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val platformText: TextView =
            itemView.findViewById(
                R.id.platformText
            )

        val statusText: TextView =
            itemView.findViewById(
                R.id.statusText
            )

        val progressText: TextView =
            itemView.findViewById(
                R.id.progressText
            )

        val captionText: TextView =
            itemView.findViewById(
                R.id.captionText
            )

        val actionButton: MaterialButton =
            itemView.findViewById(
                R.id.actionButton
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_history,
                parent,
                false
            )

        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        val job =
            jobs[position]

        holder.platformText.text =
            job.platform

        holder.statusText.text =
            job.status.replaceFirstChar {
                it.uppercase()
            }

        holder.progressText.text =
            "${job.progress}%"

        holder.captionText.text =
            if (job.caption.isBlank()) {
                "No caption"
            } else {
                job.caption
            }

        when (job.status.lowercase()) {

            "pending",
            "uploading" -> {

                holder.actionButton.visibility =
                    View.VISIBLE

                holder.actionButton.text =
                    "Cancel"

                holder.actionButton.setOnClickListener {

                    uploadManager.cancelUpload(
                        job.id
                    )
                }
            }

            "failed",
            "cancelled" -> {

                holder.actionButton.visibility =
                    View.VISIBLE

                holder.actionButton.text =
                    "Retry"

                holder.actionButton.setOnClickListener {

                    uploadManager.retryUpload(
                        jobId = job.id,
                        platform = job.platform
                    )
                }
            }

            "completed" -> {

                holder.actionButton.visibility =
                    View.GONE

                holder.actionButton.setOnClickListener(
                    null
                )
            }

            else -> {

                holder.actionButton.visibility =
                    View.GONE

                holder.actionButton.setOnClickListener(
                    null
                )
            }
        }
    }

    override fun getItemCount(): Int =
        jobs.size

    fun updateData(
        newJobs: List<PublishJob>
    ) {

        jobs = newJobs

        notifyDataSetChanged()
    }
}
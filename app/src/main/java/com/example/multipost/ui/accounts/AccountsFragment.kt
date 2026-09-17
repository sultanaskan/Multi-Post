package com.example.multipost.ui.accounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.multipost.R
import com.example.multipost.auth.YouTubeAuthActivity
import com.example.multipost.network.YouTubeApiService
import com.example.multipost.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountsFragment : Fragment() {

    private lateinit var accountRepository: AccountRepository

    private lateinit var youtubeStatusText: TextView
    private lateinit var youtubeChannelText: TextView
    private lateinit var youtubeButton: Button

    private val fragmentScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view =
            inflater.inflate(
                R.layout.fragment_accounts,
                container,
                false
            )

        accountRepository =
            AccountRepository(requireContext())

        youtubeStatusText =
            view.findViewById(
                R.id.textYouTubeStatus
            )

        youtubeChannelText =
            view.findViewById(
                R.id.textYouTubeChannel
            )

        youtubeButton =
            view.findViewById(
                R.id.buttonConnectYouTube
            )

        updateYouTubeAccountState()

        return view
    }

    override fun onResume() {

        super.onResume()

        if (::accountRepository.isInitialized) {
            updateYouTubeAccountState()
        }
    }

    private fun updateYouTubeAccountState() {

        val account =
            accountRepository.getAccountByAccountId(
                "youtube_default"
            )

        if (
            account != null &&
            !account.accessToken.isNullOrBlank()
        ) {

            youtubeStatusText.text =
                "✓ YouTube account connected"

            youtubeButton.text =
                "Disconnect YouTube"

            youtubeButton.setOnClickListener {

                disconnectYouTube()
            }

            loadYouTubeChannel()

        } else {

            youtubeStatusText.text =
                "Connect your YouTube account to publish videos."

            youtubeChannelText.visibility =
                View.GONE

            youtubeButton.text =
                "Connect YouTube"

            youtubeButton.setOnClickListener {

                val intent =
                    Intent(
                        requireContext(),
                        YouTubeAuthActivity::class.java
                    )

                startActivity(intent)
            }
        }
    }

    private fun loadYouTubeChannel() {

        youtubeChannelText.visibility =
            View.VISIBLE

        youtubeChannelText.text =
            "Loading channel information..."

        fragmentScope.launch {

            try {

                val account =
                    accountRepository
                        .getAccountByAccountId(
                            "youtube_default"
                        )

                if (account == null) {

                    youtubeChannelText.visibility =
                        View.GONE

                    return@launch
                }

                val accessToken =
                    accountRepository
                        .getAccessToken(
                            account.accountId
                        )

                if (accessToken.isNullOrBlank()) {

                    youtubeChannelText.text =
                        "YouTube access token was not found."

                    return@launch
                }

                val channelInfo =
                    withContext(
                        Dispatchers.IO
                    ) {

                        YouTubeApiService(
                            requireContext()
                        ).getMyChannelInfo(
                            accessToken
                        )
                    }

                youtubeChannelText.text =
                    """
                    Channel: ${channelInfo.channelTitle}

                    Channel ID: ${channelInfo.channelId}

                    Subscribers: ${channelInfo.subscriberCount}

                    Videos: ${channelInfo.videoCount}
                    """.trimIndent()

            } catch (exception: Exception) {

                exception.printStackTrace()

                youtubeChannelText.text =
                    """
                    Unable to load YouTube channel information.

                    ${exception.message}
                    """.trimIndent()
            }
        }
    }

    private fun disconnectYouTube() {

        accountRepository.deleteAccountTokens(
            "youtube_default"
        )

        val account =
            accountRepository.getAccountByAccountId(
                "youtube_default"
            )

        if (account != null) {

            accountRepository.deleteAccount(
                account.id
            )
        }

        updateYouTubeAccountState()
    }

    override fun onDestroyView() {

        fragmentScope.cancel()

        if (::accountRepository.isInitialized) {
            accountRepository.close()
        }

        super.onDestroyView()
    }
}
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
import com.example.multipost.repository.AccountRepository

class AccountsFragment : Fragment() {

    private lateinit var accountRepository: AccountRepository

    private lateinit var youtubeStatusText: TextView
    private lateinit var youtubeButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
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

        youtubeButton =
            view.findViewById(
                R.id.buttonConnectYouTube
            )

        youtubeButton.setOnClickListener {

            val intent = Intent(
                requireContext(),
                YouTubeAuthActivity::class.java
            )

            startActivity(intent)
        }

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

        if (account != null &&
            !account.accessToken.isNullOrBlank()
        ) {

            youtubeStatusText.text =
                "✓ YouTube account connected"

            youtubeButton.text =
                "Disconnect YouTube"

            youtubeButton.setOnClickListener {

                disconnectYouTube()
            }

        } else {

            youtubeStatusText.text =
                "Connect your YouTube account to publish videos."

            youtubeButton.text =
                "Connect YouTube"

            youtubeButton.setOnClickListener {

                val intent = Intent(
                    requireContext(),
                    YouTubeAuthActivity::class.java
                )

                startActivity(intent)
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

        accountRepository.close()

        super.onDestroyView()
    }
}
package com.example.multipost

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.multipost.ui.accounts.AccountsFragment
import com.example.multipost.ui.history.HistoryFragment
import com.example.multipost.ui.home.HomeFragment
import com.example.multipost.ui.upload.UploadFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // Notification permission result.
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        bottomNavigation =
            findViewById(
                R.id.bottomNavigation
            )

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.navigation_home -> {
                    openFragment(
                        HomeFragment()
                    )
                    true
                }

                R.id.navigation_upload -> {
                    openFragment(
                        UploadFragment()
                    )
                    true
                }

                R.id.navigation_accounts -> {
                    openFragment(
                        AccountsFragment()
                    )
                    true
                }

                R.id.navigation_history -> {
                    openFragment(
                        HistoryFragment()
                    )
                    true
                }

                else -> false
            }
        }

        if (
            savedInstanceState == null
        ) {

            if (
                shouldOpenHistory(intent)
            ) {

                openHistoryFromNotification()

            } else {

                bottomNavigation.selectedItemId =
                    R.id.navigation_home
            }
        }

        requestNotificationPermission()
    }

    override fun onNewIntent(
        intent: Intent
    ) {

        super.onNewIntent(intent)

        setIntent(intent)

        if (
            shouldOpenHistory(intent)
        ) {

            openHistoryFromNotification()
        }
    }

    private fun openHistoryFromNotification() {

        if (
            bottomNavigation.selectedItemId ==
            R.id.navigation_history
        ) {

            openFragment(
                HistoryFragment()
            )

        } else {

            bottomNavigation.selectedItemId =
                R.id.navigation_history
        }
    }

    private fun shouldOpenHistory(
        intent: Intent?
    ): Boolean {

        return intent?.getBooleanExtra(
            "open_history",
            false
        ) == true
    }

    private fun requestNotificationPermission() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permission =
                Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                notificationPermissionLauncher
                    .launch(permission)
            }
        }
    }

    private fun openFragment(
        fragment: Fragment
    ) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }
}
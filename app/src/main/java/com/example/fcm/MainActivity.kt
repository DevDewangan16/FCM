package com.example.fcm

import ChatScreen
import ErrorScreen
import LoadingScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fcm.ui.theme.FCMTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            FCMTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        viewModel.state.isLoading -> LoadingScreen()
                        viewModel.state.errorMessage != null -> ErrorScreen(
                            message = viewModel.state.errorMessage!!,
                            onRetry = { viewModel.initializeFcm() }
                        )
                        viewModel.state.registrationSuccess -> ChatScreen(
                            messageText = viewModel.state.messageText,
                            onMessageChange = viewModel::onMessageChange,
                            onMessageSend = { viewModel.sendMessage(false) },
                            onMessageBroadcast = { viewModel.sendMessage(true) }
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}
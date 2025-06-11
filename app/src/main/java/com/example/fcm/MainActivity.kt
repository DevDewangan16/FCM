package com.example.fcm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationPermission()

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        viewModel.initializeFcm()

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            val loadingLayout = findViewById<LinearLayout>(R.id.loadingLayout)
            val errorLayout = findViewById<LinearLayout>(R.id.errorLayout)
            val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)

            when {
                state.isLoading -> {
                    loadingLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                    chatLayout.visibility = View.GONE
                }
                state.errorMessage != null -> {
                    loadingLayout.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    chatLayout.visibility = View.GONE

                    findViewById<TextView>(R.id.errorText).text = state.errorMessage
                    findViewById<Button>(R.id.retryButton).setOnClickListener {
                        viewModel.initializeFcm()
                    }
                }
                state.registrationSuccess -> {
                    loadingLayout.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                    chatLayout.visibility = View.VISIBLE

                    val messageInput = findViewById<EditText>(R.id.messageInput)
                    val sendButton = findViewById<ImageButton>(R.id.sendButton)
                    val broadcastButton = findViewById<ImageButton>(R.id.broadcastButton)

                    messageInput.setText(state.messageText)

                    sendButton.setOnClickListener {
                        viewModel.sendMessage(false)
                    }

                    broadcastButton.setOnClickListener {
                        viewModel.sendMessage(true)
                    }

                    messageInput.addTextChangedListener {
                        viewModel.onMessageChange(it.toString())
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
package com.example.fcm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
            val loadingLayout = findViewById<View>(R.id.loadingLayout)
            val errorLayout = findViewById<View>(R.id.errorLayout)
            val successLayout = findViewById<View>(R.id.successLayout)

            when {
                state.isLoading -> {
                    loadingLayout.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                    successLayout.visibility = View.GONE
                }
                state.errorMessage != null -> {
                    loadingLayout.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    successLayout.visibility = View.GONE

                    findViewById<TextView>(R.id.errorText).text = state.errorMessage
                    findViewById<Button>(R.id.retryButton).setOnClickListener {
                        viewModel.retry()
                    }
                }
                state.isSuccess -> {
                    loadingLayout.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                    successLayout.visibility = View.VISIBLE

                    findViewById<TextView>(R.id.deviceIdText).text =
                        "Device ID: ${state.customerId}"
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
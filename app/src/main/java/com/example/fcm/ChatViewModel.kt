package com.example.fcm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.UUID

class ChatViewModel : ViewModel() {

    var state by mutableStateOf(ChatState())
        private set

    private val api: FcmApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Replace with your actual backend URL
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    init {
        initializeFcm()
    }

    fun initializeFcm() {
        state = state.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Generate customer ID if not already set
                val customerId = state.customerId.ifEmpty { UUID.randomUUID().toString() }

                // Get FCM token
                val fcmToken = Firebase.messaging.token.await()

                // Register with backend
                val response = api.registerToken(RegisterTokenDto(
                    customer_id = customerId,
                    fcm_token = fcmToken
                ))

                // Subscribe to topic
                Firebase.messaging.subscribeToTopic("all_customers").await()

                // Update state
                state = state.copy(
                    isLoading = false,
                    customerId = customerId,
                    registrationSuccess = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to initialize messaging: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onMessageChange(message: String) {
        state = state.copy(messageText = message)
    }

    fun sendMessage(isBroadcast: Boolean) {
        viewModelScope.launch {
            try {
                val messageDto = SendMessageDto(
                    to = if (isBroadcast) null else state.customerId,
                    notification = NotificationBody(
                        title = "New Message",
                        body = state.messageText
                    )
                )

                if (isBroadcast) {
                    api.broadcast(messageDto)
                } else {
                    api.sendMessage(messageDto)
                }

                state = state.copy(messageText = "")
            } catch (e: Exception) {
                state = state.copy(errorMessage = "Failed to send message: ${e.localizedMessage}")
            }
        }
    }
}
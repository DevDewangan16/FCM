package com.example.fcm

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
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
import java.io.IOException

class ChatViewModel: ViewModel() {

    var state by mutableStateOf(ChatState())
        private set

    // Update this URL to your CodeIgniter backend URL
    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/") // Replace with your server URL
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    init {
        viewModelScope.launch {
            Firebase.messaging.subscribeToTopic("chat").await()
        }
    }

    fun onRemoteTokenChange(newToken: String) {
        state = state.copy(
            remoteToken = newToken
        )
    }

    fun onSubmitRemoteToken() {
        state = state.copy(
            isEnteringToken = false
        )
        // Register token with backend
        registerTokenWithBackend(state.remoteToken)
    }

    fun onMessageChange(message: String) {
        state = state.copy(
            messageText = message
        )
    }

    private fun registerTokenWithBackend(customerId: String) {
        viewModelScope.launch {
            try {
                val currentToken = Firebase.messaging.token.await()
                val tokenDto = RegisterTokenDto(
                    customer_id = customerId,
                    fcm_token = currentToken
                )
                api.registerToken(tokenDto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun sendMessage(isBroadcast: Boolean) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = if (isBroadcast) null else state.remoteToken,
                notification = NotificationBody(
                    title = "New Message",
                    body = state.messageText
                )
            )

            try {
                if (isBroadcast) {
                    api.broadcast(messageDto)
                } else {
                    api.sendMessage(messageDto)
                }

                state = state.copy(
                    messageText = ""
                )
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

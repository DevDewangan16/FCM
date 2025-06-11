package com.example.fcm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _state = MutableLiveData(ChatState())
    val state: LiveData<ChatState> = _state

    private val api: FcmApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    fun initializeFcm() {
        _state.value = _state.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val customerId = _state.value?.customerId?.ifEmpty { UUID.randomUUID().toString() } ?: UUID.randomUUID().toString()
                val fcmToken = Firebase.messaging.token.await()

                api.registerToken(RegisterTokenDto(
                    customer_id = customerId,
                    fcm_token = fcmToken
                ))

                Firebase.messaging.subscribeToTopic("all_customers").await()

                _state.value = _state.value?.copy(
                    isLoading = false,
                    customerId = customerId,
                    registrationSuccess = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value?.copy(
                    isLoading = false,
                    errorMessage = "Failed to initialize messaging: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onMessageChange(message: String) {
        _state.value = _state.value?.copy(messageText = message)
    }

    fun sendMessage(isBroadcast: Boolean) {
        viewModelScope.launch {
            try {
                val messageDto = SendMessageDto(
                    to = if (isBroadcast) null else _state.value?.customerId,
                    notification = NotificationBody(
                        title = "New Message",
                        body = _state.value?.messageText ?: ""
                    )
                )

                if (isBroadcast) {
                    api.broadcast(messageDto)
                } else {
                    api.sendMessage(messageDto)
                }

                _state.value = _state.value?.copy(messageText = "")
            } catch (e: Exception) {
                _state.value = _state.value?.copy(errorMessage = "Failed to send message: ${e.localizedMessage}")
            }
        }
    }
}
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
    private val _state = MutableLiveData(FcmState())
    val state: LiveData<FcmState> = _state

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
                val customerId = UUID.randomUUID().toString()
                val fcmToken = Firebase.messaging.token.await()

                api.registerToken(RegisterTokenDto(
                    customer_id = customerId,
                    fcm_token = fcmToken
                ))

                _state.value = _state.value?.copy(
                    isLoading = false,
                    isSuccess = true,
                    customerId = customerId
                )
            } catch (e: Exception) {
                _state.value = _state.value?.copy(
                    isLoading = false,
                    errorMessage = "Failed to register FCM: ${e.localizedMessage}"
                )
            }
        }
    }

    fun retry() {
        initializeFcm()
    }
}
package com.example.fcm

import android.util.Log
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

    companion object {
        private const val TAG = "FCMToken"
    }

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
                Log.d(TAG, "Attempting to get FCM token...")
                val customerId = UUID.randomUUID().toString()
                val fcmToken = Firebase.messaging.token.await()

                Log.d(TAG, "Successfully retrieved FCM token: $fcmToken")
                Log.d(TAG, "Customer ID: $customerId")

                try {
                    Log.d(TAG, "Attempting to register token with backend...")
                    api.registerToken(RegisterTokenDto(
                        customer_id = customerId,
                        fcm_token = fcmToken
                    ))
                    Log.d(TAG, "Successfully registered token with backend")

                    _state.value = _state.value?.copy(
                        isLoading = false,
                        isSuccess = true,
                        customerId = customerId
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error registering token with backend", e)
                    _state.value = _state.value?.copy(
                        isLoading = false,
                        errorMessage = "Backend registration failed: ${e.localizedMessage}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FCM token", e)
                Log.e(TAG, "Error details: ${e.message}")
                Log.e(TAG, "Stack trace: ${Log.getStackTraceString(e)}")

                _state.value = _state.value?.copy(
                    isLoading = false,
                    errorMessage = "Failed to get FCM token: ${e.localizedMessage}"
                )
            }
        }
    }

    fun retry() {
        initializeFcm()
    }
}
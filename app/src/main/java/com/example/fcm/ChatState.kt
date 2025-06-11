package com.example.fcm

data class ChatState(
    val isLoading: Boolean = true,
    val registrationSuccess: Boolean = false,
    val customerId: String = "",
    val messageText: String = "",
    val errorMessage: String? = null
)
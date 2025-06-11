package com.example.fcm

data class ChatState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false,
    val customerId: String = "",
    val messageText: String = ""
)
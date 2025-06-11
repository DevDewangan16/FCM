package com.example.fcm

data class FcmState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val customerId: String = ""
)

data class RegisterTokenDto(
    val customer_id: String,
    val fcm_token: String
)
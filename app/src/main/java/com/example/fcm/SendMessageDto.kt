package com.example.fcm

data class SendMessageDto(
    val to:String?,
    val notification: NotificationBody
)

data class NotificationBody(
    val title:String,
    val body:String
)

data class RegisterTokenDto(
    val customer_id: String,
    val fcm_token: String
)
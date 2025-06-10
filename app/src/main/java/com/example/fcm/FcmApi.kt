// Updated FcmApi.kt
package com.example.fcm

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {

    @POST("/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto
    )

    @POST("/broadcast")
    suspend fun broadcast(
        @Body body: SendMessageDto
    )

    @POST("index.php/fcm/register")
    suspend fun registerToken(
        @Body body: RegisterTokenDto
    )
}

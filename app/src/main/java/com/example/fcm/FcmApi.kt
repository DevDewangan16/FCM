package com.example.fcm

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("index.php/fcm/register")
    suspend fun registerToken(@Body body: RegisterTokenDto)
}
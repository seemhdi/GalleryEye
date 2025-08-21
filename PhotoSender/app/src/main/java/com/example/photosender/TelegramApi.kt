package com.example.photosender

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface TelegramApi {
    @Multipart
    @POST("bot{token}/sendPhoto")
    suspend fun sendPhoto(
        @Path("token") token: String,
        @Part("chat_id") chatId: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response<Unit> // We don't need to parse the response body, just check success.
}

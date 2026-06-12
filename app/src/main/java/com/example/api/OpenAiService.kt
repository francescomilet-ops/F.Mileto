package com.example.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class OpenAiImageRequest(
    val prompt: String,
    val model: String = "dall-e-3",
    val n: Int = 1,
    val size: String = "1024x1024"
)

@JsonClass(generateAdapter = true)
data class OpenAiImageResponse(
    val created: Long,
    val data: List<OpenAiImageUrl>
)

@JsonClass(generateAdapter = true)
data class OpenAiImageUrl(
    val url: String
)

interface OpenAiService {
    @POST("v1/images/generations")
    suspend fun generateImage(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiImageRequest
    ): OpenAiImageResponse
}

package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.OpenAiImageRequest
import com.example.api.OpenAiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ShirinImageGenViewModel(application: Application) : AndroidViewModel(application) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val openAiService = retrofit.create(OpenAiService::class.java)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl: StateFlow<String?> = _generatedImageUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun generateImage(prompt: String, style: String, resolution: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _generatedImageUrl.value = null

            val prefs = getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
            val apiKey = prefs.getString("custom_openai_key", "") ?: ""
            if (apiKey.isBlank() || apiKey.contains("MY_OPENAI_API")) {
                _errorMessage.value = "Bitte füge deinen OPENAI_API_KEY in der App hinzu, um Bilder mit Dall-E 3 zu generieren."
                _isLoading.value = false
                return@launch
            }

            try {
                // Enhance the prompt with the style
                val finalPrompt = "Create a $style style image: $prompt"
                
                val req = OpenAiImageRequest(
                    prompt = finalPrompt,
                    model = "dall-e-3",
                    n = 1,
                    size = resolution // e.g. "1024x1024"
                )

                val response = openAiService.generateImage("Bearer $apiKey", req)
                val url = response.data.firstOrNull()?.url
                if (url != null) {
                    _generatedImageUrl.value = url
                } else {
                    _errorMessage.value = "Es wurde kein Bild von der API zurückgegeben."
                }
            } catch (e: Exception) {
                Log.e("ShirinImageGenViewModel", "Error generating image", e)
                _errorMessage.value = "Fehler bei der Bildgenerierung: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

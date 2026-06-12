package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context, private val onInitSuccess: () -> Unit = {}) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.GERMANY
                onInitSuccess()
            } else {
                Log.e("TtsHelper", "Initialization failed")
            }
        }
    }

    fun speak(text: String, locale: Locale = Locale.GERMANY) {
        if (!isInitialized) return
        try {
            tts?.language = locale
            tts?.setPitch(1.1f) // Etwas höhere Tonlage für eine freundlichere Stimme
            tts?.setSpeechRate(0.95f) // Etwas langsamer für einen ruhigen, menschlicheren Klang
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "shirin_speech_id")
        } catch (e: Exception) {
            Log.e("TtsHelper", "Error in speaking: ${e.message}")
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        if (isInitialized) {
            tts?.shutdown()
        }
    }
}

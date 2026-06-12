package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceActivationHelper(
    private val context: Context,
    private val onKeywordDetected: () -> Unit,
    private val onLogMessage: (String) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isListening = false
    private var isContinuous = false
    private val handler = Handler(Looper.getMainLooper())

    init {
        handler.post {
            setupRecognizer()
        }
    }

    private fun setupRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("VoiceActivation", "Speech recognition not available on this device")
            onLogMessage("Spracherkennung auf diesem Gerät nicht verfügbar")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onLogMessage("Höre kontinuierlich auf 'Hey Shirin'...")
                }

                override fun onBeginningOfSpeech() {
                    onLogMessage("Audio wird analysiert...")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio-Aufnahmefehler"
                        SpeechRecognizer.ERROR_CLIENT -> "Schnittstellen-Fehler"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon-Berechtigung fehlt"
                        SpeechRecognizer.ERROR_NETWORK -> "Netzwerkverbindung erforderlich"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Netzwerk-Timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Keine Spracheingabe erkannt"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Spracherkennung beschäftigt"
                        SpeechRecognizer.ERROR_SERVER -> "Server-Rückmeldung Fehler"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                        else -> "Fehlercode ($error)"
                    }
                    Log.d("VoiceActivation", "Speech Error: $message")
                    
                    // Continuous restart sequence
                    if (isListening && isContinuous) {
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS || 
                            error == SpeechRecognizer.ERROR_CLIENT ||
                            error == SpeechRecognizer.ERROR_AUDIO) {
                            Log.e("VoiceActivation", "Fatal error ($error), stopping continuous listening.")
                            isListening = false
                            isContinuous = false
                            return
                        }
                        handler.postDelayed({ restartListeningInternal() }, 1000)
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null) {
                        checkForKeyword(matches)
                    }
                    if (isListening && isContinuous) {
                        handler.postDelayed({ restartListeningInternal() }, 600)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null) {
                        checkForKeyword(matches)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (e: Exception) {
            Log.e("VoiceActivation", "Error initializing speech recognizer: ${e.message}", e)
            onLogMessage("Fehler beim Starten der Erfassung: ${e.message}")
        }
    }

    private fun checkForKeyword(matches: List<String>) {
        for (match in matches) {
            val lowercase = match.lowercase()
            Log.d("VoiceActivation", "Candidate text: $lowercase")
            if (lowercase.contains("shirin") || lowercase.contains("schirin") || 
                lowercase.contains("chirin") || lowercase.contains("sheerin") ||
                lowercase.contains("shirley") || lowercase.contains("sirene")) {
                
                Log.d("VoiceActivation", "🔥 MATCH FOUND FOR KEYWORD: $lowercase")
                isListening = false
                speechRecognizer?.stopListening()
                
                handler.post {
                    onKeywordDetected()
                }
                break
            }
        }
    }

    fun startListening(continuous: Boolean = true) {
        if (isListening) return
        isContinuous = continuous
        isListening = true
        handler.post {
            try {
                if (speechRecognizer == null) {
                    setupRecognizer()
                }
                speechRecognizer?.startListening(recognizerIntent)
                Log.d("VoiceActivation", "Listening started.")
            } catch (e: Exception) {
                Log.e("VoiceActivation", "Failed startListening: ${e.message}")
            }
        }
    }

    fun stopListening() {
        isListening = false
        isContinuous = false
        handler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                Log.d("VoiceActivation", "Listening stopped.")
            } catch (e: Exception) {
                Log.e("VoiceActivation", "Failed stopListening: ${e.message}")
            }
        }
    }

    private fun restartListeningInternal() {
        if (!isListening) return
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.startListening(recognizerIntent)
        } catch (e: Exception) {
            Log.e("VoiceActivation", "Failed restartListeningInternal: ${e.message}")
        }
    }

    fun destroy() {
        stopListening()
        handler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e("VoiceActivation", "Failed destroy: ${e.message}")
            }
        }
    }
}

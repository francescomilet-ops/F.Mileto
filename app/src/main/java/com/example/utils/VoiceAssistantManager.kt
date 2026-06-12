package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceAssistantManager(private val context: Context, private val onCommandRecognized: (String) -> Unit) {

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull() ?: ""
                        
                        Log.d("VoiceAssistant", "Gehört: \$spokenText")
                        processGlobalCommand(spokenText)
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
    }

    // Die KI-Logik: Versteht, was der User will, egal wie er es sagt
    private fun processGlobalCommand(command: String) {
        val lowerCommand = command.lowercase()
        
        when {
            lowerCommand.contains("shirin") -> {
                // Wake-Word erkannt! Jetzt den eigentlichen Befehl filtern
                handleNavigationAndSearch(lowerCommand)
            }
            else -> onCommandRecognized("Suche gestartet für: \$command")
        }
    }

    private fun handleNavigationAndSearch(command: String) {
        when {
            command.contains("shop") || command.contains("kleidung") || command.contains("jogginganzug") -> {
                onCommandRecognized("NAVIGATE_TO_SHOP")
            }
            command.contains("handwerker") || command.contains("pfa") || command.contains("strom") -> {
                onCommandRecognized("NAVIGATE_TO_CRAFTSMAN")
            }
            command.contains("auto") || command.contains("audi") || command.contains("amg") -> {
                onCommandRecognized("NAVIGATE_TO_AUTOMOTIVE")
            }
            command.contains("chat") -> {
                onCommandRecognized("NAVIGATE_TO_CHAT")
            }
            else -> {
                // Universelle KI-Suche über alle Portale
                onCommandRecognized("GLOBAL_SEARCH:\$command")
            }
        }
    }
}

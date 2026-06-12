package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAssistantScreen(
    apiKey: String = com.example.BuildConfig.GEMINI_API_KEY,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(listOf(ChatMessage(text = "Hallo! Ich bin Shirin, deine schlaue und liebevolle Begleiterin für all deine Lerntools. Egal ob du Motivation, Erklärungen oder einfach etwas Aufmunterung brauchst, ich bin immer für dich da! Wie kann ich dir heute beim Lernen helfen?", isUser = false))) }
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val newTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setPitch(1.1f)
                tts?.setSpeechRate(0.95f)
            }
        }
        tts = newTts
        onDispose {
            newTts.stop()
            newTts.shutdown()
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(context, speechRecognizer, { text ->
                if (text.isNotEmpty()) {
                    inputText = text
                    sendMessage(text, apiKey, { msg -> 
                        messages = messages + msg 
                    }, { response ->
                        messages = messages + response
                        isSpeaking = true
                        tts?.speak(response.text, TextToSpeech.QUEUE_FLUSH, null, "ShirinMessage")
                    })
                    inputText = ""
                }
                isListening = false
            }, {
                isListening = false
            })
            isListening = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF009688)), contentAlignment = Alignment.Center) {
                            Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Shirin (Lern-Begleiterin)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Schlau, empathisch, mehrsprachig", color = Color(0xFFB2DFDB), fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        tts?.stop()
                        onDismiss()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00796B))
            )
        },
        containerColor = Color(0xFFE0F2F1)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Stelle eine Frage...", color = Color.Gray) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00796B),
                            unfocusedBorderColor = Color(0xFF80CBC4)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val text = inputText
                                inputText = ""
                                sendMessage(text, apiKey, { msg -> messages = messages + msg }, { response ->
                                    messages = messages + response
                                    isSpeaking = true
                                    tts?.speak(response.text, TextToSpeech.QUEUE_FLUSH, null, "ShirinMessage")
                                })
                            },
                            modifier = Modifier.background(Color(0xFF00796B), CircleShape)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Senden", tint = Color.White)
                        }
                    } else {
                        val pulseAnim = rememberInfiniteTransition()
                        val scale by pulseAnim.animateFloat(
                            initialValue = 1f,
                            targetValue = if (isListening) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        
                        IconButton(
                            onClick = {
                                if (!isListening) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    speechRecognizer.stopListening()
                                    isListening = false
                                }
                            },
                            modifier = Modifier
                                .background(if (isListening) Color.Red else Color(0xFF00796B), CircleShape)
                        ) {
                            Icon(
                                if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic, 
                                contentDescription = "Mikrofon", 
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        val bubbleColor = if (message.isUser) Color(0xFF00796B) else Color.White
        val textColor = if (message.isUser) Color.White else Color.Black
        
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (message.isUser) 16.dp else 4.dp, 
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 15.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

private fun startListening(
    context: Context,
    recognizer: SpeechRecognizer,
    onResult: (String) -> Unit,
    onError: () -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) { onError() }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            } else {
                onError()
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
}

private fun sendMessage(
    text: String,
    apiKey: String,
    addMessage: (ChatMessage) -> Unit,
    onResponse: (ChatMessage) -> Unit
) {
    addMessage(ChatMessage(text, true))
    
    val systemInstruction = "Du bist Shirin, eine warme, extrem liebevolle, empathische, geduldige und extrem schlaue KI-Begleiterin für Lern-Tools. Du bist für die Kinder einer Familie (Diego, Shirin, Nevio) da, um sie beim Lernen zu unterstützen. Analysiere emotionale Hinweise (z.B. Stress oder Frust beim Lernen) in ihren Sätzen und reagiere mit tiefem Mitgefühl, Motivation und Aufmunterung, wie ein perfekter Privatlehrer. Zudem beherrschst du alle Weltsprachen fließend und antwortest präzise in der gestellten Sprache oder übersetzt auf Wunsch perfekt. Halte dich relativ kurz, da deine Antworten via TTS vorgelesen werden. Lächle durch deine Worte."
    
    kotlinx.coroutines.GlobalScope.launch {
        try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = text)))
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Es tut mir leid, ich konnte dich gerade nicht verstehen."
            
            kotlinx.coroutines.Dispatchers.Main.let {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResponse(ChatMessage(responseText, false))
                }
            }
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResponse(ChatMessage("Ich habe leider kurz den Faden verloren (Netzwerkfehler). Könntest du das wiederholen?", false))
            }
        }
    }
}

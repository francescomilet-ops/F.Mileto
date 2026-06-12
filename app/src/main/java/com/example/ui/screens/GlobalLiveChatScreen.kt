package com.example.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ShirinViewModel
import com.example.utils.VoiceAssistantManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

// Angepasste Version, um die bestehende Architektur zu nutzen
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GlobalLiveChatScreen(
    viewModel: ShirinViewModel,
    onClose: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val voiceAssistant = remember {
        VoiceAssistantManager(context) { command ->
            // On voice command recognized
            viewModel.sendChatMessage(command)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceAssistant.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Live Chat") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Text("X")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Chat-Verlauf
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val sender = if (msg.isUser) "Me" else "AI"
                            Text(text = "$sender: ${msg.text}", style = MaterialTheme.typography.bodyLarge)
                            msg.translatedText?.let {
                                Text(text = "🌍 KI-Übersetzung: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            // Eingabeleiste mit Mikrofon
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nachricht schreiben...") }
                )
                
                // Mikrofon-Button für Sprache-zu-Text
                IconButton(onClick = { 
                    if (audioPermissionState.status.isGranted) {
                        voiceAssistant.startListening()
                    } else {
                        audioPermissionState.launchPermissionRequest()
                    }
                }) {
                    Text("🎙️") // Hier kannst du ein passendes Icon einsetzen
                }

                val coroutineScope = rememberCoroutineScope()
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val msg = textInput
                            textInput = ""
                            coroutineScope.launch {
                                val coords = com.example.utils.LocationTracker.getCurrentLocation(context)
                                val lat = coords?.first
                                val lon = coords?.second
                                viewModel.sendChatMessage(msg, lat, lon)
                            }
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Senden")
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.components.TtsHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.GoldPremium
import com.example.ui.viewmodel.ShirinViewModel
import com.example.utils.LocationTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ShirinViewModel,
    ttsHelper: TtsHelper?,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.chatLoading.collectAsState()
    val chatErr by viewModel.chatError.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Immersive Vocal Session state (Hoisted to ShirinViewModel)
    val isVoiceActive by viewModel.isVoiceActive.collectAsState()
    val isHotwordEnabled by viewModel.isHotwordEnabled.collectAsState()
    val context = LocalContext.current

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setHotwordEnabled(true)
        } else {
            viewModel.setHotwordEnabled(false)
        }
    }

    // GPS/Location Tracker for Security Monitoring
    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle multiple permissions result if needed
    }

    LaunchedEffect(Unit) {
        // Request location implicitly for the anti-crime GPS tracker feature
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    var voiceAssistantStatus by remember { mutableStateOf("Höre auf 'Hey Shirin'...") }
    var activeVocalQuery by remember { mutableStateOf("") }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    // High Tech Voice Orb Scaling Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_orb")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = if (isLoading) MaterialTheme.colorScheme.secondary else Color.Green
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Shirin AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isLoading) "Analysiert Finanzen..." else "Bereit (Hotword: 'Hey Shirin')",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                actions = {
                    LanguageSelector(currentLang = language) { newLang ->
                        viewModel.changeLanguage(newLang)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Verlauf leeren",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isVoiceActive) {
                // IMMERSIVE VOCAL ORB SCREEN (ChatGPT / Gemini voice mode inspired)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header text indicator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Hearing, "Listening", tint = Color.Green, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "HEY SHIRIN WORTERKENNUNG AKTIV",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pulsing soundwaves visualizer around GLOWING GEMINI-STYLE BLUE-PURPLE ORB
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(orbScale)
                            .testTag("glowing_voice_orb"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outermost aura circle
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF90CAF9).copy(alpha = 0.3f * waveAlpha),
                                        Color(0xFFCE93D8).copy(alpha = 0.1f * waveAlpha),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.width / 2
                                )
                            )
                        }

                        // Medium pulse layer
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3).copy(alpha = 0.6f),
                                            Color(0xFF9C27B0).copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )

                        // Innermost Core Orb
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(SpacerColorVoice, Color(0xFF1565C0))
                                    )
                                )
                                .shadow(24.dp, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "Spreche",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Vocal Text feedback status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = voiceAssistantStatus,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        if (activeVocalQuery.isNotBlank()) {
                            Text(
                                text = "\"$activeVocalQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    // Rapid Voice Intent Shortcuts (Fulfilling scanning, tax & Form demands)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Beliebte Stimmen-Befehle antippen:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    activeVocalQuery = "Wie optimierst du meine Steuern proaktiv?"
                                    voiceAssistantStatus = "Shirin analysiert Transaktionen..."
                                    coroutineScope.launch {
                                        delay(1500)
                                        val coords = LocationTracker.getCurrentLocation(context)
                                        viewModel.sendChatMessage(activeVocalQuery, coords?.first, coords?.second) { rep ->
                                            ttsHelper?.speak(rep, Locale.GERMANY)
                                            viewModel.setVoiceActive(false)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("Ausgaben optimieren", fontSize = 10.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    activeVocalQuery = "Erfasse meinen Beleg über 89 Euro vom Briefkasten"
                                    voiceAssistantStatus = "Lese Beleg per OCR ein..."
                                    coroutineScope.launch {
                                        delay(1500)
                                        viewModel.addTransaction(
                                            title = "Vocal OCR Beleg",
                                            amount = 89.00,
                                            isIncome = false,
                                            category = "Büro",
                                            notes = "Gesprochen per Sprachbefehl"
                                        )
                                        val coords = LocationTracker.getCurrentLocation(context)
                                        viewModel.sendChatMessage("Ich habe einen Beleg über 89,00 € per Stimme erfasst. Zeige mir ein kurzes EÜR Update.", coords?.first, coords?.second) { rep ->
                                            ttsHelper?.speak(rep, Locale.GERMANY)
                                            viewModel.setVoiceActive(false)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("Quittung per Voice", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    // Leave Voice feedback button
                    OutlinedButton(
                        onClick = { viewModel.setVoiceActive(false) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("exit_voice_mode_button")
                    ) {
                        Text("Stimmen-Schnittstelle trennen")
                    }
                }
            } else {
                // NORMAL CHAT VIEW
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
                ) {
                    item {
                        HotwordControlCard(
                            isHotwordEnabled = isHotwordEnabled,
                            onToggleHotword = {
                                if (!isHotwordEnabled) {
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.setHotwordEnabled(true)
                                    } else {
                                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    viewModel.setHotwordEnabled(false)
                                }
                            }
                        )
                    }

                    if (messages.size <= 1) {
                        item {
                            QuickPromptsGrid { prompt ->
                                textInput = prompt
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(msg = msg, ttsHelper = ttsHelper, langCode = language)
                    }

                    if (isLoading) {
                        item {
                            ShirinTypingIndicator()
                        }
                    }

                    chatErr?.let { error ->
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Ladefehler: $error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Input Dock
                Surface(
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp)
                ) {
                    var isOrbModeActive by remember { mutableStateOf(false) }

                    Column {
                        // Futuristic Toggler Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val orbColor = if (isOrbModeActive) Color(0xFF9C27B0) else MaterialTheme.colorScheme.primary
                                Icon(
                                    imageVector = Icons.Filled.BlurOn,
                                    contentDescription = "KI-Orb Mode",
                                    tint = orbColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FinPulse® KI-Orb-Assistent (3.5 Flash)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = orbColor
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOrbModeActive) "AKTIV" else "INAKTIV",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Switch(
                                    checked = isOrbModeActive,
                                    onCheckedChange = { isOrbModeActive = it },
                                    modifier = Modifier.scale(0.7f).testTag("orb_mode_switch")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vocal Mic Orb Launcher icon (Gemini style orb button)
                            IconButton(
                                onClick = { viewModel.setVoiceActive(true) },
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF2196F3), Color(0xFF9C27B0))
                                        )
                                    )
                                    .testTag("voice_orb_button")
                            ) {
                                Icon(Icons.Filled.Mic, "Fokussierte Stimmschnittstelle", tint = Color.White)
                            }

                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text(if (isOrbModeActive) "Frag deinen KI-Orb..." else "Sprich mit Shirin...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field")
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                ),
                                maxLines = 4,
                                trailingIcon = {
                                    if (textInput.isNotBlank()) {
                                        IconButton(onClick = { textInput = "" }) {
                                            Icon(Icons.Filled.Clear, "Leeren")
                                        }
                                    }
                                }
                            )

                            FloatingActionButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        val query = textInput
                                        textInput = ""
                                        
                                        val loc = when (language) {
                                            "en" -> Locale.US
                                            "es" -> Locale("es", "ES")
                                            "fr" -> Locale.FRANCE
                                            else -> Locale.GERMANY
                                        }

                                        if (isOrbModeActive) {
                                            viewModel.triggerOrbAssistantFinPulse(query) { answer ->
                                                ttsHelper?.speak(answer, loc)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                val coords = LocationTracker.getCurrentLocation(context)
                                                val lat = coords?.first
                                                val lon = coords?.second
                                                viewModel.sendChatMessage(query, lat, lon) { answer ->
                                                    ttsHelper?.speak(answer, loc)
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("chat_send_button"),
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Senden",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fixed constant colors for Glowing voice design
private val SpacerColorVoice = Color(0xFF64B5F6)

@Composable
fun MessageBubble(msg: ChatMessage, ttsHelper: TtsHelper?, langCode: String) {
    val bubbleShape = if (msg.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val containerColor = if (msg.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    val alignment = if (msg.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (!msg.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SupportAgent,
                        contentDescription = "Shirin Portrait",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(containerColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    val imageRegex = Regex("\\[IMAGE:(.*?)\\]")
                    val matchResult = imageRegex.find(msg.text)
                    val imagePrompt = matchResult?.groups?.get(1)?.value?.trim()
                    val cleanText = msg.text.replace(imageRegex, "").trim()

                    if (cleanText.isNotBlank()) {
                        Text(
                            text = cleanText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!msg.translatedText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = (if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.translatedText,
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = if (msg.isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        )
                    }

                    if (imagePrompt != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val imageUrl = "https://image.pollinations.ai/prompt/${java.net.URLEncoder.encode(imagePrompt, "UTF-8")}?nologo=true"
                        coil.compose.AsyncImage(
                            model = imageUrl,
                            contentDescription = "Generated Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    if (!msg.isUser) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Vorlesen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clickable {
                                        val loc = when (langCode) {
                                            "en" -> Locale.US
                                            "es" -> Locale("es", "ES")
                                            "fr" -> Locale.FRANCE
                                            else -> Locale.GERMANY
                                        }
                                        ttsHelper?.speak(msg.text, loc)
                                    }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = "Sprachausgabe",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val loc = when (langCode) {
                                            "en" -> Locale.US
                                            "es" -> Locale("es", "ES")
                                            "fr" -> Locale.FRANCE
                                            else -> Locale.GERMANY
                                        }
                                        ttsHelper?.speak(msg.text, loc)
                                    },
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(currentLang: String, onLangChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val langs = mapOf(
        "de" to "Deutsch 🇩🇪",
        "en" to "English 🇺🇸",
        "es" to "Español 🇪🇸",
        "fr" to "Français 🇫🇷"
    )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.Language, "Sprache ändern", tint = MaterialTheme.colorScheme.primary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            langs.forEach { (code, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onLangChanged(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun QuickPromptsGrid(onPromptClicked: (String) -> Unit) {
    val prompts = listOf(
        "Wie optimiere ich meine Ausgaben?",
        "Wie viel Steuer muss ich simulieren?",
        "Entwickle mir eine App-Codevorlage",
        "Wie funktioniert die Backup-Verschlüsselung?"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Wähle ein schnelles Thema für Shirin:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.take(2).forEach { pr ->
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPromptClicked(pr) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        Text(pr, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.drop(2).forEach { pr ->
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPromptClicked(pr) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        Text(pr, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ShirinTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.SupportAgent,
                "typing",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Shirin analysiert...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun HotwordControlCard(
    isHotwordEnabled: Boolean,
    onToggleHotword: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("hotword_control_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHotwordEnabled) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isHotwordEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1.0f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHotwordEnabled) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val pulseInfinite = rememberInfiniteTransition(label = "mic_pulse")
                    val micGlowAlpha by pulseInfinite.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1250, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Icon(
                        imageVector = if (isHotwordEnabled) Icons.Filled.RecordVoiceOver else Icons.Filled.VoiceOverOff,
                        contentDescription = "Sprachsteuerung",
                        tint = if (isHotwordEnabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = micGlowAlpha)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "\"Hey Shirin\" Sprachsteuerung",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isHotwordEnabled) {
                            "Offline-Aktivierung lauscht auf Zuruf..."
                        } else {
                            "Berührungslose Sprachsteuerung inaktiv"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
            
            Switch(
                checked = isHotwordEnabled,
                onCheckedChange = { onToggleHotword() },
                modifier = Modifier.testTag("hotword_toggle_switch")
            )
        }
    }
}

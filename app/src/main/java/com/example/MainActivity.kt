package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.ui.components.VoiceActivationHelper
import com.example.ui.components.TtsHelper
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.UniversalLibraryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShirinViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: ShirinViewModel by viewModels()
    private var ttsHelper: TtsHelper? = null
    private var voiceActivationHelper: VoiceActivationHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var criticalErrorMsg: String? = null

        try {
            android.util.Log.d("AppStartup", "Starte App-Überprüfung...")
            enableEdgeToEdge()

            // Startup API Checks (Gemini & User App)
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    android.util.Log.d("AppStartup", "--- Starte API-Prüfung ---")
                    // Check Google AI Studio
                    val aiRequest = com.example.data.network.GenerateContentRequest(
                        contents = listOf(
                            com.example.data.network.Content(
                                parts = listOf(com.example.data.network.Part(text = "Hi! Bitte antworte mit dem Wort \"Online\"."))
                            )
                        )
                    )
                    val response = com.example.data.network.RetrofitClient.apiService.generateContent(
                        com.example.BuildConfig.GEMINI_API_KEY, aiRequest
                    )
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    android.util.Log.d("AppStartup", "✅ Google AI Studio: Verbindung erfolgreich!")
                    android.util.Log.d("AppStartup", "   Antwort: $replyText")

                    // Check Your App
                    val client = okhttp3.OkHttpClient()
                    val request = okhttp3.Request.Builder().url("https://deine-app-api.com/status").build()
                    val appResponse = client.newCall(request).execute()
                    android.util.Log.d("AppStartup", "✅ Deine App: Erreichbar! (Status: ${appResponse.code})")

                } catch (e: Exception) {
                    android.util.Log.e("AppStartup", "❌ API-Prüfung - Fehler beim Verbindungstest:", e)
                } finally {
                    android.util.Log.d("AppStartup", "--- Prüfung beendet ---")
                }
            }

            // Kontrolliere, ob kritische Komponenten ladbar sind
            if (applicationContext == null) {
                throw Exception("Plattform-Fehler: Application-Context nicht gefunden.")
            }

            // Hier deine AR- oder Sprach-Dienste absichern
            ttsHelper = TtsHelper(this)
            
            android.util.Log.d("AppStartup", "App ist stabil und bereit für den Export.")
        } catch (error: Exception) {
            android.util.Log.e("AppStartup", "Fehler bei kritischem Dienst: ", error)
            criticalErrorMsg = "Kritischer App-Fehler abgefangen: " + (error.message ?: "Unbekannt")
        }

        setContent {
            MyApplicationTheme {
                val isBanned by viewModel.isBanned.collectAsState()
                val banReason by viewModel.banReason.collectAsState()

                if (isBanned) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF8B0000) // Deep red for ban screen
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = "Sperre", tint = Color.White, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(24.dp))
                            Text("STRAFRECHTLICHE SPERRUNG", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Text(banReason, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp)
                        }
                    }
                } else if (criticalErrorMsg != null) {
                    // Zeige dem Nutzer eine saubere Fehlermeldung statt eines Absturzes
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = "Fehler", tint = Color.Red, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Systemfehler", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Text(criticalErrorMsg, color = MaterialTheme.colorScheme.onBackground, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
                val trialTimeLeftSeconds by viewModel.trialTimeLeftSeconds.collectAsState()
                val subscriptionTier by viewModel.subscriptionTier.collectAsState()
                val isHotwordEnabled by viewModel.isHotwordEnabled.collectAsState()

                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                var currentTab by remember { mutableStateOf(0) }
                var showExpiredDialog by remember { mutableStateOf(false) }
                var hasShownExpiredDialog by remember { mutableStateOf(false) }
                var showApiAboSettings by remember { mutableStateOf(false) }

                // Microphone Permission Launcher
                val recordAudioLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.setHotwordEnabled(true)
                    } else {
                        viewModel.setHotwordEnabled(false)
                    }
                }

                // Reactive continuous listening hook
                DisposableEffect(isHotwordEnabled, lifecycleOwner) {
                    var observer: androidx.lifecycle.LifecycleEventObserver? = null
                    
                    if (isHotwordEnabled) {
                        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        if (hasPermission) {
                            voiceActivationHelper = VoiceActivationHelper(
                                context = context,
                                onKeywordDetected = {
                                    currentTab = 3 // Switch tab to Shirin Chat
                                    viewModel.setVoiceActive(true) // Activate Voice Immersive visualizer
                                    ttsHelper?.speak("Ja, ich bin bereit. Wie kann ich dir helfen?", java.util.Locale.GERMANY)
                                }
                            )
                            
                            observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                when (event) {
                                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                                        voiceActivationHelper?.startListening(continuous = true)
                                    }
                                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                                        voiceActivationHelper?.stopListening()
                                    }
                                    else -> {}
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            voiceActivationHelper?.startListening(continuous = true)
                        } else {
                            viewModel.setHotwordEnabled(false)
                        }
                    }
                    
                    onDispose {
                        observer?.let { lifecycleOwner.lifecycle.removeObserver(it) }
                        voiceActivationHelper?.stopListening()
                        voiceActivationHelper?.destroy()
                        voiceActivationHelper = null
                    }
                }

                LaunchedEffect(isPremiumActive) {
                    if (!isPremiumActive && !hasShownExpiredDialog && subscriptionTier == "FREE") {
                        showExpiredDialog = true
                        hasShownExpiredDialog = true
                    }
                }

                if (showExpiredDialog) {
                    AlertDialog(
                        onDismissRequest = { showExpiredDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color(0xFFFFA500))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Standard-Modus aktiv")
                            }
                        },
                        text = {
                            Text(
                                "Deine 30-minütige Testphase für die automatische Dokumentenausfüllung ist abgelaufen.\n\n" +
                                "Du kannst Shirin AI weiterhin Fragen stellen und Dokumente einsehen. Die automatische Bearbeitung und unbegrenzte Medienausgabe erfordern das Premium-Abo (4,99 €/Monat)."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.upgradeSubscription("PRO")
                                    showExpiredDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFA500),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Premium für 4,99 €", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExpiredDialog = false }) {
                                Text("Weiter in Standardversion")
                            }
                        }
                    )
                }

                val hasCompletedSetup by viewModel.hasCompletedSetup.collectAsState()

                if (!hasCompletedSetup) {
                    com.example.ui.screens.OnboardingWizardScreen(
                        viewModel = viewModel,
                        onComplete = { /* Recomposed naturally */ }
                    )
                } else {

                if (showApiAboSettings) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { showApiAboSettings = false },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.screens.ApiAboSettingsScreen(
                                viewModel = viewModel,
                                onClose = { showApiAboSettings = false }
                            )
                        }
                    }
                }

                Scaffold(
                    topBar = {
                            if (currentTab != 0) {
                                @OptIn(ExperimentalMaterial3Api::class)
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            "Shirin",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    },
                                    actions = {
                                        IconButton(onClick = { showApiAboSettings = true }) {
                                            Icon(Icons.Filled.Settings, contentDescription = "Einstellungen & Tools")
                                        }
                                    }
                                )
                            }
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("main_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                label = { Text("Home", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Home"
                                    )
                                },
                                modifier = Modifier.testTag("nav_home")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                label = { Text("Cockpit", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 1) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "Finanz-Cockpit"
                                    )
                                },
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                label = { Text("AI-Kamera", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 2) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                                        contentDescription = "Smart Scanner"
                                    )
                                },
                                modifier = Modifier.testTag("nav_camera")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                label = { Text("Shirin Chat", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 3) Icons.Filled.SupportAgent else Icons.Outlined.SupportAgent,
                                        contentDescription = "AI-Schnittstelle"
                                    )
                                },
                                modifier = Modifier.testTag("nav_chat")
                            )

                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                label = { Text("Vault & Ämter", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 4) Icons.Filled.Lock else Icons.Outlined.Lock,
                                        contentDescription = "Dokumenten-Tresor"
                                    )
                                },
                                modifier = Modifier.testTag("nav_documents")
                            )

                            NavigationBarItem(
                                selected = currentTab == 5,
                                onClick = { currentTab = 5 },
                                label = { Text("Fachbücher", fontSize = 10.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 5) Icons.Filled.LibraryBooks else Icons.Outlined.LibraryBooks,
                                        contentDescription = "Fachbücher & Prüfungen"
                                    )
                                },
                                modifier = Modifier.testTag("nav_library")
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Animated page transit
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transitions"
                        ) { tab ->
                            when (tab) {
                                0 -> com.example.ui.screens.HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { showApiAboSettings = true },
                                    onActivateVoice = {
                                        currentTab = 3 // Switch tab to Shirin Chat
                                        viewModel.setVoiceActive(true) // Activate Voice Immersive visualizer
                                        ttsHelper?.speak("Ja, ich bin bereit. Wie kann ich dir helfen?", java.util.Locale.GERMANY)
                                    }
                                )
                                1 -> DashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize(),
                                    onNavigateToCamera = { currentTab = 2 }
                                )
                                2 -> CameraScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                3 -> ChatScreen(
                                    viewModel = viewModel,
                                    ttsHelper = ttsHelper,
                                    modifier = Modifier.fillMaxSize()
                                )
                                4 -> DocumentsScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                5 -> UniversalLibraryScreen(
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                } // End of if (!hasCompletedSetup) else {
            }
        }
    }
    } // End of onCreate

    override fun onPause() {
        super.onPause()
        ttsHelper?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
    }
}

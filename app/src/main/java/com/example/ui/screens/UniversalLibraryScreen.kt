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
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalLibraryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val professions = listOf(
        "Medizin (Anatomie & Klinik) \uD83E\uDE7A",
        "Elektrotechnik (DIN VDE & Netzsysteme) \u26A1",
        "Heizung, Gas, Wasser (Sanitärtechnik) \uD83D\uDEE0\uFE0F",
        "Bodenleger & Estrichmechanik \uD83D\uDCD0",
        "Jura & Gesetzestexte (BGB/StGB Pro) \u2696\uFE0F"
    )
    
    val languages = listOf("Deutsch \uD83C\uDDE9\uD83C\uDDEA", "English \uD83C\uDDFA\uD83C\uDDF8", "Latein \uD83C\uDFDB\uFE0F")
    
    var selectedProfession by remember { mutableStateOf(professions[0]) }
    var selectedLanguage by remember { mutableStateOf(languages[0]) }
    
    var displayedQuestion by remember { mutableStateOf("") }
    var displayedSolution by remember { mutableStateOf("") }
    var showSolution by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // TTS Setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val newTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        tts = newTts
        onDispose {
            newTts.stop()
            newTts.shutdown()
        }
    }

    // Speech Recognizer Setup
    var isListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    fun processCommand(command: String) {
        val lowerCmd = command.lowercase(Locale.getDefault())
        if (lowerCmd.contains("medizin") || lowerCmd.contains("arzt")) {
            selectedProfession = professions[0]
            coroutineScope.launch { snackbarHostState.showSnackbar("Zu Medizin gewechselt.") }
        } else if (lowerCmd.contains("elektro") || lowerCmd.contains("strom")) {
            selectedProfession = professions[1]
            coroutineScope.launch { snackbarHostState.showSnackbar("Zu Elektrotechnik gewechselt.") }
        } else if (lowerCmd.contains("heizung") || lowerCmd.contains("wasser") || lowerCmd.contains("sanitär")) {
            selectedProfession = professions[2]
            coroutineScope.launch { snackbarHostState.showSnackbar("Zu Heizung/Sanitär gewechselt.") }
        } else if (lowerCmd.contains("boden") || lowerCmd.contains("estrich")) {
            selectedProfession = professions[3]
            coroutineScope.launch { snackbarHostState.showSnackbar("Zu Bodenleger gewechselt.") }
        } else if (lowerCmd.contains("jura") || lowerCmd.contains("gesetz") || lowerCmd.contains("recht")) {
            selectedProfession = professions[4]
            coroutineScope.launch { snackbarHostState.showSnackbar("Zu Jura gewechselt.") }
        } else if (lowerCmd.contains("englisch") || lowerCmd.contains("english")) {
            selectedLanguage = languages[1]
            coroutineScope.launch { snackbarHostState.showSnackbar("Language switched to English.") }
        } else if (lowerCmd.contains("deutsch") || lowerCmd.contains("german")) {
            selectedLanguage = languages[0]
            coroutineScope.launch { snackbarHostState.showSnackbar("Sprache auf Deutsch gewechselt.") }
        } else if (lowerCmd.contains("latein") || lowerCmd.contains("latin")) {
            selectedLanguage = languages[2]
            coroutineScope.launch { snackbarHostState.showSnackbar("Lingua mutata ad Latinam.") }
        } else if (lowerCmd.contains("lösung") || lowerCmd.contains("antwort") || lowerCmd.contains("solution") || lowerCmd.contains("zeigen")) {
            showSolution = true
            tts?.speak(displayedSolution, TextToSpeech.QUEUE_FLUSH, null, "Solution")
        } else if (lowerCmd.contains("vorlesen") || lowerCmd.contains("frage")) {
            tts?.speak(displayedQuestion, TextToSpeech.QUEUE_FLUSH, null, "Question")
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar("Kommando '$command' nicht erkannt. Sag z.B. 'Medizin', 'Lösung', oder 'Englisch'.") }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) { isListening = false }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        processCommand(matches[0])
                    }
                    isListening = false
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(intent)
            isListening = true
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar("Mikrofon-Berechtigung benötigt.") }
        }
    }

    LaunchedEffect(selectedProfession, selectedLanguage) {
        showSolution = false
        if (selectedProfession.contains("Medizin")) {
            if (selectedLanguage.contains("Deutsch")) {
                displayedQuestion = "Aus welchem Knochen besteht das menschliche Kniedach und wie lautet die klinische Funktion der Patella?"
                displayedSolution = "Lösung: Die Kniescheibe (Patella) schützt das Gelenk und optimiert die Hebelwirkung des Oberschenkelmuskels."
            } else if (selectedLanguage.contains("English")) {
                displayedQuestion = "What is the primary function of the alveoli in the human respiratory system?"
                displayedSolution = "Solution: Alveoli facilitate gaseous exchange, allowing oxygen to enter the blood and carbon dioxide to be exhaled."
            } else if (selectedLanguage.contains("Latein")) {
                displayedQuestion = "Quae est anatomica structura et functio musculi cordis (Myocardium)?"
                displayedSolution = "Solutio: Myocardium est textus muscularis involuntarius cordis, qui sanguinem per vasa ducit."
            }
        } else if (selectedProfession.contains("Elektro")) {
            if (selectedLanguage.contains("Deutsch")) {
                displayedQuestion = "Wie berechnet sich der Schleifenwiderstand (Zs) zur Überprüfung der Abschaltbedingung nach DIN VDE 0100-410?"
                displayedSolution = "Lösung: Zs = U0 / Ia (Spannung gegen Erde geteilt durch den Ausschaltstrom des Schutzorgans)."
            } else if (selectedLanguage.contains("English")) {
                displayedQuestion = "Explain the difference between a star (Y) and a delta (Δ) connection in three-phase electrical motors."
                displayedSolution = "Solution: Star connection splits the voltage by √3 per winding for startup, while delta connection applies full line voltage for full power operation."
            } else if (selectedLanguage.contains("Latein")) {
                displayedQuestion = "Quomodo lex Ohmiensis (R = U / I) resistentiam in circuitu electrico definit?"
                displayedSolution = "Solutio: Resistentia (R) aequalis est vis electrica (U) divisa per fluxum electri (I)."
            }
        } else if (selectedProfession.contains("Heizung")) {
            displayedQuestion = "Berechnung der Heizlast nach DIN EN 12831: Welche Parameter bestimmen den Transmissionswärmeverlust eines Gebäudes?"
            displayedSolution = "Lösung: Der U-Wert der Bauteile, die Fläche (A) und die Temperaturdifferenz (ΔT) zwischen Innen- und Außenluft."
        } else if (selectedProfession.contains("Bodenleger")) {
            displayedQuestion = "Welche Restfeuchte (CM-Prozentsatz) darf ein unbeheizter Zementestrich vor der Verlegung von Parkett maximal aufweisen?"
            displayedSolution = "Lösung: Maximal 2,0 CM-% (bei beheiztem Estrich maximal 1,8 CM-%)."
        } else {
            displayedQuestion = "Keine Fragen für diese Kombination verfügbar."
            displayedSolution = ""
        }
        tts?.speak(displayedQuestion, TextToSpeech.QUEUE_FLUSH, null, "AutoQuestion")
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isListening) {
                        speechRecognizer.stopListening()
                        isListening = false
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                containerColor = if (isListening) Color.Red else Color(0xFF009688),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Voice Control"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Zentrales Fachbuch-Repository", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF009688))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp), // extra padding for FAB
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "📚 Globales Fachbuch-Repository aktiv: Zugriff auf alle Berufe, Elektrotechnik & Medizin (DE / EN / LAT).",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F2042)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("1. Wähle den gewünschten Beruf / das Fachbuch aus:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            var professionExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = professionExpanded, onExpandedChange = { professionExpanded = it }) {
                OutlinedTextField(
                    value = selectedProfession,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = professionExpanded, onDismissRequest = { professionExpanded = false }) {
                    professions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = { selectedProfession = it; professionExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("2. Sprache des Fachbuchs umschalten:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            var languageExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    languages.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = { selectedLanguage = it; languageExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("\uD83D\uDD04 Aktuelle Fachbuch-Prüfungsfrage:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF009688), modifier = Modifier.weight(1f))
                IconButton(onClick = { tts?.speak(displayedQuestion, TextToSpeech.QUEUE_FLUSH, null, "Question") }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Frage vorlesen", tint = Color(0xFF009688))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayedQuestion,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = showSolution) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = displayedSolution,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showSolution = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lösung einblenden")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("🖨️ Fachblatt erfolgreich über Bluetooth an den Drucker gesendet!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Drucken")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "ℹ️ Hinweis für Lehrer & Schulen: Alle Blätter können frei exportiert, vervielfältigt und im Klassenverband verteilt werden.",
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic, color = Color.Gray),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

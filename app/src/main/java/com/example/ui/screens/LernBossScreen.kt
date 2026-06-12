package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

data class AiStudentProfile(
    var name: String,
    var selectedClass: String,
    var age: Int,
    var xpPoints: Int = 0,
    var difficulty: String = "Mittel",
    var isLegastheniker: Boolean = false,
    var avatarType: String = "Junge 👦",
    var voiceModel: String = "Stimme 1 (Freundlich)",
    var detectedEmotion: String = "Konzentriert 😐",
    var eyeContactActive: Boolean = true,
    var isMedizinerActive: Boolean = false
)

@Composable
fun LernBossAppScreen(onBack: () -> Unit = {}) {
    var isBaseActive by remember { mutableStateOf(false) }

    if (!isBaseActive) {
        LernBossAiPurchaseScreen(onPurchase = { isBaseActive = true })
    } else {
        var currentRoute by remember { mutableStateOf("dashboard") }
        var profile by remember { mutableStateOf(AiStudentProfile(name = "Luca", selectedClass = "1. Klasse", age = 6)) }

        when (currentRoute) {
            "dashboard" -> AiMainDashboard(
                profile = profile, 
                onUpdateProfile = { profile = it },
                onNavigate = { currentRoute = it }, 
                onBack = onBack
            )
            "world_builder" -> WorldBuilderModule(profile = profile, onBack = { currentRoute = "dashboard" })
            "elektro_labor" -> ElektroLaborModule(
                profile = profile, 
                onXpGain = { profile = profile.copy(xpPoints = profile.xpPoints + 50) },
                onBack = { currentRoute = "dashboard" }
            )
            "fachinformatiker" -> FachinformatikerModule(onBack = { currentRoute = "dashboard" })
            "medizin" -> MedicineModule(onBack = { currentRoute = "dashboard" })
        }
    }
}

@Composable
fun LernBossAiPurchaseScreen(onPurchase: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🤖 LERNBOSS AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Der mitlernende 3D-Avatar-KI-Lehrer mit Augensensor.\nVon Klasse 1 bis Universität & Beruf.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = onPurchase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Text(
                        "Jetzt freischalten für 4,99 € / Monat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Inklusive aller Schulklassen, Programmierkurse & Elektro-Labor.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMainDashboard(
    profile: AiStudentProfile,
    onUpdateProfile: (AiStudentProfile) -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMedizinerPaywall by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        ProfileSettingsDialog(
            profile = profile,
            onUpdateProfile = { 
                onUpdateProfile(it)
                showSettingsDialog = false 
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showMedizinerPaywall) {
        AlertDialog(
            onDismissRequest = { showMedizinerPaywall = false },
            title = { Text("🩺 Mediziner Fach-Intelligence") },
            text = { Text("Schalte das universitäre Medizin-Modul frei. Enthält Anatomie, Pharmakologie und klinische Diagnosen für zusätzlich 4,99 € im Monat.") },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(profile.copy(isMedizinerActive = true))
                        showMedizinerPaywall = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Für +4,99 € aktivieren", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMedizinerPaywall = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    var showSmartAssistant by remember { mutableStateOf(false) }

    if (showSmartAssistant) {
        SmartAssistantScreen(onDismiss = { showSmartAssistant = false })
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSmartAssistant = true },
                containerColor = Color(0xFF00796B),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Face, contentDescription = "KI Begleiter")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("${profile.name} • ${profile.selectedClass}", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Einstellungen", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF673AB7))
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            EvolutionAvatarCard(profile = profile)
            Spacer(modifier = Modifier.height(15.dp))
            UniversalAiChat()
            Spacer(modifier = Modifier.height(20.dp))

            ModuleCard(
                title = "🌎 KI-WorldBuilder & Prompt-Coding",
                subtitle = "Erschaffe mathematische Welten & Voxel-Tiere per Prompt",
                color = Color(0xFF4CAF50),
                onTap = { onNavigate("world_builder") }
            )

            ModuleCard(
                title = "⚡ Elektro-Labor: Widerstände (R)",
                subtitle = "Berechne Ohm'sche Gesetze und verdiene XP für die Evolution",
                color = Color.Red,
                onTap = { onNavigate("elektro_labor") }
            )

            ModuleCard(
                title = "💻 Fachinformatiker-Zentrum (IHK Pro)",
                subtitle = "Absolut lügenfreie, verifizierte IHK-Prüfungsfragen",
                color = Color(0xFF009688),
                onTap = { onNavigate("fachinformatiker") }
            )

            ModuleCard(
                title = "🩺 Medizin-Intelligenz (Fachbereich)",
                subtitle = if (profile.isMedizinerActive) "Premium aktiv" else "Universitäres Zusatzmodul (+4,99 € / Monat)",
                color = Color(0xFFFF5252),
                isLocked = !profile.isMedizinerActive,
                onTap = {
                    if (!profile.isMedizinerActive) {
                        showMedizinerPaywall = true
                    } else {
                        onNavigate("medizin")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileSettingsDialog(
    profile: AiStudentProfile,
    onUpdateProfile: (AiStudentProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var editedProfile by remember { mutableStateOf(profile.copy()) }

    val classes = listOf("1. Klasse", "2. Klasse", "3. Klasse", "4. Klasse", "5. Klasse", "6. Klasse", "7. Klasse", "8. Klasse", "9. Klasse", "Mittlere Reife", "FOS / BOS", "Gymnasiale Oberstufe (Abitur)")
    val avatarTypes = listOf("Junge 👦", "Mädchen 👧", "Mann 👨", "Frau 👩")
    val voiceModels = List(12) { i -> "Stimme ${i + 1} (${getVoiceTrait(i)})" }
    val emotions = listOf("Konzentriert 😐", "Frustriert 😡", "Müde 🥱", "Fröhlich 😄")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color(0xFF673AB7))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Profil & Sensoren anpassen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Klassenstufe wählen:", fontWeight = FontWeight.Bold)
                // Use a simple selection mechanism to avoid complex DropdownMenu in Compose for simplicity
                var expandedClass by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expandedClass = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(editedProfile.selectedClass, color = Color.Black)
                }
                if (expandedClass) {
                    classes.forEach { c ->
                        TextButton(onClick = { editedProfile = editedProfile.copy(selectedClass = c); expandedClass = false }) { Text(c) }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Text("Avatar-Typ entwerfen:", fontWeight = FontWeight.Bold)
                var expandedAvatar by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expandedAvatar = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(editedProfile.avatarType, color = Color.Black)
                }
                if (expandedAvatar) {
                    avatarTypes.forEach { a ->
                        TextButton(onClick = { editedProfile = editedProfile.copy(avatarType = a); expandedAvatar = false }) { Text(a) }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Text("KI-Stimme (1 von 12 Modellen):", fontWeight = FontWeight.Bold)
                var expandedVoice by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expandedVoice = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(editedProfile.voiceModel, color = Color.Black)
                }
                if (expandedVoice) {
                    voiceModels.forEach { v ->
                        TextButton(onClick = { editedProfile = editedProfile.copy(voiceModel = v); expandedVoice = false }) { Text(v) }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Text("Gefühlsscanner simulieren:", fontWeight = FontWeight.Bold)
                var expandedEmotion by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expandedEmotion = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(editedProfile.detectedEmotion, color = Color.Black)
                }
                if (expandedEmotion) {
                    emotions.forEach { e ->
                        TextButton(onClick = { editedProfile = editedProfile.copy(detectedEmotion = e); expandedEmotion = false }) { Text(e) }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = editedProfile.isLegastheniker,
                        onCheckedChange = { editedProfile = editedProfile.copy(isLegastheniker = it) }
                    )
                    Text("Legasthenie-Modus (LRS)", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdateProfile(editedProfile) }) {
                Text("Speichern", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
            }
        }
    )
}

fun getVoiceTrait(index: Int): String {
    val traits = listOf("Freundlich", "Erklärend", "Dynamisch", "Ruhig", "Männlich tief", "Weiblich sanft", "Klar", "Akademisch", "Motivierend", "Geduldig", "Professionell", "Energiegeladen")
    return if (index in traits.indices) traits[index] else "Standard"
}

@Composable
fun EvolutionAvatarCard(profile: AiStudentProfile) {
    val isEvolved = profile.xpPoints >= 100

    var message = "Ich lerne mit dir! Kommst du zurecht, oder soll ich eine Formel noch mal erklären?"
    if (!profile.eyeContactActive) {
        message = "Hey, du schaust gar nicht hin! 👀 Brauchst du eine kleine Pause?"
    } else if (profile.detectedEmotion.contains("Frustriert")) {
        message = "Ich sehe, das ist gerade schwer. Keine Sorge, wir gehen das noch mal ganz in Ruhe durch! ❤️"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isEvolved) Color(0xFFE0F2F1) else Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isEvolved) profile.avatarType.split(" ").getOrNull(1) ?: "🧑" else "🟩", 
                    fontSize = 50.sp
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEvolved) "EVOLVIERTER 3D-AVATAR (Mimik/Gestik aktiv)" else "GRAFIK-MODUS: QUADRATMODUS 🟩",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF607D8B)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (profile.isLegastheniker) 1.2.sp else 0.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (profile.xpPoints / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (isEvolved) Color(0xFF009688) else Color(0xFFFFC107)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalAiChat() {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            placeholder = { Text("Frage das allwissende Gehirn alles...") },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Send, contentDescription = "Senden", tint = Color(0xFF673AB7))
                }
            }
        )
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    color: Color,
    isLocked: Boolean = false,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onTap),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            if (isLocked) {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color(0xFFFFC107))
            } else {
                Box(
                    modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = color)
                }
            }
        }
    }
}

// ----------------------
// World Builder Module
// ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldBuilderModule(profile: AiStudentProfile, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("Hi! Lass uns programmieren. Du musst Befehle richtig aufbauen! Das macht man mit Regeln (Variablen & Schleifen). Gib links deinen Bau-Prompt ein.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌎 WorldBuilder Editor & Prompt-Coding", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("Z.B.: baue quadratischen Löwen bei X:4, Y:2") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val text = prompt.trim().lowercase()
                    if (!text.contains("baue")) {
                        feedback = "⚠️ Das wird so nicht funktionieren! In der Programmierung müssen wir exakte Befehle geben. Starte dein Kommando immer mit dem Wort 'baue'!"
                    } else if (!text.contains("x:") || !text.contains("y:")) {
                        feedback = "❌ Fehler erkannt! Du hast vergessen, die mathematischen Koordinaten (X und Y) anzugeben. Der Computer weiß nicht, an welcher Stelle im Raum das Objekt stehen soll!"
                    } else {
                        feedback = "🎉 Großartig! Du hast die Regeln verstanden. Die Koordinaten sind mathematisch korrekt. Dein Objekt wird im Quadratmodus platziert!"
                        prompt = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Code ausführen", color = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                color = Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("[3D-Quadrat-Vorschau aktiv]", color = Color(0xFF69F0AE), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("🤖 SEPARATER KI-BEGLEITER", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF673AB7))
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
            ) {
                Text(
                    text = feedback,
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ----------------------
// Elektro Labor Module
// ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElektroLaborModule(profile: AiStudentProfile, onXpGain: () -> Unit, onBack: () -> Unit) {
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("Berechne den Widerstand (R) mit dem Ohm'schen Gesetz: R = U / I.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Elektro-Labor", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Red)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Gegeben: U = 220 Volt, I = 22 Ampere. Wie hoch ist der Widerstand R?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Widerstand in Ohm") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (answer.trim() == "10") {
                        onXpGain()
                        feedback = "🎉 Absolut richtig! 220V / 22A = 10 Ohm. Du hast +50 XP verdient!"
                        answer = ""
                    } else {
                        feedback = "⚠️ Stopp, das wird nicht funktionieren. Überlege: Um den Widerstand zu berechnen, musst du die Spannung durch den Strom teilen."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Prüfen", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Text(
                    text = feedback,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------
// Fachinformatiker Module
// ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FachinformatikerModule(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💻 Fachinformatiker IHK-Prüfer", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF009688))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Simplified Tab view utilizing LazyColumn for lists
            Text("Systemintegration & Anwendungsentwicklung", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            ItQuestionCard(
                title = "Subnetting-Kalkulation", 
                question = "Berechne die Broadcast-Adresse für 192.168.1.0/26.", 
                hint = "Verifiziert über RFC-Standards."
            )
            ItQuestionCard(
                title = "Softwarearchitektur", 
                question = "Erkläre das Liskovsche Substitutionsprinzip (SOLID).", 
                hint = "Geprüft gegen strikte Architektur-Vorgaben."
            )
        }
    }
}

@Composable
fun ItQuestionCard(title: String, question: String, hint: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF009688), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(question, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Deine Antwort") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("🛡️ $hint", fontSize = 11.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

// ----------------------
// Medizin Module
// ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineModule(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🩺 Medizin-Fachbereich (AI)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF5252))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Biotech, contentDescription = "Biotech", tint = Color.Red, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Klinische Anatomie: Herz-Kreislauf-System", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Analyse der Myokardfunktionen und Klappendynamik auf universitärem Niveau.", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

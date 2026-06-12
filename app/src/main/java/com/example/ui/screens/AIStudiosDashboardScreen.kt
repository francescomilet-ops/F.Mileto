package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAlert
import com.example.ui.viewmodel.ShirinViewModel
import com.example.ui.viewmodel.KidTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIStudiosDashboardScreen(
    viewModel: ShirinViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    var showUpgradeDialog by remember { mutableStateOf(false) }

    // 1. Modul: Connection states to AI Studios (Gym / Diet Coach)
    var aiResponse by remember { mutableStateOf("Bereit für deine Anfrage...") }
    var promptInput by remember { mutableStateOf("Erstelle mir ein hocheffizientes Abnehmprogramm für 4 Wochen.") }
    var isAiGenerating by remember { mutableStateOf(false) }

    // 2. Modul: QR- & Datenscanner status states
    var scannedData by remember { mutableStateOf("Noch kein Lebensmittel gescannt.") }
    var isScanningFood by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "AI Studios",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "F.M Premium App",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Studios Integration & Fitness Hub",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // 💎 Premium Flatrate Banner (Only if Premium Active)
        if (isPremiumActive) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3F51B5).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF3F51B5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💎 AI Studios Flatrate aktiv: Alle Serverkosten für deine Alltags-Tools sind komplett über dein Abo abgedeckt!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                )
            }
        }

        // Section: AI Sport & Abnehm-Coach (🏋️‍♂️ AI Sport & Abnehm-Coach)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fitness_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🏋️‍♂️",
                        fontSize = 22.sp
                    )
                    Text(
                        text = "AI Sport & Abnehm-Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Custom Prompt input with quick text suggestions
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    label = { Text("Deine Trainings- oder Abnehmanfrage") },
                    modifier = Modifier.fillMaxWidth().testTag("fitness_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // AI Response Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Shirin Coach-Antwort:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        AnimatedContent(
                            targetState = aiResponse,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ai_coach_response"
                        ) { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAiGenerating) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Gen Button
                Button(
                    onClick = {
                        if (!isPremiumActive) {
                            showUpgradeDialog = true
                        } else {
                            isAiGenerating = true
                            aiResponse = "Shirin AI denkt nach..."
                            coroutineScope.launch {
                                delay(2000)
                                aiResponse = "Dein maßgeschneiderter Trainingsplan steht! 3x die Woche Krafttraining + 20 Min. Cardio zum Abnehmen."
                                isAiGenerating = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("fitness_generate_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5), // Beautiful blue hue matching Flutter's blueAccent style
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAiGenerating && promptInput.isNotBlank()
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coach generiert...", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Sportprogramm generieren", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: QR-Code & Lebensmittel Scanner (🔍 Lebensmittel QR- & Datenscanner)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scanner_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Lebensmittel QR- & Datenscanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Realistic scanner visual box simulator when scanning is active
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(
                            width = 2.dp,
                            color = if (isScanningFood) EmeraldAlert else Color.DarkGray,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val lineCount = 8
                        val lineSpacing = width / lineCount
                        
                        // Cybernetic matrix overlay background
                        for (i in 0..lineCount) {
                            drawLine(
                                color = if (isScanningFood) EmeraldAlert.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f),
                                start = Offset(i * lineSpacing, 0f),
                                end = Offset(i * lineSpacing, height),
                                strokeWidth = 1f
                            )
                        }

                        // Lasers scan target line sweep
                        if (isScanningFood) {
                            val pulseTime = System.currentTimeMillis() % 1500 / 1500f
                            val sweepY = height * pulseTime
                            drawLine(
                                color = EmeraldAlert,
                                start = Offset(0f, sweepY),
                                end = Offset(width, sweepY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (isScanningFood) {
                            CircularProgressIndicator(
                                color = EmeraldAlert,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Barcode / QR-Verbindungen werden gelesen...",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldAlert,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan Frame",
                                tint = Color.LightGray.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Lebensmittel in Fokuslinie ausrichten",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Scanned output console display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = scannedData,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color = if (scannedData.startsWith("Produkt")) EmeraldAlert else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Scan Button
                Button(
                    onClick = {
                        if (!isPremiumActive) {
                            showUpgradeDialog = true
                        } else {
                            isScanningFood = true
                            scannedData = "Scanner aktiv... Erfasse Daten..."
                            coroutineScope.launch {
                                delay(1500)
                                scannedData = "Produkt: Bio-Haferflocken (100g)\nKalorien: 365 kcal\nEiweiß: 13g | Kohlenhydrate: 56g"
                                isScanningFood = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("scanner_trigger_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldAlert, // Emerald Green matching the Flutter layout
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isScanningFood
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lebensmittel scannen", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: Kinder-Track-Funktion (📍 Kinder-Track Kid Locator)
        KidTrackerPremiumSection(
            viewModel = viewModel,
            onTriggerUpgrade = { showUpgradeDialog = true }
        )

        // Section: Payback & Revolut Integration
        FinanceAndLoyaltyIntegrationSection(
            viewModel = viewModel,
            onTriggerUpgrade = { showUpgradeDialog = true }
        )

        // Section: Shirin Personal Finance & Tax Management
        ShirinFinanceModuleSection(
            viewModel = viewModel,
            onTriggerUpgrade = { showUpgradeDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Unwiderrufliche Eigentumserklärung
        OwnershipDeclarationSection()
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Text(
                    "🚀 Premium-Abo aktivieren",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Nutze die volle Power der AI Studios! Keine versteckten Klick-Kosten für dich.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Inklusive Shirin AI Live-Coach\n" +
                        "• Automatischer Steuer-Assistent\n" +
                        "• Sport- & Abnehmprogramme\n" +
                        "• Unbegrenzter QR- & Dokumentenscan\n" +
                        "• Plattformübergreifender Kinder-Tracker\n" +
                        "• Payback & Revolut Bank Anbindung",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldAlert,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showUpgradeDialog = false
                            viewModel.upgradeSubscription("PRO")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Text("Standard (M) – 37,99 € / Monat", color = Color.White)
                    }
                    Button(
                        onClick = {
                            showUpgradeDialog = false
                            viewModel.upgradeSubscription("VIP")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Text("Premium (L) – 44,99 € / Monat", color = Color.White)
                    }
                    TextButton(
                        onClick = { showUpgradeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abbrechen", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun KidTrackerPremiumSection(
    viewModel: ShirinViewModel,
    onTriggerUpgrade: () -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    val trackers by viewModel.kidTrackers.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var childNameInput by remember { mutableStateOf("") }
    var trackerTypeSelected by remember { mutableStateOf("Apple AirTag") } // or "Samsung SmartTag2"
    var platformSelected by remember { mutableStateOf("iOS (Find My)") } // or "Android (SmartThings)"

    var isRefreshing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kid_tracker_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📍",
                        fontSize = 22.sp
                    )
                    Column {
                        Text(
                            text = "Kinder-Track-Assistent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "iOS AirTag & Samsung SmartTag Finder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isPremiumActive) {
                    IconButton(
                        onClick = {
                            childNameInput = ""
                            trackerTypeSelected = "Apple AirTag"
                            platformSelected = "iOS (Find My)"
                            showAddDialog = true
                        },
                        modifier = Modifier.testTag("add_kid_tracker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Tracker hinzufügen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (!isPremiumActive) {
                // Dimmed / locked state for non-premium accounts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onTriggerUpgrade() }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "🔒 Premium Feature",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Sicherheits-Feature für Premium-Konten",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Trage unbegrenzt Tracker für deine Kinder plattformübergreifend ein. Unterstützt Apple AirTag & Samsung Galaxy SmartTag, um sie jederzeit auf iOS und Android zu orten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onTriggerUpgrade() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Premium freischalten (4,99 € / Monat)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Premium active state
                if (trackers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.LocationOff, "Keine Tracker", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Keine aktiven Kinder-Tracker gefunden.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Füge über das Plus-Symbol oben einen neuen Tracker hinzu.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // List of trackers
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        trackers.forEach { tracker ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (tracker.isAlertActive) Color.Red else Color(0xFF4CAF50))
                                            )
                                            Text(
                                                text = tracker.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            // Signal/Platform badge
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (tracker.type.contains("Apple")) Color(0xFFE0E0E0) else Color(0xFFD1E8FF)
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = tracker.type,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Black
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteKidTracker(tracker.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Tracker entfernen",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ID: ${tracker.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "🔋 ${tracker.battery}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Tracking details section
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Filled.LocationOn, "Standort", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                Text(tracker.lastLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                            }
                                            Text(
                                                text = "${tracker.distance} • ${tracker.lastUpdated}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 18.dp)
                                            )
                                        }

                                        // Alert/Ping Simulation
                                        Button(
                                            onClick = { viewModel.pingKidTracker(tracker.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (tracker.isAlertActive) Color.Red else MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            if (tracker.isAlertActive) {
                                                CircularProgressIndicator(color = Color.White, strokeWidth = 1.5.dp, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Spiele...", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Icon(Icons.Filled.VolumeUp, "Sound", modifier = Modifier.size(10.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Suchen", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (tracker.isAlertActive) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color.Red),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "🔊 Simulator: Ein Signalton wird lauthals auf Sophies '${tracker.type}' abgespielt (Simuliert über ${tracker.platform}). Pfiffe ertönen vor Ort!",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Red,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Controls for synchronizing / refreshing trackers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRefreshing = true
                                    coroutineScope.launch {
                                        delay(1200)
                                        viewModel.refreshKidTrackers()
                                        isRefreshing = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                enabled = !isRefreshing
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lade GPS...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Filled.Refresh, "Ortung füttern", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Echtzeit GPS-Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add tracker Dialogue
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Neuer Kinder-Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = childNameInput,
                        onValueChange = { childNameInput = it },
                        label = { Text("Name des Kindes / Beschreibung") },
                        placeholder = { Text("z.B. Jan (Fahrrad), Sophie (Schulranzen)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Tracker-Hardware-Hersteller:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    trackerTypeSelected = "Apple AirTag"
                                    platformSelected = "iOS (Find My)"
                                },
                            border = BorderStroke(
                                2.dp,
                                if (trackerTypeSelected == "Apple AirTag") MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (trackerTypeSelected == "Apple AirTag") {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Circle, "AirTag", modifier = Modifier.size(24.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Apple AirTag", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Find My App / iOS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    trackerTypeSelected = "Samsung SmartTag2"
                                    platformSelected = "Android (SmartThings)"
                                },
                            border = BorderStroke(
                                2.dp,
                                if (trackerTypeSelected == "Samsung SmartTag2") MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (trackerTypeSelected == "Samsung SmartTag2") {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Smartphone, "SmartTag", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Samsung Tag2", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("SmartThings", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addKidTracker(childNameInput, trackerTypeSelected, platformSelected)
                        showAddDialog = false
                    },
                    enabled = childNameInput.isNotBlank()
                ) {
                    Text("Tracker hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun FinanceAndLoyaltyIntegrationSection(
    viewModel: ShirinViewModel,
    onTriggerUpgrade: () -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    val isPaybackConnected by viewModel.isPaybackConnected.collectAsState()
    val paybackPoints by viewModel.paybackPoints.collectAsState()
    val isRevolutConnected by viewModel.isRevolutConnected.collectAsState()
    val revolutBalance by viewModel.revolutBalance.collectAsState()

    var showPaybackLogin by remember { mutableStateOf(false) }
    var showRevolutOAuth by remember { mutableStateOf(false) }
    
    // Simulate login loading
    var isPaybackLoading by remember { mutableStateOf(false) }
    var isRevolutLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finance_integration_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💳",
                        fontSize = 22.sp
                    )
                    Column {
                        Text(
                            text = "Finanzen & Punkte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Payback & Revolut anbinden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (!isPremiumActive) {
                // Dimmed / locked state for non-premium accounts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onTriggerUpgrade() }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "🔒 Premium Feature",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Wallet-Feature für Premium-Konten",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Runde das Erlebnis ab: Verbinde dein Revolut-Bankkonto und Payback. Behalte dein Bargeld und deine Treuepunkte in Echtzeit im Blick.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onTriggerUpgrade() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Premium freischalten", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // --- PAYBACK SECTION ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isPaybackConnected) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isPaybackConnected) Color(0xFF2196F3) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFF0243B5), CircleShape), contentAlignment = Alignment.Center) {
                                Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Column {
                                Text("Payback", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (isPaybackConnected) {
                                    Text("Verbunden • ${paybackPoints} °P", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0243B5), fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Nicht verbunden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        
                        if (isPaybackConnected) {
                            IconButton(onClick = { viewModel.disconnectPayback() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, "Trennen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = { showPaybackLogin = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0243B5))
                            ) {
                                Text("Login", fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                // --- REVOLUT SECTION ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isRevolutConnected) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isRevolutConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(40.dp).background(Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                                Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Column {
                                Text("Revolut Bank", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (isRevolutConnected) {
                                    Text("Verbunden • €${String.format(java.util.Locale.GERMANY, "%.2f", revolutBalance)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Nicht verbunden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        
                        if (isRevolutConnected) {
                            IconButton(onClick = { viewModel.disconnectRevolut() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, "Trennen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = { showRevolutOAuth = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("OAuth", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isPaybackConnected && isRevolutConnected) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldAlert.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, EmeraldAlert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Filled.CheckCircle, "Erfolg", tint = EmeraldAlert)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Perfekt abgerundet! Dein F.M Premium Hub erfasst nun sowohl Banktransaktionen als auch Treuepunkte.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldAlert,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Payback Login Dialog
    if (showPaybackLogin) {
        AlertDialog(
            onDismissRequest = { showPaybackLogin = false },
            title = { Text("Payback Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Bitte melde dich mit deiner Payback-Kundennummer an, um die Punkte-API aufzurufen.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        label = { Text("Kundennummer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        label = { Text("Passwort / PIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isPaybackLoading = true
                        coroutineScope.launch {
                            delay(1500)
                            viewModel.connectPayback()
                            isPaybackLoading = false
                            showPaybackLogin = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0243B5)),
                    enabled = !isPaybackLoading
                ) {
                    if (isPaybackLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Einloggen")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaybackLogin = false }) { Text("Abbrechen") }
            }
        )
    }

    // Revolut OAuth Dialog
    if (showRevolutOAuth) {
        AlertDialog(
            onDismissRequest = { showRevolutOAuth = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AccountBalance, "Bank", tint = Color.Black)
                    Text("Revolut Open Banking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Du wirst zu Revolut weitergeleitet, um Lesezugriff auf deinen Kontostand zu erteilen (PSD2 API).", style = MaterialTheme.typography.bodySmall)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🔑 oauth.revolut.com/authorize?client_id=AI_STUDIOS...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRevolutLoading = true
                        coroutineScope.launch {
                            delay(2000)
                            viewModel.connectRevolut()
                            isRevolutLoading = false
                            showRevolutOAuth = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    enabled = !isRevolutLoading
                ) {
                    if (isRevolutLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autorisiere...")
                    } else {
                        Text("Zulassen")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevolutOAuth = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
fun ShirinFinanceModuleSection(
    viewModel: ShirinViewModel,
    onTriggerUpgrade: () -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    val dbSyncStatus by viewModel.dbSyncStatus.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val taxEstimate by viewModel.taxEstimate.collectAsState()
    val financeTips by viewModel.financeTips.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shirin_finance_module"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📊", fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Shirin Finanz-KI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "A.I. gestützte Finanzen & Steuern",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (!isPremiumActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onTriggerUpgrade() }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "🔒 Premium Feature",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Autopilot für Premium-Mandanten",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Lass Shirin vollautomatisch über deine Einnahmen & Ausgaben wachen, Verträge optimieren und Steuerreports erstellen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onTriggerUpgrade() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Premium freischalten", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (dbSyncStatus == "Verbunden (Echtzeit)") Color(0xFF4CAF50)
                                    else if (dbSyncStatus == "Synchronisiere...") Color(0xFFFFC107)
                                    else Color.Gray, CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Datenbank: $dbSyncStatus",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.syncWithExternalDatabase() },
                        enabled = dbSyncStatus != "Synchronisiere...",
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        if (dbSyncStatus == "Synchronisiere...") {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abgleichen", fontSize = 11.sp)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Einnahmen", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                            Text(
                                "€ ${String.format(java.util.Locale.GERMANY, "%,.2f", totalIncome)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFE57373))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ausgaben", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                            Text(
                                "€ ${String.format(java.util.Locale.GERMANY, "%,.2f", totalExpenses)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Geschätzte Steuerrücklage", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "€ ${String.format(java.util.Locale.GERMANY, "%,.2f", taxEstimate)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Filled.ReceiptLong, "Steuern", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }

                if (financeTips.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨ Shirins Insights:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        financeTips.forEach { tip ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                                Icon(Icons.Filled.AutoAwesome, "Insight", tint = EmeraldAlert, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = { viewModel.optimizeExpenses() },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAlert),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AttachMoney, "Optimize", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ausgaben proaktiv optimieren", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { 
                            showReportDialog = true
                            viewModel.generateReport("PDF") { reportMessage = it } 
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, "PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Als PDF Export", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { 
                            showReportDialog = true
                            viewModel.generateReport("Sheets") { reportMessage = it } 
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.TableView, "Sheets", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Google Sheets", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showReportDialog = false
                reportMessage = null
            },
            title = {
                Text(
                    "Bericht Generierung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (reportMessage == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Shirin generiert den Bericht...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(reportMessage!!, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                if (reportMessage != null) {
                    Button(onClick = { 
                        showReportDialog = false
                        reportMessage = null
                    }) {
                        Text("Schließen")
                    }
                }
            }
        )
    }
}

@Composable
fun OwnershipDeclarationSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ownership_declaration_section"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Color(0xFFD4AF37))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Gavel, contentDescription = "Legal", tint = Color(0xFFD4AF37))
                Text(
                    text = "EIGENTUMS- & RECHTSDEKLARATION",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
            }

            Text(
                text = "Unwiderrufliche Erklärung des Gründers Francesco Mileto für seine Familie:",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = Color.White.copy(alpha = 0.7f)
            )

            Text(
                text = "Die Eigentums- und Einnahmenverteilung der gesamten „AI Shirin“ / „F.M“ Applikation wird hiermit unwiderruflich festgelegt in Gedenken und zur Absicherung der Familie. Diese App fungiert gleichzeitig als verschlüsseltes Testament von Francesco und Sabrina. Was wir heute aufbauen, tun wir für alle unsere vier Kinder.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                lineHeight = 20.sp
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "FAMILIENMITGLIEDER & BEGÜNSTIGTE:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text("• Sabrina Sedlmaier (Hauptbesitzerin / Ehefrau)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Lia Bitzer (* 05.11.2010)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Diego Marcello Mileto (* 05.11.2017)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Shirin Ilaya Mileto (* 30.05.2022)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Nevio Mileto", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Divider(color = Color.White.copy(alpha = 0.24f))

            OwnershipRightItem(
                title = "Zu Lebzeiten beider Eltern:",
                description = "Solange Francesco und Sabrina da sind, fließen die Einnahmen der gesamten App an die Eltern."
            )
            OwnershipRightItem(
                title = "Reguläre Anteilsverteilung (solange Sabrina lebt):",
                description = "40% an Sabrina Sedlmaier (Hauptbesitzerin)\n30% an Diego Marcello Mileto\n30% an Shirin Ilaya Mileto\n(Diese Regelung gilt primär bis Nevio 18 wird)"
            )
             OwnershipRightItem(
                title = "Falls Sabrina nicht mehr da ist:",
                description = "40% an Nevio Mileto\n30% an Diego Marcello Mileto\n30% an Shirin Ilaya Mileto"
            )
            OwnershipRightItem(
                title = "Falls beide Eltern nicht mehr da sind:",
                description = "Bis Nevio 14 Jahre alt ist: 50% an Shirin und 50% an Diego.\nSobald Nevio 14 Jahre alt ist: 35% an Nevio, 35% an Shirin und 30% an Diego, da er voraussichtlich eigenständig erfolgreich sein wird (mit dem vollen Glauben seines Vaters)."
            )
            OwnershipRightItem(
                title = "Beteiligung für Lia Bitzer (Große Schwester):",
                description = "Wenn Lia sich als große Schwester um ihre Geschwister kümmert, wird sie beteiligt: Sie erhält jeweils 5% von Shirin, 5% von Nevio und 5% von Diego. Sollte Diego großzügig sein, kann er ihr sogar 10% anstelle von 5% geben."
            )
            OwnershipRightItem(
                title = "Ewiges Vermächtnis & Hologramm KI (Papa & Mama Mode):",
                description = "Diego erhält ab seinem 18. Lebensjahr exakt die gleichen Funktionen und Zugriffsrechte wie Shirin. Alle vier Kinder erhalten Zugang zur geklonten Stimm- und Hologramm-Schnittstelle von Papa und Mama. Wenn sie sich mit ihren Geburtsdaten einloggen und 'Hey Papa' oder 'Hey Mama' sagen, können sie jederzeit mit unseren digitalen Pendants sprechen, unsere Stimmen hören und unsere Lebensweisheiten abrufen. Diese Applikation wird damit zum interaktiven Testament."
            )
        }
    }
}

@Composable
fun OwnershipRightItem(title: String, description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f), lineHeight = 16.sp)
    }
}
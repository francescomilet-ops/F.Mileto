package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.ShirinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: ShirinViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Search bar state
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    // Flutter-derived status states
    var statusMessage by remember { mutableStateOf("Bereit für Scan") }
    var isWarning by remember { mutableStateOf(false) }

    fun startCameraScanWithStatus() {
        val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.HARDWARE.contains("goldfish")

        if (isEmulator) {
            statusMessage = "Status: Kamera im Android-Simulator nicht verfügbar! Starte AI-Simulation..."
            isWarning = true
        } else {
            statusMessage = "Kamera wird auf dem Gerät gestartet..."
            isWarning = false
        }
    }

    // Camera Scan Selection States
    val scanCategories = listOf(
        ScanCategory("Steuerbelege", Icons.Filled.ReceiptLong, "Belege & Quittungen für EÜR einscannen"),
        ScanCategory("Straßen & Maps", Icons.Filled.Map, "Straßenschilder & Orte mit Live-Maps verbinden"),
        ScanCategory("Pflanzen & Insekten", Icons.Filled.LocalFlorist, "Blumen, Bäume, Ameisen, Käfer bestimmen"),
        ScanCategory("Tiere & Vögel", Icons.Filled.Pets, "Vögel, Haustiere, Insekten scannen"),
        ScanCategory("Alltag & Objekte", Icons.Filled.QrCodeScanner, "Lebensmittel, Möbel & Haushaltsdinge scannen")
    )
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    // Scanning State Simulation
    var isScanning by remember { mutableStateOf(false) }
    var scanResultData by remember { mutableStateOf<ScanResult?>(null) }
    var scanProgress by remember { mutableStateOf(0f) }

    var isScanningText by remember { mutableStateOf("Kompiliere Tiefenmatrix...") }

    // Hardware camera bitmap hook (when triggered)
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            // Autofill scan
            isScanning = true
            isScanningText = "Lese Bilddaten via KI-OCR..."
        }
    }

    // Pulse animation for simulated camera target
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Camera,
                contentDescription = "Camera Hub",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "F.M Beleg-Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Intelligente Belegprüfung § 4 Abs. 3 EStG & Alltagsforschung",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // SECTION: Beleg-Scanner & Export (Flutter UI Mirror)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("flutter_scanner_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Beleg-Scanner & Export",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Scanne deine Dokumente oder Belege direkt mit der Gerätekamera.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Simulated Interactive Status-Box
                val boxBgColor = if (isWarning) RoseAlert.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                val boxBorderColor = if (isWarning) RoseAlert else MaterialTheme.colorScheme.primary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(boxBgColor)
                        .border(1.5.dp, boxBorderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWarning) Icons.Filled.WarningAmber else Icons.Filled.Info,
                        contentDescription = "Status",
                        tint = if (isWarning) RoseAlert else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWarning) RoseAlert else MaterialTheme.colorScheme.primary
                    )
                }

                // Big main scanning button matching the Flutter ElevatedButton.icon
                Button(
                    onClick = {
                        startCameraScanWithStatus()
                        // Automatically open the camera or run simulated AI scan
                        if (!isScanning) {
                            isScanning = true
                            scanResultData = null
                            coroutineScope.launch {
                                delay(1200)
                                isScanningText = "Lese Bilddaten via KI-OCR..."
                                delay(1000)
                                scanResultData = getSimulatedScanResult(0, viewModel) // default to receipt
                                isScanning = false
                                statusMessage = "Beleg erfolgreich eingepflegt & verarbeitet!"
                                isWarning = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("flutter_scanner_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Scanner", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanner starten",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Section A: Google-Search
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_container_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Google Search",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Intelligente Google-Suche",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Hier suchen (z.B. Ameisenstraßen bekämpfen)...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, "Löschen")
                                }
                            }
                        }
                    )
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                isSearching = true
                                searchResult = null
                                coroutineScope.launch {
                                    delay(1500) // Simulated Google API fetch latency
                                    searchResult = getSimulatedGoogleSearch(searchQuery)
                                    isSearching = false
                                }
                            }
                        },
                        modifier = Modifier.testTag("google_search_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Suchen")
                    }
                }

                // Search Results display
                AnimatedVisibility(
                    visible = isSearching || searchResult != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        if (isSearching) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Durchsuche das Web mit Shirin...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text(
                                "Google Suchtreffer für: \"$searchQuery\"",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = searchResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { searchResult = null }) {
                                    Text("Ergebnis schließen", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section B: Beautiful Cybernetic Live Cam Simulation Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .testTag("camera_viewfinder_box"),
            contentAlignment = Alignment.Center
        ) {
            // Draw scanning animations or camera simulation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Grid background lines for robotic scanning aesthetic
                val gridSpacing = 40.dp.toPx()
                var x = 0f
                while (x < width) {
                    drawLine(
                        color = Color.Green.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += gridSpacing
                }

                var y = 0f
                while (y < height) {
                    drawLine(
                        color = Color.Green.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }

                // Pulse red/green targets in center
                drawCircle(
                    color = Color.Red.copy(alpha = 0.1f),
                    center = Offset(width / 2, height / 2),
                    radius = 80.dp.toPx()
                )

                // Laser scanline animation
                val laserY = height * laserOffset
                drawLine(
                    color = Color.Green,
                    start = Offset(0f, laserY),
                    end = Offset(width, laserY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Text status inside Camera viewfinder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header of overlay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Green,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "LIVE DETECTOR MODUS - 1080P",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = scanCategories[selectedCategoryIndex].title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }

                // Dynamic scanning layout
                if (isScanning) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressIndicator(color = Color.Green, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = isScanningText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = scanCategories[selectedCategoryIndex].icon,
                            contentDescription = "Scan Icon",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[ Kamera simulieren / Zielen ]",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Richte dein Objekt zentral in das Visier",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }

                // Viewfinder bottom prompt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "ISO 400 - F2.4",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                cameraLauncher.launch(null)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Filled.PhotoCamera, "Hardware Kamera", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Quick Scan Category Tabs
        Column {
            Text(
                "1. Scanziel-Kategorie wählen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scanCategories.take(3).forEachIndexed { index, cat ->
                    val isSelected = selectedCategoryIndex == index
                    OutlinedCard(
                        onClick = {
                            selectedCategoryIndex = index
                            scanResultData = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_cat_tab_$index"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                cat.icon,
                                contentDescription = cat.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                cat.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scanCategories.drop(3).forEachIndexed { innerIndex, cat ->
                    val index = innerIndex + 3
                    val isSelected = selectedCategoryIndex == index
                    OutlinedCard(
                        onClick = {
                            selectedCategoryIndex = index
                            scanResultData = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_cat_tab_$index"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                cat.icon,
                                contentDescription = cat.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                cat.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Capture Action Trigger Button
        Button(
            onClick = {
                if (!isScanning) {
                    isScanning = true
                    scanResultData = null
                    coroutineScope.launch {
                        isScanningText = "Analysiere Fokuspunkt..."
                        delay(600)
                        isScanningText = "Ermittle geologische Merkmale..."
                        delay(600)
                        isScanningText = "Gegenprobe mit globaler Bio- & Kartenmatrix..."
                        delay(600)

                        // Build corresponding Scan Results
                        scanResultData = getSimulatedScanResult(selectedCategoryIndex, viewModel)
                        isScanning = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_scan_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, "Scan")
            Spacer(modifier = Modifier.width(10.dp))
            Text("KI Live-Scan Auslösen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Section C: Scan Results visual cards
        AnimatedVisibility(
            visible = scanResultData != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            scanResultData?.let { res ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_results_display_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isReceipt) EmeraldAlert.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (res.isReceipt) EmeraldAlert.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    res.typeIcon,
                                    contentDescription = "Result Category",
                                    tint = if (res.isReceipt) EmeraldAlert else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.identifiedName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Match Score
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (res.isReceipt) EmeraldAlert.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Konfidenz: ${res.confidence}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isReceipt) EmeraldAlert else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Text(
                            text = res.scientificProfile,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Special action or results details
                        res.nestedDetailsCard()

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions (Integrate to database, open maps, share)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (res.isReceipt) {
                                Button(
                                    onClick = {
                                        res.onIntegrateDb?.invoke()
                                        scanResultData = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_action_integrate"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAlert)
                                ) {
                                    Icon(Icons.Filled.AccountBalanceWallet, "Sparen", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("In Steuer einpflegen", fontSize = 11.sp, maxLines = 1)
                                }
                            } else if (res.isMap) {
                                Button(
                                    onClick = {
                                        res.onOpenMaps?.invoke()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_action_maps"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Filled.Directions, "Maps", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("In Maps öffnen", fontSize = 11.sp, maxLines = 1)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scanResultData = null
                                },
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Schließen", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data structures
data class ScanCategory(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String
)

data class ScanResult(
    val id: Int,
    val identifiedName: String,
    val confidence: Int,
    val scientificProfile: String,
    val isReceipt: Boolean = false,
    val isMap: Boolean = false,
    val typeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val nestedDetailsCard: @Composable () -> Unit,
    val onIntegrateDb: (() -> Unit)? = null,
    val onOpenMaps: (() -> Unit)? = null
)

// Helper simulators
private fun getSimulatedGoogleSearch(query: String): String {
    val q = query.lowercase()
    return when {
         q.contains("ameise") || q.contains("ant") -> "✓ Shirin Web-Recherche: Die Bekämpfung von Ameisen im Haus gelingt am ökologischsten mithilfe von natürlichen Geruchsbarrieren wie Zimt, Thymian, Teebaumöl oder Lavendelkraut. Diese ätherischen Öle verwirren die Phermon-Richtungssinne der Späherameisen komplett, womit die Ameisenstraße sofort kollabiert. Für die Steuer sind chemische Ungezieferbekämpfer unter haushaltsnahen Dienstleistungen zu 20% direkt abziehbar."
         q.contains("steuer") || q.contains("eür") -> "✓ Shirin Web-Recherche: Die Einnahmenüberschussrechnung (EÜR) nach § 4 Abs. 3 EStG ist eine einfache Gewinnermittlungsart für Freiberufler & Kleingewerbe. Steuerfreie Belege müssen getrennt aufbewahrt werden. Belege im Ausland (z.B. Umsatzsteuer-Vergütungsverfahren im EU-Ausland) können komplett über das Bundeszentralamt für Steuern (BZSt) deklariert werden."
         q.contains("gericht") || q.contains("gerichtshilfe") -> "✓ Shirin Web-Recherche: Im Falle eines Gerichtsverfahrens in Deutschland können einkommensschwache Bürger 'Prozesskostenhilfe' (PKH) oder 'Verfahrenskostenhilfe' beantragen. Die dazugehörigen Formulare zur Versicherung der wirtschaftlichen Verhältnisse können direkt im Bereich 'Vault & Ämter' dieser App geladen und ausgefüllt werden."
         q.contains("kindergeld") -> "✓ Shirin Web-Recherche: Kindergeld wird in Deutschland bei der Familienkasse der Agentur für Arbeit beantragt. Seit 2023 beträgt das Kindergeld einheitlich 250 Euro pro Kind. Anträge stehen ab sofort in der Ämter-Bibliothek zum Download bereit."
         q.contains("jobcenter") -> "✓ Shirin Web-Recherche: Bürgergeldberechnung berücksichtigt Kosten der Unterkunft (Miete, Heizung). Der Hauptantrag auf Bürgergeld sowie Weiterbewilligungsanträge (WBA) sind direkt über die App-Bibliothek per PDF ausfüllbar."
         else -> "✓ Shirin Web-Recherche: Ein intelligenter Live-Abgleich der Begriffe zeigt einen direkten Zusammenhang mit deutschem Verwaltungsabläufen und Alltagsregularien. Shirin stellt hierzu passende Leitfäden, steuerlich absetzbare Abschreibungsregeln (Standard-Afa-Tabellen) sowie offizielle Downloads im Ämter-Katalog bereit."
    }
}

private fun getSimulatedScanResult(categoryIndex: Int, viewModel: ShirinViewModel): ScanResult {
    return when (categoryIndex) {
        0 -> { // Belege
            ScanResult(
                id = 101,
                identifiedName = "Steuerbeleg / Quittung (Deutschland)",
                confidence = 98,
                scientificProfile = "Erkannter Beleg: REWE Supermarkt (Bürobedarf & Bewirtung) vom aktuellen Tag. MwSt-Anteil von 19% und 7% wurde automatisch extrahiert.",
                isReceipt = true,
                typeIcon = Icons.Filled.Receipt,
                nestedDetailsCard = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Extrahierte Rohdaten:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("• Empfänger: REWE Markt GmbH, Köln", style = MaterialTheme.typography.bodySmall)
                            Text("• Bruttobetrag: 45,90 €", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("• Steueranteil (19%): 7,33 €", style = MaterialTheme.typography.bodySmall)
                            Text("• Kategorie: Bürobedarf / Bewirtung", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                onIntegrateDb = {
                    viewModel.addTransaction(
                        title = "REWE Beleg (Bürobedarf)",
                        amount = 45.90,
                        isIncome = false,
                        category = "Büro",
                        notes = "Gescannt via AI-Kamera OCR"
                    )
                }
            )
        }
        1 -> { // Straßen Maps
            ScanResult(
                id = 102,
                identifiedName = "Königsallee, Düsseldorf",
                confidence = 95,
                scientificProfile = "Karten- und Bilderkennung klassifiziert diesen Standort als Königsallee in 40212 Düsseldorf. Bekannt als exklusiver Boulevard, verknüpft mit umliegenden Wirtschaftsberatern.",
                isMap = true,
                typeIcon = Icons.Filled.MyLocation,
                nestedDetailsCard = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Google Maps Live-Anbindung:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Red).padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("PROJEKTION", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 8.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("📍 GPS: 51.2227° N, 6.7786° E", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Straße: Königsallee (Stadtmitte)", style = MaterialTheme.typography.bodySmall)
                            Text("Verkehrsanschluss: U-Bahn Steinstraße/Königsallee (120m)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                onOpenMaps = {
                    // Simulates opening google maps link by sending feedback in dialogue / toast format
                }
            )
        }
        2 -> { // Pflanzen & Insekten
            ScanResult(
                id = 103,
                identifiedName = "Rote Waldameise (Formica rufa)",
                confidence = 92,
                scientificProfile = "Biologische Erkennung: Ameisenvölker der Gattung Formica rufa. Wichtige nützliche Waldinsekten. Unter strengem Bundesnaturschutz in Deutschland.",
                isReceipt = false,
                typeIcon = Icons.Filled.Eco,
                nestedDetailsCard = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Lexikon-Eintrag:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("• Klasse: Insecta (Insekten)", style = MaterialTheme.typography.bodySmall)
                            Text("• Ordnung: Hautflügler (Hymenoptera)", style = MaterialTheme.typography.bodySmall)
                            Text("• Familie: Ameisen (Formicidae)", style = MaterialTheme.typography.bodySmall)
                            Text("• Giftigkeit: Ungiftig, spritzt Ameisensäure bei Bedrohung", style = MaterialTheme.typography.bodySmall)
                            Text("• Hinweis: Ameisenstraßen im Garten deuten auf Blattlausbefall hin.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            )
        }
        3 -> { // Tiere Vögel
            ScanResult(
                id = 104,
                identifiedName = "Blaumeise (Cyanistes caeruleus)",
                confidence = 97,
                scientificProfile = "Ornithologische Erkennung: Cyanistes caeruleus (Klasse Aves). Dieser hübsche europäische Singvogel ernährt sich hauptsächlich von Insekten & Pflanzensamen.",
                isReceipt = false,
                typeIcon = Icons.Filled.Pets,
                nestedDetailsCard = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Vogel-Charakteristika:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("• Gesang: Helles 'tsi-tsi-tsurrr'", style = MaterialTheme.typography.bodySmall)
                            Text("• Schutzstatus: Besonders geschützt in Deutschland", style = MaterialTheme.typography.bodySmall)
                            Text("• Nutzen im Garten: Vertilgt schädliche Raupen von Bäumen", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            )
        }
        else -> { // Alltag & Sonstige
            ScanResult(
                id = 105,
                identifiedName = "Lebensmittel: Bio-Bruchmüsli",
                confidence = 94,
                scientificProfile = "Nährwert-Zusammensetzung & Scanergebnis für Bio Hafer-Vollkornmüsli mit Früchten. Ausgezeichnete Vollkorn-Ballaststoffquelle.",
                isReceipt = false,
                typeIcon = Icons.Filled.SettingsOverscan,
                nestedDetailsCard = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Nährwerte pro 100g:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("• Energie: 365 kcal / 1530 kJ", style = MaterialTheme.typography.bodySmall)
                            Text("• Ballaststoffe: 9.8 g", style = MaterialTheme.typography.bodySmall)
                            Text("• Proteine: 11.2 g", style = MaterialTheme.typography.bodySmall)
                            Text("• Zuckeranteil: Gering (ohne Zuckerzusatz)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            )
        }
    }
}

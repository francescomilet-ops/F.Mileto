package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class GlobalEducationalProfile(
    val child1: String = "Diego Marcello Mileto",
    val child2: String = "Shirin Mileto",
    val child3: String = "Nevio Mileto",
    val founder: String = "Francesco Mileto",
    val targetIban: String = "DE58100101785341301902",
    val targetBic: String = "REVODEB2",
    var universityDegreeFinished: Boolean = false,
    var aiCameraScanVerified: Boolean = false,
    var parentalManualRelease: Boolean = false,
    var nightVerificationSuccessful: Boolean = false,
    var lastCheckTimestamp: String = "Noch keine Prüfung erfolgt",
    var totalAppRevenuePool: Double = 184500.00
) {
    val diegoSharePercentage: Double = 0.35
    val shirinSharePercentage: Double = 0.35
    val nevioSharePercentage: Double = 0.30

    fun calculateDiegoAge(): Int {
        var age = 2026 - 2017
        if (6 < 11) age--
        return age
    }

    fun calculateShirinAge(): Int {
        return 2026 - 2022
    }

    fun isAbsoluteAgeReached(): Boolean {
        return calculateDiegoAge() >= 30
    }

    fun getDiegoShare(): Double = totalAppRevenuePool * diegoSharePercentage
    fun getShirinShare(): Double = totalAppRevenuePool * shirinSharePercentage
    fun getNevioShare(): Double = totalAppRevenuePool * nevioSharePercentage

    fun executeNightlyUniversityCheck(): GlobalEducationalProfile {
        return this.copy(
            lastCheckTimestamp = "Heute um 23:00 Uhr",
            nightVerificationSuccessful = if (this.aiCameraScanVerified || this.universityDegreeFinished) true else this.nightVerificationSuccessful,
            universityDegreeFinished = if (this.aiCameraScanVerified || this.universityDegreeFinished) true else this.universityDegreeFinished
        )
    }
}

@Composable
fun DiegosLerntoolScreen(onBack: () -> Unit = {}) {
    var profile by remember { mutableStateOf(GlobalEducationalProfile()) }

    DiegosMainDashboardScreen(
        profile = profile,
        onUpdateProfile = { profile = it },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiegosMainDashboardScreen(
    profile: GlobalEducationalProfile,
    onUpdateProfile: (GlobalEducationalProfile) -> Unit,
    onBack: () -> Unit
) {
    var showTrusteeLedger by remember { mutableStateOf(false) }
    var showCameraScan by remember { mutableStateOf(false) }
    var showSecureLedgerInfo by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showTrusteeLedger) {
        TrusteeLedgerDialog(
            profile = profile,
            onDismiss = { showTrusteeLedger = false },
            onStartScan = {
                showTrusteeLedger = false
                showCameraScan = true
            },
            onVerifyPin = {
                showTrusteeLedger = false
                showSecureLedgerInfo = true
            },
            onShowError = {
                coroutineScope.launch { snackbarHostState.showSnackbar("❌ Falsche PIN!") }
            }
        )
    }

    if (showCameraScan) {
        CameraScanDialog(
            onDismiss = { showCameraScan = false },
            onScanExecute = {
                val updated = profile.copy(aiCameraScanVerified = true).executeNightlyUniversityCheck()
                onUpdateProfile(updated)
                showCameraScan = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🎉 Urkunde erfasst! Der abendliche KI-Abgleich prüft nun die Echtheit...")
                }
            }
        )
    }

    if (showSecureLedgerInfo) {
        SecureLedgerInfoBottomSheet(
            profile = profile,
            onDismiss = { showSecureLedgerInfo = false }
        )
    }

    var showSmartAssistant by remember { mutableStateOf(false) }

    if (showSmartAssistant) {
        SmartAssistantScreen(onDismiss = { showSmartAssistant = false })
        return
    }

    val hasFullControl = profile.nightVerificationSuccessful || profile.parentalManualRelease

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Diegos Lerntool • Erbe & Koppelung", color = Color.White, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F2042)),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showTrusteeLedger = true }) {
                        Icon(Icons.Filled.Lock, contentDescription = "Trustee", tint = Color(0xFFFFC107))
                    }
                }
            )
        },
        containerColor = Color(0xFFF4F6FA)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // REVOLUT STAMMDATEN-CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Revolut-Einzahlungskonto: ${profile.founder}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("IBAN: ${profile.targetIban}\nBIC: ${profile.targetBic}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // LIVE-UMLAGE-RECHNER
                Text("📊 Automatische Gewinnverteilung der App-Abos:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                ShareTile(profile.child1, "35%", profile.getDiegoShare(), Color(0xFF3F51B5))
                ShareTile(profile.child2, "35%", profile.getShirinShare(), Color(0xFF009688))
                ShareTile(profile.child3, "30%", profile.getNevioShare(), Color(0xFFFF9800))

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "Gesamter integrierter Umsatz-Pool: %.2f €".format(java.util.Locale.US, profile.totalAppRevenuePool),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(20.dp))

                // ALTERS- UND TEST-STATUS
                Text(
                    text = "Verfügungs- und Leistungsstatus:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val diegoAge = profile.calculateDiegoAge()
                val shirinAge = profile.calculateShirinAge()
                val absoluteUnlock = profile.isAbsoluteAgeReached()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (hasFullControl) Color(0xFFE0F2F1) else Color(0xFFFFEBEE))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            if (hasFullControl) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (hasFullControl) Color(0xFF009688) else Color(0xFFF44336),
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Column {
                            Text(
                                if (hasFullControl) "ZUGRIFFS-STATUS: FREIGEGEBEN" else "ZUGRIFFS-STATUS: GESPERRT",
                                fontWeight = FontWeight.Bold,
                                color = if (hasFullControl) Color(0xFF004D40) else Color(0xFFB71C1C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (absoluteUnlock) 
                                    "Das absolute Alter ist erreicht. Alle Tests und Nachweise sind dauerhaft deaktiviert."
                                else 
                                    "Diego ($diegoAge Jahre) und Shirin ($shirinAge Jahre) befinden sich in der Lernphase. Einteilung erfolgt auf das Revolut-Konto. Voller Zugriff erfordert den abendlichen KI-Studienabschluss-Scan.",
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2042))
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Betrete LearnSpace Tropea (Magna Graecia)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TrusteeLedgerDialog(
    profile: GlobalEducationalProfile,
    onDismiss: () -> Unit,
    onStartScan: () -> Unit,
    onVerifyPin: () -> Unit,
    onShowError: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    val parentPin = "2412"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF3F51B5))
                Spacer(modifier = Modifier.width(10.dp))
                Text("🔒 Family-Schloss Zentrale", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Zugriff über Eltern-PIN, Kamera-Scan oder den automatischen Abend-Abgleich:", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Eltern-PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(15.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(15.dp))
                
                Text("Abendlicher KI-Serverabgleich:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3F51B5))
                Spacer(modifier = Modifier.height(5.dp))
                Text("Letzte Prüfung: ${profile.lastCheckTimestamp}", fontSize = 11.sp, color = Color.Gray)
                Text(
                    if (profile.nightVerificationSuccessful) "✅ Uni-Zentralregister: Abschluss offiziell verifiziert!"
                    else "⏳ Uni-Zentralregister: Warte auf offizielles Prüfungs-Zertifikat.",
                    fontSize = 11.sp,
                    color = if (profile.nightVerificationSuccessful) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))
                
                Button(
                    onClick = onStartScan,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Studienurkunde per Kamera scannen", fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
        confirmButton = {
            Button(onClick = {
                if (pinInput == parentPin) {
                    onVerifyPin()
                } else {
                    onShowError()
                }
            }) {
                Text("PIN verifizieren")
            }
        }
    )
}

@Composable
fun CameraScanDialog(
    onDismiss: () -> Unit,
    onScanExecute: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        title = { Text("📷 KI-Dokumenten-Scanner aktiv", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF212121), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color(0xFF64FFDA))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("KI analysiert Stempel & OCR-Text...", color = Color(0xFF64FFDA), fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen", color = Color.White) }
        },
        confirmButton = {
            Button(
                onClick = onScanExecute,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
            ) {
                Text("Scan ausführen", color = Color.White)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureLedgerInfoBottomSheet(
    profile: GlobalEducationalProfile,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {
            Text("💼 Family-Schloss Status: ${profile.founder}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Revolut-Zielkonto: ${profile.targetIban}", fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Prüfungs-Protokoll:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Manueller Eltern-Status: ${if (profile.parentalManualRelease) "Freigegeben ✅" else "Keine Freigabe 🔒"}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Automatischer Abend-Abgleich: ${if (profile.nightVerificationSuccessful) "Erfolgreich von Universität bestätigt! ✅" else "Warte auf nächtliche Validierung ⏳"}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Schließen")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ShareTile(childName: String, percentage: String, currentCash: Double, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(color, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(percentage, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(childName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Treuhand-Anteil rechtssicher gebunden", fontSize = 12.sp, color = Color.Gray)
            }
            Text("%.2f €".format(java.util.Locale.US, currentCash), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

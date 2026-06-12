package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class PfaJob(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val budget: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PfaWorkersScreen(
    onClose: () -> Unit
) {
    var showDirectLeadDialog by remember { mutableStateOf(false) }
    var showPostJobDialog by remember { mutableStateOf(false) }
    val jobs = remember { mutableStateListOf<PfaJob>() }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PFA.workers & MyHammer Portal", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            },
            containerColor = Color(0xFF0F172A),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showPostJobDialog = true },
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Auftrag einstellen")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    // Die Bibliothek – Deine Firma
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Business, contentDescription = "Firma", tint = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PFA.workers", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Pfaffenhofens Experten für exklusiven Innenausbau & Trockenbau.",
                                color = Color.White.copy(0.8f),
                                fontSize = 14.sp
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Text("📍 Pfaffenhofen an der Ilm / Göbelsbach", color = Color.White, fontSize = 14.sp)
                            Text("🛠️ Trockenbau (Q1-Q4), Innenausbau, LED-Lichtdesign, Smart Home Vorbereitung", color = Color.White, fontSize = 14.sp)
                            
                            Button(
                                onClick = { showDirectLeadDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = "Kontakt", tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Direkt bei PFA.workers anfragen", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Auftrags-Ausschreibungen (MyHammer-Style)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                if (jobs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Keine aktiven Aufträge gefunden.", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    items(jobs) { job ->
                        JobCardItem(job)
                    }
                }
            }
        }

        if (showDirectLeadDialog) {
            var name by remember { mutableStateOf("") }
            var gewerk by remember { mutableStateOf("") }
            var details by remember { mutableStateOf("") }
            var leadSent by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showDirectLeadDialog = false },
                title = { Text(if (leadSent) "Erfolgreich!" else "Lead an PFA.workers senden") },
                text = {
                    if (leadSent) {
                        Text("Ihre Anfrage wurde direkt an PFA.workers übermittelt. Wir melden uns kurzfristig!")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name / Kontakt") }, singleLine = true)
                            OutlinedTextField(value = gewerk, onValueChange = { gewerk = it }, label = { Text("Gewerk (z.B. Trockenbau)") }, singleLine = true)
                            OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Projektbeschreibung") }, minLines = 3)
                        }
                    }
                },
                confirmButton = {
                    if (leadSent) {
                        Button(onClick = { showDirectLeadDialog = false }) { Text("Schließen") }
                    } else {
                        Button(onClick = { leadSent = true }) { Text("Senden") }
                    }
                },
                dismissButton = {
                    if (!leadSent) {
                        TextButton(onClick = { showDirectLeadDialog = false }) { Text("Abbrechen") }
                    }
                }
            )
        }

        if (showPostJobDialog) {
            var title by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("") }
            var budget by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showPostJobDialog = false },
                title = { Text("Ausschreibung erstellen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("🔒 Premium-Feature", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titel") }, singleLine = true)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Gewerk (Fliesenleger, Elektro...)") }, singleLine = true)
                        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ort (PLZ / Stadt)") }, singleLine = true)
                        OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (€)") }, singleLine = true)
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Beschreibung") }, minLines = 3)
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        jobs.add(PfaJob(
                            id = "job_${System.currentTimeMillis()}",
                            title = title.takeIf { it.isNotBlank() } ?: "Neues Projekt",
                            category = category.takeIf { it.isNotBlank() } ?: "Allgemein",
                            location = location.takeIf { it.isNotBlank() } ?: "Unbekannt",
                            budget = budget.takeIf { it.isNotBlank() } ?: "Nach Absprache",
                            description = desc
                        ))
                        showPostJobDialog = false 
                    }) {
                        Text("Posten")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPostJobDialog = false }) { Text("Abbrechen") }
                }
            )
        }
    }
}

@Composable
fun JobCardItem(job: PfaJob) {
    var showApplyDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(job.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(color = Color(0xFF334155), shape = RoundedCornerShape(4.dp)) {
                    Text(job.category, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text("📍 ${job.location} | 💰 ${job.budget} €", color = Color.White.copy(0.7f), fontSize = 14.sp)
            Text(job.description, color = Color.White, fontSize = 14.sp)
            
            Button(
                onClick = { showApplyDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Angebot abgeben (Premium)", color = Color.White)
            }
        }
    }

    if (showApplyDialog) {
        var price by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var applySuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text(if (applySuccess) "Angebot übermittelt!" else "Angebot abgeben") },
            text = {
                if (applySuccess) {
                    val priceVal = price.toDoubleOrNull() ?: 0.0
                    val fee = if (priceVal >= 1000.0) 10 else 5
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dein Angebot wurde sicher an den Kunden übermittelt!")
                        Surface(color = Color(0xFF334155), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💡 Transparenz-Info (Provision)", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sobald der Kunde dein Angebot annehmen sollte, wird eine einmalige Portal-Gebühr für die erfolgreiche Vermittlung fällig:\n\nDein Gebot: $price €\nStatus: Großauftrag (>= 1.000€) = 10€, sonst 5€\nFällige Gebühr bei Erfolg: $fee €", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Nur für verifizierte Premium-Handwerker.", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Dein Angebotspreis (€)") }, singleLine = true)
                        
                        val currentPrice = price.toDoubleOrNull() ?: 0.0
                        if (currentPrice > 0) {
                            val expectedFee = if (currentPrice >= 1000.0) 10 else 5
                            Text("Voraussichtl. Vermittlungsgebühr: $expectedFee €", color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Nachricht an Kunden") }, minLines = 3)
                    }
                }
            },
            confirmButton = {
                if (applySuccess) {
                    Button(onClick = { showApplyDialog = false }) { Text("Schließen") }
                } else {
                    Button(onClick = { applySuccess = true }) { Text("Angebot absenden") }
                }
            },
            dismissButton = {
                if (!applySuccess) {
                    TextButton(onClick = { showApplyDialog = false }) { Text("Abbrechen") }
                }
            }
        )
    }
}

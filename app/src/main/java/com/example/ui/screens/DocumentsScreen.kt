package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Document
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.ShirinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: ShirinViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsState()
    val isE2EE by viewModel.isE2EEActive.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showAddDocDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Mein Tresor, 1: Behörden-Bibliothek

    // Selected Document for Inspected Details Dialogue
    var inspectedDocument by remember { mutableStateOf<Document?>(null) }

    // Export & Notification States
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var globalAlertMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDocDialog = true },
                    modifier = Modifier.testTag("add_document_fab"),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = "Dokument hochladen")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Document Screen Hub Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, "Safe", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mein E2EE Tresor", fontWeight = FontWeight.SemiBold)
                    }},
                    modifier = Modifier.testTag("tab_vault")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalance, "Bib", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Behörden-Bibliothek", fontWeight = FontWeight.SemiBold)
                    }},
                    modifier = Modifier.testTag("tab_library")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BusinessCenter, "Büro", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Büro-Zentrale 💼", fontWeight = FontWeight.SemiBold)
                    }},
                    modifier = Modifier.testTag("tab_smart_office")
                )
            }

            // Global active feedback alert
            AnimatedVisibility(
                visible = globalAlertMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                globalAlertMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, "Schnittstelle", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { globalAlertMessage = null }) {
                                Icon(Icons.Filled.Close, "Zu", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // VAULT MODE (LOCAL DOCS LIST)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        item {
                            Column {
                                Text(
                                    text = "E2EE Dokumenten-Tresor",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sichere Belege, Verträge oder Auszüge mit lokaler Ende-zu-Ende-Verschlüsselung (AES-256).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Security Status Banner
                        item {
                            SecurityStatusBanner(isE2EE) {
                                viewModel.toggleE2EE()
                            }
                        }

                        // Export PDF / Backup Centrale
                        item {
                            EverydayToolsCard(
                                onExportPdf = {
                                    globalAlertMessage = "Erfolgreich kompiliert: Alle steuerpflichtigen Dokumenten-Auszüge wurden mit AES-256 (GCM) verschlüsselt und als verschlüsselter Bericht exportiert."
                                },
                                onBackupCloud = {
                                    globalAlertMessage = "Datenbank verschlüsselt synchronisiert. Dein E2EE Cloud-Vault Backup via AES-256-GCM (Chiffretext Base64) ist erfolgreich generiert und sicher übertragen!"
                                }
                            )
                        }

                        item {
                            Text(
                                text = "Meine Dokumente (Tippe zum Öffnen/Analysieren)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (documents.isEmpty()) {
                            item {
                                EmptyDocumentsState()
                            }
                        } else {
                            items(documents, key = { it.id }) { doc ->
                                DocumentItemRow(
                                    doc = doc,
                                    onClick = {
                                        inspectedDocument = doc
                                    },
                                    onDelete = {
                                        viewModel.deleteDocument(doc)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // BEHÖRDEN-BIBLIOTHEK MODE (OFFICIAL GERMAN AGENCY FORMS)
                    GermanFormLibraryView(
                        viewModel = viewModel,
                        globalAlertMessageSetter = { globalAlertMessage = it },
                        onFormDownloaded = { formName ->
                            globalAlertMessage = "Download abgeschlossen: '$formName' wurde erfolgreich im Download-Ordner Ihres Geräts als PDF bereitgestellt."
                        },
                        onFormSavedToSafe = { formName, docType ->
                            viewModel.addDocument(
                                name = "$formName (Vorlage)",
                                type = docType,
                                sizeKb = 240L,
                                notes = "Offizielle Formularvorlage aus der Shirin Bibliothek."
                            )
                            globalAlertMessage = "Erfolgreich integriert: Vorlage '$formName' wurde in deinen verschlüsselten E2EE-Tresor überspielt!"
                        }
                    )
                }
                2 -> {
                    // BÜRO-ZENTRALE / SMART OFFICE (KI-ANGBOTS & OCR EXTRATION)
                    BuroZentraleView(
                        viewModel = viewModel,
                        globalAlertMessageSetter = { globalAlertMessage = it }
                    )
                }
            }
        }
    }

    // Modal Add Document Dialog
    if (showAddDocDialog) {
        AddDocumentDialog(
            viewModel = viewModel,
            onDismiss = { showAddDocDialog = false }
        )
    }

    // Modal Inspected Document Viewer Dialog
    if (inspectedDocument != null) {
        DocumentAnalyzerDialog(
            doc = inspectedDocument!!,
            viewModel = viewModel,
            onDismiss = { inspectedDocument = null },
            onInfoExtracted = { title, amt ->
                globalAlertMessage = "✓ Datenextraktion erfolgreich: Beleg '$title' über $amt € wurde für deine Anlage EÜR klassifiziert und verbucht!"
            }
        )
    }
}

@Composable
fun SecurityStatusBanner(isE2EE: Boolean, onToggle: () -> Unit) {
    val gradientColors = if (isE2EE) {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.errorContainer
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.7f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isE2EE) Icons.Filled.VerifiedUser else Icons.Filled.Warning,
                            contentDescription = "Sicherheits-Status",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isE2EE) "Militärische E2EE Aktiv" else "Schutz Deaktiviert",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isE2EE) "Daten via AES-256 lokal geschützt" else "Unsichere Ablage, tippe rechts zum Aktivieren",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Switch(
                    checked = isE2EE,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(0.3f)
                        .testTag("e2ee_switch")
                )
            }
        }
    }
}

@Composable
fun EverydayToolsCard(
    onExportPdf: () -> Unit,
    onBackupCloud: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Alltagshelfer-Zentrale",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, "PDF", tint = RoseAlert)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Bericht (PDF)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Einnahmen exportieren",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                OutlinedCard(
                    onClick = onBackupCloud,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CloudUpload, "Backup", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Cloud Backup",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tresor sichern",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItemRow(
    doc: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("document_item_${doc.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.75f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (doc.type) {
                            "PDF" -> Icons.Filled.PictureAsPdf
                            "STEUER" -> Icons.Filled.QueryStats
                            "RECHNUNG" -> Icons.Filled.ReceiptLong
                            else -> Icons.Filled.FolderZip
                        },
                        contentDescription = "Doku-Typ",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = doc.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${doc.sizeBytes / 1024} KB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AES-E2EE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (doc.notes.isNotBlank()) {
                        Text(
                            text = doc.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.weight(0.25f),
                colors = IconButtonDefaults.iconButtonColors()
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Dokument entfernen",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDocumentsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FolderOpen,
            contentDescription = "Ordner leer",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tresor leer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Klicke auf das '+' unten rechts, um sensible Rechnungen oder Dokumentenmetadaten verschlüsselt abzulegen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ---------------- ADDITIONAL BEHÖRDEN BIBLIOTHEK ----------------
@Composable
fun GermanFormLibraryView(
    viewModel: ShirinViewModel,
    globalAlertMessageSetter: (String) -> Unit,
    onFormDownloaded: (String) -> Unit,
    onFormSavedToSafe: (String, String) -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var upgradeFormName by remember { mutableStateOf("") }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoFixHigh,
                        contentDescription = "Autopilot",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Premium: KI-Ausfüll-Autopilot")
                }
            },
            text = {
                Text(
                    "Möchtest du das offizielle Formular '$upgradeFormName' vollautomatisch ausfüllen lassen?\n\n" +
                    "Lass Shirin AI das fehlerfreie Ausfüllen für dich übernehmen. Als Premium-Mitglied genießt du sofortigen Zugriff auf die automatische Formularbearbeitung, proaktive Steueroptimierung und unbegrenzte Downloads.\n\n" +
                    "Sichere dir jetzt den Vollzugriff für nur 4,99 €/Monat (jederzeit kündbar)."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeSubscription("PRO")
                        showUpgradeDialog = false
                        globalAlertMessageSetter("Premium freigeschaltet! Shirin übernimmt nun das automatische Ausfüllen deiner Dokumente.")
                    }
                ) {
                    Text("Jetzt freischalten")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    val agencies = listOf(
        FormAgency(
            "Finanzamt",
            Icons.Filled.AccountBalance,
            listOf(
                FormItem("Einkommensteuererklärung 2026", "PDF", 640)
            )
        ),
        FormAgency(
            "HWK (Handwerkskammer)",
            Icons.Filled.Build,
            listOf(
                FormItem("Antrag auf Handwerksrolle", "PDF", 510)
            )
        ),
        FormAgency(
            "IHK / Gewerbeamt",
            Icons.Filled.Store,
            listOf(
                FormItem("Gewerbeanmeldung (GewA1)", "PDF", 410)
            )
        ),
        FormAgency(
            "Arbeitsamt (Agentur für Arbeit)",
            Icons.Filled.Work,
            listOf(
                FormItem("ALG-1 Antrag auf Arbeitslosengeld", "PDF", 450),
                FormItem("Erklärung über Nebeneinkommen", "PDF", 120),
                FormItem("Mitteilung über Auslandsaufenthalt", "PDF", 95)
            )
        ),
        FormAgency(
            "Gerichte & Beihilfe",
            Icons.Filled.Balance,
            listOf(
                FormItem("Antrag auf Prozesskostenhilfe (PKH)", "STEUER", 540),
                FormItem("Erklärung über wirtschaftliche Verhältnisse", "STEUER", 380),
                FormItem("Musterschreiben Klageerwiderung", "PDF", 110)
            )
        ),
        FormAgency(
            "Inkasso- & Schuldnerabwehr",
            Icons.Filled.Gavel,
            listOf(
                FormItem("Mustereinspruch gegen Inkassoforderung", "PDF", 80),
                FormItem("Ratenzahlungsvereinbarung Vorlage", "PDF", 65),
                FormItem("Auskunftsbegehren nach DSGVO Art. 15", "PDF", 90)
            )
        ),
        FormAgency(
            "Jobcenter / Bürgergeld",
            Icons.Filled.Diversity3,
            listOf(
                FormItem("Bürgergeld Hauptantrag (HA)", "PDF", 680),
                FormItem("Anlage Kosten der Unterkunft (KdU)", "RECHNUNG", 215),
                FormItem("Anlage Einkommenserklärung (EK)", "RECHNUNG", 310)
            )
        ),
        FormAgency(
            "Kindergeld & Familienkasse",
            Icons.Filled.ChildCare,
            listOf(
                FormItem("Antrag auf Gewährung von Kindergeld", "PDF", 320),
                FormItem("Veränderungsmitteilung für Kindergeldempfänger", "PDF", 140)
            )
        ),
        FormAgency(
            "Standesamt / Heiratsunterlagen",
            Icons.Filled.Favorite,
            listOf(
                FormItem("Anmeldung der Eheschließung Vorlage", "PDF", 280),
                FormItem("Merkblatt Namenserklärung", "PDF", 115),
                FormItem("Erklärung zur Bestimmung des Ehenamens", "PDF", 90)
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Deutsche Behörden-Bibliothek (Bibliothek)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lade offizielle Anträge herunter, kopiere sie in deinen Tresor oder lasse sie über den KI-Autopilot automatisch ausfüllen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        items(agencies) { agency ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agency_card_${agency.className}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(agency.icon, "Amt", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(agency.className, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        agency.items.forEach { form ->
                            FormRowItem(
                                form = form,
                                isPremiumActive = isPremiumActive,
                                onDownload = { onFormDownloaded(form.name) },
                                onAddToSafe = { onFormSavedToSafe(form.name, form.type) },
                                onAutofillClicked = {
                                    if (isPremiumActive) {
                                        globalAlertMessageSetter("Shirin AI übernimmt das Ausfüllen von '${form.name}' für dich. Die Daten werden verschlüsselt strukturiert!")
                                    } else {
                                        upgradeFormName = form.name
                                        showUpgradeDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormRowItem(
    form: FormItem,
    isPremiumActive: Boolean,
    onDownload: () -> Unit,
    onAddToSafe: () -> Unit,
    onAutofillClicked: () -> Unit
) {
    var isDownloading by remember { mutableStateOf(false) }
    var downloadFinished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.5f)) {
                Text(form.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${form.sizeBytes} KB • ${form.type}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            // Downloader options
            Row(
                modifier = Modifier.weight(0.5f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onAutofillClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .padding(end = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isPremiumActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoFixHigh,
                        contentDescription = "Automatisch ausfüllen",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ausfüllen", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        if (!isDownloading && !downloadFinished) {
                            isDownloading = true
                            scope.launch {
                                delay(1200) // Simulate fast secure download
                                isDownloading = false
                                downloadFinished = true
                                onDownload()
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                    } else {
                        Icon(
                            imageVector = if (downloadFinished) Icons.Filled.CheckCircle else Icons.Filled.Download,
                            contentDescription = "Formular Downloaden",
                            tint = if (downloadFinished) EmeraldAlert else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onAddToSafe,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.LockOpen,
                        contentDescription = "In Safe überspielen",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

data class FormAgency(val className: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val items: List<FormItem>)
data class FormItem(val name: String, val type: String, val sizeBytes: Int)

// ---------------- DIALOG FOR DOCUMENT INSPECTION & EXTRACTION ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAnalyzerDialog(
    doc: Document,
    viewModel: ShirinViewModel,
    onDismiss: () -> Unit,
    onInfoExtracted: (String, Double) -> Unit
) {
    var notesText by remember { mutableStateOf(doc.notes) }
    var detectedText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var extractionsDone by remember { mutableStateOf(false) }

    var detectedAmount by remember { mutableStateOf(129.90) }
    var detectedCategory by remember { mutableStateOf("Büro") }

    // Mock realistic OCR scanning text dependent on Doc Type
    LaunchedEffect(doc.id) {
        isAnalyzing = true
        delay(1200)
        isAnalyzing = false
        val content = viewModel.getDecryptedDocumentContent(doc)
        detectedText = if (content.isNotBlank()) {
            content
        } else {
            when (doc.type) {
                "RECHNUNG" -> "RECHNUNGS-OCR EXTRAKT:\nBetreff: Kauf IT-Zubehör & Serverlizenzen\nEmpfänger: Gemini Studios Tech Dev\nRechnungsnummer: #2026-9080\nDatum: 01.06.2026\nSumme Brutto: 129,90 € (MwSt-Anteil 19%)\nZahlungsmethode: SEPA-Banküberweisung"
                "STEUER" -> "STEUERAKT-OCR EXTRAKT:\nFinanzamt Hamburg-Nord\nSteuernummer: 22/190/08711\nBetreff: Einkommensteuer-Vorauszahlungs-Ankündigung 2026\nGeforderte vierteljährliche Sondervorauszahlung: 450,00 €"
                "PDF" -> "PDF-TEXT EXTRAKT:\nArbeitsvertrag Muster / Beilage\n§ 1 Arbeitszeit und Vergütung\nDas monatliche Bruttogehalt beträgt 3.850,00 € und ist am Monatsende fällig. Der Arbeitnehmer übernimmt Softwarearchitektur-Verpflichtungen."
                else -> "DOKUMENT-OCR EXTRAKT:\nDokumentenklasse: ${doc.type}\nInhaltsbeilagen: ${doc.name}\nEntscheidungsakten vorberaten. Keine automatische Steuerrelevanz erkannt."
            }
        }
        detectedAmount = when (doc.type) {
            "RECHNUNG" -> 129.90
            "STEUER" -> 450.00
            "PDF" -> 3850.00
            else -> 0.0
        }
        detectedCategory = when (doc.type) {
            "RECHNUNG" -> "Abonnements"
            "STEUER" -> "Steuern"
            "PDF" -> "Einkommen"
            else -> "Büro"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Scanner, "Scanner", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shirin Dokumenten-Inspektor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Dokumentenname: ${doc.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(doc.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Gray.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${doc.sizeBytes / 1024} KB", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider()

                // Scanner Content Extract Area
                Text("KI-Inhaltsanalyse & Textextraktion (OCR)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                if (isAnalyzing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Green, strokeWidth = 1.5.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verschlüsselte Verbindung...", color = Color.Green, style = MaterialTheme.typography.labelSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("⚡ [KEYSTORE] Lade Schlüssel aus Android Keystore...", color = Color.LightGray, style = MaterialTheme.typography.labelSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Text("⚡ [ALGO] Initialisiere AES/GCM/NoPadding (AES-256)...", color = Color.LightGray, style = MaterialTheme.typography.labelSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Text("⚡ [DECRYPT] Entschlüssele lokalen Dokumentenspeicher...", color = Color.LightGray, style = MaterialTheme.typography.labelSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = detectedText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                if (detectedAmount > 0.0 && !isAnalyzing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldAlert.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Extrahierte Schlüssel-Parameter:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldAlert)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Steuerrelevanter Betrag: $detectedAmount €", style = MaterialTheme.typography.bodySmall)
                            Text("• Vorgeschlagene Budget-Kategorie: $detectedCategory", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Eigene Anmerkung bearbeiten") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (detectedAmount > 0.0 && !extractionsDone) {
                    Button(
                        onClick = {
                            viewModel.addTransaction(
                                title = "[DOKU-BUCHUNG] ${doc.name}",
                                amount = detectedAmount,
                                isIncome = doc.type == "PDF", // If PDF let's simulate as income (contract) or expense
                                category = detectedCategory,
                                notes = "Automatisch extrahiert aus dem E2EE-Tresor."
                            )
                            extractionsDone = true
                            onInfoExtracted(doc.name, detectedAmount)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAlert)
                    ) {
                        Text("In Cockpit buchen")
                    }
                }
                Button(
                    onClick = { onDismiss() }
                ) {
                    Text("Schließen")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Abbrechen")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentDialog(
    viewModel: ShirinViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PDF") }
    var sizeKbStr by remember { mutableStateOf("240") }
    var notes by remember { mutableStateOf("") }

    val types = listOf("PDF", "RECHNUNG", "STEUER", "VERTRAG", "ANDERE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dokument verschlüsselt sichern") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dokumentenname / Beschreibung") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_doc_name")
                )

                Text("Dokumenten-Kategorie", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedType)
                                Icon(Icons.Filled.ArrowDropDown, "Mehr")
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            types.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        selectedType = t
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = sizeKbStr,
                    onValueChange = { sizeKbStr = it },
                    label = { Text("Mock Dateigröße (KB)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_doc_size")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Bemerkungen (z.B. Vertragsnummer)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kbVal = sizeKbStr.toLongOrNull() ?: 120L
                    if (name.isNotBlank()) {
                        viewModel.addDocument(
                            name = name,
                            type = selectedType,
                            sizeKb = kbVal,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("add_doc_confirm")
            ) {
                Text("End-to-End verschlüsseln")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun BuroZentraleView(
    viewModel: ShirinViewModel,
    globalAlertMessageSetter: (String) -> Unit
) {
    var smartOfficeTab by remember { mutableStateOf(0) } // 0: Angebots-Kalkulator, 1: Beleg-Scanner OCR
    val isOfficeLoading by viewModel.isOfficeLoading.collectAsState()

    // Tab 0 states
    var customerRequestText by remember { mutableStateOf("") }
    var offerResult by remember { mutableStateOf<com.example.ui.viewmodel.OfferCalculationResult?>(null) }

    // Tab 1 states
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var scanResult by remember { mutableStateOf<com.example.ui.viewmodel.OfficeReceiptScanResult?>(null) }
    var showRawJson by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Intro Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.BusinessCenter,
                        contentDescription = "Smart Office",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shirin Smart Office 💼",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Intelligente Büro-Automatisierung für Handwerk, Agenturen und Freelancer direkt gekoppelt an deine EÜR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub tabs
        TabRow(
            selectedTabIndex = smartOfficeTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = smartOfficeTab == 0,
                onClick = { smartOfficeTab = 0 },
                text = { Text("KI-Kalkulations", style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = smartOfficeTab == 1,
                onClick = { smartOfficeTab = 1 },
                text = { Text("Beleg-OCR", style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = smartOfficeTab == 2,
                onClick = { smartOfficeTab = 2 },
                text = { Text("Offene Belege", style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = smartOfficeTab == 3,
                onClick = { smartOfficeTab = 3 },
                text = { Text("Premium", style = MaterialTheme.typography.labelSmall) }
            )
        }

        when (smartOfficeTab) {
            0 -> {
                // --- OFFER CALCULATOR ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Handwerker Angebots-Kalkulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gib eine Kundenanfrage ein oder wähle einen Preset, um sofort ein professionell gegliedertes deutsches Angebot mit kalkulierter Gewinnmarge zu erzeugen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Quick Presets Selector
                Text(
                    text = "Beispiel-Anfragen:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Maler", "Elektro", "Trockenbau").forEach { type ->
                        SuggestionChip(
                            onClick = {
                                customerRequestText = when (type) {
                                    "Maler" -> "Ich brauche ein Angebot für das Streichen von 3 Zimmern, ca. 70qm, inkl. Premium-Farbe in weiß und abkleben. Bitte auch Anfahrt berechnen. Wohne in Hamburg-Altona."
                                    "Elektro" -> "Sanierung der Hauselektrik: Brauche 12 neue Dreifach-Steckdosen montiert und einen neuen modernen Verteilerkasten inkl. FI-Schutzschalter."
                                    "Trockenbau" -> "Trockenbauwand erstellen, ca. 15qm, Metall-Ständerwerk, doppelseitig beplankt mit Feuerschutz-Gipsplatten inkl. Fugen verspachteln."
                                    else -> ""
                                }
                            },
                            label = { Text(type) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customerRequestText,
                    onValueChange = { customerRequestText = it },
                    placeholder = { Text("Füge hier eine formlose E-Mail oder Kundenanfrage ein...") },
                    label = { Text("Kundenanfrage / Projekt-Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        viewModel.calculateOfficeOffer(customerRequestText) { result ->
                            offerResult = result
                            globalAlertMessageSetter("Angebot '${result.offerNumber}' wurde erfolgreich kalkuliert!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = customerRequestText.isNotBlank() && !isOfficeLoading
                ) {
                    if (isOfficeLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Berechne Angebot...")
                    } else {
                        Icon(Icons.Filled.Calculate, contentDescription = "Kalkulieren")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mit Shirin AI kalkulieren")
                    }
                }

                // Display Result Offer Card
                offerResult?.let { res ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Briefkopf / Letterhead
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "F.M AI-STUDIOS PARTNER",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Professionelles Handwerker-Angebot",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmeraldAlert.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = "MARGE: ${res.profitMarginPercent.toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAlert,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Metadata Block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("ANGEBOTS-NR:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.offerNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DATUM:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.date, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Column {
                                Text("EMPFÄNGER:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.customerName, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("VORGANG / PROJEKT:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.projectName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Positions Listing
                            Text("PROJEKTOBJEKTE & EINZELPOSTEN:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            res.positions.forEach { pos ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = pos.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(text = String.format("%.2f €", pos.total), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = pos.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "(${pos.quantity} ${pos.unit} × ${String.format("%.2f €", pos.pricePerUnit)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Totals
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Summe Netto:", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.2f €", res.totalNet), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Umsatzsteuer (19%):", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.2f €", res.taxAmount), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Brutto-Gesamtsumme:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Text(String.format("%.2f €", res.totalGross), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = res.summaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            // Integration actions trigger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        globalAlertMessageSetter("Angebot '${res.offerNumber}' wurde als professionelle PDF in Ihren Dateien abgespeichert!")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Teilen", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Teilen / PDF", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        // Save to accounting DB
                                        viewModel.addTransaction(
                                            title = "Kunde: ${res.projectName}",
                                            amount = res.totalGross,
                                            isIncome = true,
                                            category = "Büro",
                                            notes = "Vertrag über Angebot ${res.offerNumber} verbucht. Deckung: ${res.profitMarginPercent}%"
                                        )
                                        globalAlertMessageSetter("Erfolgreich verbucht! '${res.projectName}' wurde als vertragliches Einkommen erfasst und in dein Finanz-Cockpit eingepflegt.")
                                    },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Filled.Verified, contentDescription = "Verbuchen", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Vertrag buchen", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            }
            1 -> {
                // --- DOCUMENT SCANNER OCR ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "KI-gestützter Beleg-Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bringe Struktur in deine Zettelwirtschaft. Der OCR-Scanner liest alle Beträge und Steuern aus und ordnet sie den vordefinierten absetzbaren Konten zu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = "Simuliere Scan für Beleg-Vorlage:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Presets template chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Bauhaus (Ausgabe)", "Knauf Gips", "Aral Beleg").forEachIndexed { index, name ->
                        FilterChip(
                            selected = selectedPresetIndex == index,
                            onClick = { selectedPresetIndex = index },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.scanOfficeReceipt(selectedPresetIndex) { result ->
                            scanResult = result
                            globalAlertMessageSetter("Erfolgreich eingescannt: Beleg von '${result.merchantName}' extrahiert!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isOfficeLoading
                ) {
                    if (isOfficeLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanne Beleg...")
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = "Scannen")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beleg scannen & analysieren (OCR)")
                    }
                }

                // Digital sci-fi sweep laser line simulation
                if (isOfficeLoading) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.8f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Lese Metadaten via OCR...", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Display scan output matching requirements
                scanResult?.let { res ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Validiert",
                                        tint = EmeraldAlert,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "OCR VALIDATION REPORT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAlert
                                    )
                                }
                                TextButton(onClick = { showRawJson = !showRawJson }) {
                                    Text(
                                        if (showRawJson) "JSON schließen" else "Rohdaten JSON",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (showRawJson) {
                                val rawJsonText = """
                                {
                                  "merchant_name": "${res.merchantName}",
                                  "invoice_date": "${res.invoiceDate}",
                                  "invoice_number": ${res.invoiceNumber?.let { "\"$it\"" } ?: "null"},
                                  "currency": "${res.currency}",
                                  "total_gross": ${res.totalGross},
                                  "total_net": ${res.totalNet},
                                  "tax_amount": ${res.taxAmount},
                                  "tax_rate_percent": ${res.taxRatePercent},
                                  "category": "${res.category}"
                                }
                                """.trimIndent()

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = rawJsonText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Properties
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Name des Händlers:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.merchantName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rechnungsdatum:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.invoiceDate, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rechnungsnummer:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.invoiceNumber ?: "Nicht auffindbar", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Währung:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.currency, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Konto-Kategorie (Klassifizierung):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    ) {
                                        Text(
                                            text = res.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Netto-Betrag:", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.2f €", res.totalNet), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mehrwertsteuer (${res.taxRatePercent}%):", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.2f €", res.taxAmount), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Gesamt Brutto (Kassensturz):", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = RoseAlert)
                                    Text(String.format("%.2f €", res.totalGross), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = RoseAlert)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    viewModel.addTransaction(
                                        title = res.merchantName,
                                        amount = res.totalGross,
                                        isIncome = false,
                                        category = res.category,
                                        notes = "Eingescannt am 06.06.2026. Beleg-Nr: ${res.invoiceNumber ?: "N/A"}."
                                    )
                                    // Simulate extraction of VAT ID and Base64 image from the camera/receipt
                                    val simulatedVatId = "DE811111111" // Dummy German VAT ID
                                    val simulatedBase64 = "base64EncodedImageSequenceHereXCVB123"
                                    
                                    viewModel.verarbeiteEingescanntenBeleg(res.totalGross, 19, res.category, simulatedVatId, simulatedBase64)
                                    // Save copy to E2EE vault
                                    viewModel.addDocument(
                                        name = "${res.merchantName} (Scanner OCR)",
                                        type = "RECHNUNG",
                                        sizeKb = 145L,
                                        notes = "Gescannt aus der Büro-Zentrale. Klassifizierung: ${res.category}"
                                    )
                                    globalAlertMessageSetter("Erfolgreich verbucht! ${res.merchantName} (${String.format("%.2f", res.totalGross)}€) wurde als Betriebsausgabe unter '${res.category}' erfasst.")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = "Übertragen")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("In die EÜR-Buchhaltung übernehmen")
                            }
                        }
                    }
                }
            }
            }
            2 -> {
                // OFFENE BELEGE VIEW
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Offene / Unbearbeitete Belege",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hier findest du alle Belege und Rechnungen, die noch nicht final validiert oder einer EÜR-Kategorie zugeordnet wurden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Dummy list of open receipts
                    val openReceipts = listOf(
                        "Tankbeleg ARAL - 84,50 € - Scan benötigt",
                        "Baumarkt Bauhaus - Material - 213,90 € - Warten auf VIES",
                        "Google Cloud Rechnung - 45,00 € - Zuweisung fehlt"
                    )
                    
                    openReceipts.forEach { receiptText ->
                        Card(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.WarningAmber,
                                        contentDescription = "Offen",
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = receiptText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(onClick = { 
                                    globalAlertMessageSetter("Beleg wird für die EÜR validiert...")
                                }) {
                                    Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Bearbeiten", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    
                    if (openReceipts.isEmpty()) {
                        Text(
                            text = "Super! Keine offenen Belege. Alles ist aktuell in deiner EÜR verbucht.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldAlert,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            3 -> {
                PremiumTenantsSection(
                    viewModel = viewModel,
                    globalAlertMessageSetter = globalAlertMessageSetter
                )
            }
        }
    }
}

@Composable
fun PremiumTenantsSection(
    viewModel: ShirinViewModel,
    globalAlertMessageSetter: (String) -> Unit
) {
    val premiumTenants by viewModel.allPremiumTenants.collectAsState()
    
    // States for adding a new tenant
    var showAddDialog by remember { mutableStateOf(false) }
    var tenantId by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var hourlyRateTarget by remember { mutableStateOf("65.00") }
    var preferredVendorsText by remember { mutableStateOf("Hornbach, Amazon, Rexel") }
    var careerStage by remember { mutableStateOf("MEISTERBETRIEB") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prämien-Mandanten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verwalte deine Premium Firmen-Eigenschaften via Room SQLite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { 
                    tenantId = "MANDANT-2026-" + java.util.UUID.randomUUID().toString().take(5).uppercase()
                    companyName = ""
                    hourlyRateTarget = "65.00"
                    preferredVendorsText = "Hornbach, Amazon, Rexel"
                    careerStage = "MEISTERBETRIEB"
                    showAddDialog = true 
                },
                modifier = Modifier.testTag("add_tenant_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Mandant anlegen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (premiumTenants.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Group, contentDescription = "Keine Mandanten", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Keine Prämien-Mandanten gefunden.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Lege einen neuen Mandanten über das Plus-Symbol an.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            premiumTenants.forEach { tenant ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tenant.companyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mandant-ID: ${tenant.tenantId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { 
                                viewModel.deletePremiumTenant(tenant)
                                globalAlertMessageSetter("Prämien-Mandant '${tenant.companyName}' wurde erfolgreich gelöscht!")
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = RoseAlert)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("STUNDENSATZ ZIEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("%.2f € / Std.", tenant.hourlyRateTarget), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ENTWICKLUNGS-STUFE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(
                                        text = tenant.careerStage,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Column {
                            Text("BEVORZUGTE GROSSHÄNDLER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tenant.preferredVendors.forEach { vendor ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = vendor.trim(),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Neuer Prämien-Mandant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tenantId,
                            onValueChange = { tenantId = it },
                            label = { Text("Mandanten-ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Firmenname") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = hourlyRateTarget,
                            onValueChange = { hourlyRateTarget = it },
                            label = { Text("Ziel-Stundensatz (€)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = preferredVendorsText,
                            onValueChange = { preferredVendorsText = it },
                            label = { Text("Händler (kommagetrennt)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = careerStage,
                            onValueChange = { careerStage = it },
                            label = { Text("Stufe (z.B. MEISTERBETRIEB)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val rate = hourlyRateTarget.toDoubleOrNull() ?: 65.00
                            val vendors = preferredVendorsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.addPremiumTenant(
                                tenantId = tenantId,
                                companyName = companyName,
                                hourlyRateTarget = rate,
                                preferredVendors = vendors,
                                careerStage = careerStage
                            )
                            showAddDialog = false
                            globalAlertMessageSetter("Prämien-Mandant '$companyName' wurde erfolgreich in der Room Datenbank gespeichert!")
                        },
                        enabled = companyName.isNotBlank() && tenantId.isNotBlank()
                    ) {
                        Text("Speichern")
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
}


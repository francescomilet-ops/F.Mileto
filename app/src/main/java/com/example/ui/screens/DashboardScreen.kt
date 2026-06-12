package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.ShirinViewModel
import com.example.ui.components.LuxuryButton
import com.example.ui.components.LuxuryPopup
import com.example.utils.AdminService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ShirinViewModel,
    modifier: Modifier = Modifier,
    onNavigateToCamera: (() -> Unit)? = null
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val taxProjection by viewModel.latestTaxProjection.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showLibraryAuthDialog by remember { mutableStateOf(false) }
    var showLibraryScreen by remember { mutableStateOf(false) }
    var showPfaWorkersScreen by remember { mutableStateOf(false) }
    var showImageGenerationScreen by remember { mutableStateOf(false) }
    var showLernBossScreen by remember { mutableStateOf(false) }
    var showDiegosLerntoolScreen by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var showBudgetingScreen by remember { mutableStateOf(false) }

    val aktuelleNutzerEmail = "pfa.workers@gmail.com" // Angenommen aus Login-System

    if (showBudgetingScreen) {
        BudgetingScreen(viewModel = viewModel, onBack = { showBudgetingScreen = false })
        return
    }

    if (showDiegosLerntoolScreen) {
        DiegosLerntoolScreen(onBack = { showDiegosLerntoolScreen = false })
        return
    }

    if (showLernBossScreen) {
        LernBossAppScreen(onBack = { showLernBossScreen = false })
        return
    }

    if (showAdminDashboard) {
        AdminDashboardScreen(onBack = { showAdminDashboard = false })
        return
    }

    if (showImageGenerationScreen) {
        ImageGenerationScreen(onBack = { showImageGenerationScreen = false })
        return
    }

    if (showPfaWorkersScreen) {
        PfaWorkersScreen(onClose = { showPfaWorkersScreen = false })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_transaction_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Transaktion hinzufügen")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Welcome Card with localized greeting
            item {
                val userCountry = remember { java.util.Locale.getDefault().country.uppercase() }

                Text(
                    text = "Willkommen in deiner All-in-One App",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Überblicke Gelder, optimiere Steuern und sichere Belege mit Shirin AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GEMEINSAME DIENSTE (Für alle Länder weltweit sichtbar)
                Button(
                    onClick = { showBudgetingScreen = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAlert)
                ) {
                    Icon(Icons.Filled.PieChart, contentDescription = "Budgetierung")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Budget & Planung", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showDiegosLerntoolScreen = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("🎓 Diegos Lerntool", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showLernBossScreen = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("📚 LernBoss Pro", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showImageGenerationScreen = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("🎨 Shirin AI Vision (Bildgenerierung)", color = Color.Black, fontWeight = FontWeight.Bold)
                }



                Button(
                    onClick = { showLibraryAuthDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E))
                ) {
                    Icon(Icons.Filled.LibraryBooks, contentDescription = "Bibliothek", tint = Color(0xFFD4AF37))
                    Spacer(Modifier.width(8.dp))
                    Text("Familien-Bibliothek (Legacy)", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                }
                
                // SYSTEM STEUERUNG
                Spacer(modifier = Modifier.height(20.dp))
                var showLuxuryPopup by remember { mutableStateOf(false) }
                
                LuxuryButton(
                    text = "System aktivieren",
                    onClick = { showLuxuryPopup = true }
                )

                if (showLuxuryPopup) {
                    LuxuryPopup(
                        title = "System Status",
                        message = "Der Steuerungsbefehl wurde erfolgreich an das System übermittelt.",
                        onDismissRequest = { showLuxuryPopup = false },
                        onConfirm = { showLuxuryPopup = false }
                    )
                }

                // LÄNDERSPEZIFISCHE DIENSTE (Nur für Deutschland sichtbar)
                if (userCountry == "DE") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Regionale Dienste (Nur in Deutschland)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🛠️ PFA.Workers Handwerkerportal", style = MaterialTheme.typography.titleLarge)
                            Text("Trockenbau, Elektrotechnik & PV-Anlagen in deiner Region.")
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { showPfaWorkersScreen = true }) {
                                Text("Projekt starten")
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Hinweis: Handwerksleistungen (Trockenbau/Elektro) sind in deinem Land ($userCountry) derzeit nicht verfügbar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // ADMIN PANEL BEREICH
                if (AdminService.isAdmin(aktuelleNutzerEmail)) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdminDashboard = true },
                        color = Color(0xFF121212),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37))
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ENTER ADMIN PANEL",
                                color = Color(0xFFD4AF37),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Summary Financial Cards (Income & Expense Ring Chart)
            item {
                FinancialBalanceSummaryCard(transactions, viewModel)
            }

            // Tax Projection Card
            item {
                taxProjection?.let { proj ->
                    TaxEstimationCard(proj, viewModel, onNavigateToCamera)
                } ?: Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // FinPulse API Integration Control Center
            item {
                FinPulseSyncCard(viewModel = viewModel)
            }

            // Recent Bookings Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Letzte Buchungen",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (transactions.isNotEmpty()) {
                        Text(
                            text = "${transactions.size} gesamt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Transaction items list
            if (transactions.isEmpty()) {
                item {
                    EmptyFinancialsState()
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemRow(tx, viewModel) {
                        viewModel.deleteTransaction(tx)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showLibraryAuthDialog) {
        LegacyLibraryAuthDialog(
            onDismiss = { showLibraryAuthDialog = false },
            onAuthSuccess = {
                showLibraryAuthDialog = false
                showLibraryScreen = true
            }
        )
    }

    if (showLibraryScreen) {
        LegacyLibraryScreen(
            onClose = { showLibraryScreen = false },
            onTriggerAudio = { childName, isMama ->
                viewModel.checkSecurityAndActivateLegacyMode("shirin.ilaya.mileto.2022@gmail.com", "$childName ${if(isMama) "mama" else "papa"}")
            }
        )
    }
}

@Composable
fun FinancialBalanceSummaryCard(transactions: List<Transaction>, viewModel: ShirinViewModel) {
    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radial Chart Drawing in Jetpack Compose Canvas
            FinanceDonutChart(
                income = totalIncome.toFloat(),
                expense = totalExpense.toFloat(),
                modifier = Modifier
                    .size(90.dp)
                    .weight(0.35f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Financial Summary Numerical Data
            Column(
                modifier = Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Netto-Kassenbestand",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = viewModel.formatMoney(balance),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (balance >= 0) EmeraldAlert else RoseAlert,
                    modifier = Modifier.testTag("net_balance_value")
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldAlert)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Einkommen", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            text = viewModel.formatMoney(totalIncome),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldAlert
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RoseAlert)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ausgaben", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            text = viewModel.formatMoney(totalExpense),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoseAlert
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceDonutChart(income: Float, expense: Float, modifier: Modifier = Modifier) {
    val total = income + expense
    val expenseSweep = if (total > 0f) (expense / total) * 360f else 0f
    val incomeSweep = if (total > 0f) 360f - expenseSweep else 360f

    val colorIncome = EmeraldAlert
    val colorExpense = RoseAlert

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = (size.width - strokeWidth) / 2

        if (total == 0f) {
            // Empty state placeholder arc
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
        } else {
            // Outflow arc
            drawArc(
                color = colorExpense,
                startAngle = -90f,
                sweepAngle = expenseSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // Inflow arc
            drawArc(
                color = colorIncome,
                startAngle = -90f + expenseSweep,
                sweepAngle = incomeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )
        }
    }
}

@Composable
fun TaxEstimationCard(
    proj: com.example.data.TaxProjection,
    viewModel: ShirinViewModel,
    onNavigateToCamera: (() -> Unit)? = null
) {
    val umsatzerloese = viewModel.umsatzerloese
    val ustEinnahmen = viewModel.ustEinnahmen
    val materialEinkauf = viewModel.materialEinkauf
    val fremdleistungen = viewModel.fremdleistungen
    val vorsteuerAusgaben = viewModel.vorsteuerAusgaben
    val afaBetrag = viewModel.afaBetrag
    
    val einnahmen = viewModel.summeBetriebseinnahmen
    val gesamtausgaben = viewModel.summeBetriebsausgaben
    val gewinn = viewModel.ermittelterGewinn
    
    val steuersatz by viewModel.steuersatzState.collectAsState()
    val voraussichtlicheEinkommensteuer = if (gewinn > 0.0) gewinn * steuersatz else 0.0

    var showExportResult by remember { mutableStateOf(false) }
    var xmlDaten by remember { mutableStateOf("") }
    var exportProgress by remember { mutableStateOf(0f) }
    var isExporting by remember { mutableStateOf(false) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0f
            while (exportProgress < 1f) {
                kotlinx.coroutines.delay(100)
                exportProgress += 0.1f
            }
            xmlDaten = viewModel.erstelleElsterXmlExport("2026")
            isExporting = false
            showExportResult = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tax_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AccountBalance,
                        contentDescription = "Steuer Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "F.M Steuer-Automation Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "EÜR Gewinnermittlung (§ 4 Abs. 3 EStG)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DE 2026",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Tortendiagramm: Einnahmen vs. Ausgabekategorien
            Column {
                Text(
                    text = "SEKTION 3: SHIRIN STEUERÜBERBLICK (VISUELL)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val totalSum = umsatzerloese + materialEinkauf + fremdleistungen + afaBetrag + vorsteuerAusgaben
                if (totalSum > 0) {
                    val angles = remember(umsatzerloese, materialEinkauf, fremdleistungen, afaBetrag, vorsteuerAusgaben) {
                        listOf(
                            (umsatzerloese / totalSum) * 360f,
                            (materialEinkauf / totalSum) * 360f,
                            (fremdleistungen / totalSum) * 360f,
                            (afaBetrag / totalSum) * 360f,
                            (vorsteuerAusgaben / totalSum) * 360f
                        )
                    }
                    val colorsList = remember { listOf(EmeraldAlert, RoseAlert, Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFFF44336)) }
                    val labels = remember { listOf("Einnahmen", "Material", "Fremdleist.", "AfA", "Vorsteuer") }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
                            var startAngle = -90f
                            angles.forEachIndexed { index, sweepAngle ->
                                if (index < colorsList.size) {
                                    drawArc(
                                        color = colorsList[index],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle.toFloat(),
                                        useCenter = true
                                    )
                                    startAngle += sweepAngle.toFloat()
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            labels.forEachIndexed { index, label ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = colorsList[index]) {}
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                } else {
                    Text("Noch keine Daten für Diagramm vorhanden.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Sektion 1: Finanzen Übersicht
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SEKTION 1: ELSTER-KENNZAHLEN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = EmeraldAlert) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Umsatzerlöse (Kz 102)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = viewModel.formatMoney(umsatzerloese),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = RoseAlert) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Material / Wareneinkauf (Kz 500)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = viewModel.formatMoney(materialEinkauf),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = RoseAlert) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fremdleistungen (Kz 510)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = viewModel.formatMoney(fremdleistungen),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = RoseAlert) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abschreibungen (Anlage AVEÜR)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = viewModel.formatMoney(afaBetrag),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Sektion 2: Berechnung / Auswertung
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SEKTION 2: BERECHNUNG & PROGNOSE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "REINER GEWINN (EÜR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.formatMoney(gewinn),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (gewinn >= 0) EmeraldAlert else RoseAlert
                        )
                    }
                    Icon(
                        imageVector = if (gewinn >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = "Trend",
                        tint = if (gewinn >= 0) EmeraldAlert else RoseAlert,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom assumed tax rate slider
                Text(
                    text = "Angenommener Steuersatz: ${(steuersatz * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = steuersatz.toFloat(),
                    onValueChange = { viewModel.updateSteuersatz(it.toDouble()) },
                    valueRange = 0.10f..0.50f,
                    steps = 7, // allows selection like 10%, 15%, 20%, 25%, 30%, 35%, 40%, 45%, 50%
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voraussichtliche Einkommensteuer:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = viewModel.formatMoney(voraussichtlicheEinkommensteuer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (voraussichtlicheEinkommensteuer > 0) RoseAlert else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Sektion 3: Beleg-Scanner & Export
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SEKTION 3: BELEG-SCANNER & REGISTRIERUNG",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onNavigateToCamera?.invoke()
                        },
                        modifier = Modifier.weight(1f).testTag("studio_camera_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Zielen", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kamera-Scanner", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            isExporting = true
                        },
                        enabled = !isExporting,
                        modifier = Modifier.weight(1f).testTag("studio_export_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = "Exportieren", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isExporting) "Generiere..." else "Exportieren", fontSize = 11.sp, maxLines = 1)
                    }
                }

                if (isExporting) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Exportiere EÜR Protokoll.pdf & Steuerbelege.csv...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // Custom Export Success Visual Dialog Pop-Up
    if (showExportResult) {
        AlertDialog(
            onDismissRequest = { showExportResult = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountBalance, "Elster", tint = EmeraldAlert, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elster-Schnittstelle aktiv")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Die EÜR und die Anlage AVEÜR wurden für die Weiterleitung an MeinElster formatiert.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Generierte XML-Daten:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = xmlDaten,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    showExportResult = false 
                }) {
                    Text("Daten weiterleiten")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportResult = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun TransactionItemRow(
    tx: Transaction,
    viewModel: ShirinViewModel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${tx.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.7f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (tx.isIncome) EmeraldAlert.copy(alpha = 0.15f)
                            else RoseAlert.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = "Richtung",
                        tint = if (tx.isIncome) EmeraldAlert else RoseAlert,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (tx.notes.isNotBlank()) {
                            Text(
                                text = tx.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(0.3f)
            ) {
                Text(
                    text = "${if (tx.isIncome) "+" else "-"}${viewModel.formatMoney(tx.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (tx.isIncome) EmeraldAlert else RoseAlert
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eintrag löschen",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFinancialsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.AccountBalanceWallet,
            contentDescription = "Keine Transaktionen",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keine Buchungen registriert",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Klicke auf das '+' unten rechts, um Ausgaben oder Gehalt einzupflegen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: ShirinViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Lebensmittel") }

    val categories = if (isIncome) {
        listOf("Einkommen", "Rückzahlung", "Investition", "Zinsen", "Nebenjob")
    } else {
        listOf("Lebensmittel", "Wohnen", "Abonnements", "Büro", "Freizeit", "Versicherung", "Auto", "Ausschuss / Verlust")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Transaktion hinzufügen", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isIncome = false
                            selectedCategory = "Lebensmittel"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            contentColor = if (!isIncome) Color.White else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ausgabe")
                    }

                    Button(
                        onClick = {
                            isIncome = true
                            selectedCategory = "Einkommen"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) EmeraldAlert else EmeraldAlert.copy(alpha = 0.15f),
                            contentColor = if (isIncome) Color.White else EmeraldAlert
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Einkommen")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel / Empfänger") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_title")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Betrag (€)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount")
                )

                Text("Kategorie wählen", style = MaterialTheme.typography.labelSmall)
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
                                Text(selectedCategory)
                                Icon(Icons.Filled.ArrowDropDown, "Mehr")
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anmerkungen (optional)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        viewModel.addTransaction(
                            title = title,
                            amount = amt,
                            isIncome = isIncome,
                            category = selectedCategory,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("add_tx_confirm")
            ) {
                Text("Speichern")
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
fun FinPulseSyncCard(viewModel: ShirinViewModel) {
    val token by viewModel.finPulseToken.collectAsState()
    val email by viewModel.finPulseEmail.collectAsState()
    val plan by viewModel.finPulsePlan.collectAsState()
    val endpoint by viewModel.finPulseEndpoint.collectAsState()
    val status by viewModel.finPulseStatus.collectAsState()
    val cockpit by viewModel.finPulseCockpitData.collectAsState()

    var emailInput by remember { mutableStateOf(email ?: "francescomilet@gmail.com") }
    var passwordInput by remember { mutableStateOf("") }
    var endpointInput by remember { mutableStateOf(endpoint) }
    var showSettings by remember { mutableStateOf(false) }

    var showElsterResult by remember { mutableStateOf(false) }
    var elsterTicket by remember { mutableStateOf("") }
    var elsterLogs by remember { mutableStateOf("") }
    var isElsterExporting by remember { mutableStateOf(false) }

    if (showElsterResult) {
        AlertDialog(
            onDismissRequest = { showElsterResult = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ELSTER-Schnittstelle: Übertrag")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Deine Einnahmen-Überschuss-Rechnung (EÜR) wurde erfolgreich validiert und sicher an das Bundesfinanzamt übermittelt.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Meldungsticket: $elsterTicket",
                                color = Color.Green,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Server-Zeitstempel: " + java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.GERMANY).format(java.util.Date()),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                    Divider()
                    Text(
                        "ELSTER VALIDIERUNGSLOGS:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = elsterLogs,
                            color = Color.Green,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🔒 Alle Chiffre-Dateien und Belege wurden parallel im lokalen verschlüsselten Speichercache (AES-256-GCM) abgeglichen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showElsterResult = false }) {
                    Text("Schließen")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finpulse_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = "FinPulse Sync",
                        tint = if (token != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FinPulse® API-Verbindung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Connection status indicator
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (token != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.outlineVariant
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (token != null) Color(0xFF2E7D32) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (token != null) "Aktiv" else "Inaktiv",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (token != null) Color(0xFF2E7D32) else Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle status logs
            Text(
                text = "Status: $status",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (token == null) {
                // Logged OUT View: Show Login Inputs & Config
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("E-Mail Adresse") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Passwort") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Key") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Advanced endpoint config toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSettings = !showSettings }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (showSettings) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = "Toggle Endpoint settings"
                    )
                    Text(
                        "Erweiterte Verbindungs-Einstellungen",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showSettings) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endpointInput,
                        onValueChange = {
                            endpointInput = it
                            viewModel.updateFinPulseEndpoint(it)
                        },
                        label = { Text("API Endpunkt URL") },
                        supportingText = { Text("Aktueller Docker- oder Cloud-Container") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.loginFinPulse(emailInput, passwordInput)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("finpulse_login_button")
                ) {
                    Icon(Icons.Filled.VpnKey, contentDescription = "Sitzung initiieren")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kanzlei-Konto verbinden")
                }
            } else {
                // Logged IN View: Show active account, telemetry, and ELSTER utilities
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = email ?: "francescomilet@gmail.com",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Token: ${token?.take(18)}...",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                // Plan indicator badge
                                val isDev = plan == "developer_stack"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isDev) Color(0xFFFFE082) else Color(0xFFE0F7FA)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isDev) "DEV-STACK 🚀" else "BASIS (4.99 €)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDev) Color(0xFFE65100) else Color(0xFF006064)
                                    )
                                }
                            }

                            if (plan != "developer_stack") {
                                Spacer(modifier = Modifier.height(12.dp))
                                // Premium Upgrade Option inside the container
                                Button(
                                    onClick = { viewModel.upgradeFinPulseSubscription() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("finpulse_upgrade_button")
                                ) {
                                    Text("Upgrade auf Entwickler-Stack (15,99 €/Mo)")
                                }
                            }
                        }
                    }

                    // Dynamic Cockpit data representation from backend
                    cockpit?.let { data ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "FinPulse Kanzlei-Prüfung",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Umsatz:", style = MaterialTheme.typography.labelSmall)
                                        Text(viewModel.formatMoney(data.totalRevenue), fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Ausgaben:", style = MaterialTheme.typography.labelSmall)
                                        Text(viewModel.formatMoney(data.totalExpenses), fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("T-Steuerlast:", style = MaterialTheme.typography.labelSmall)
                                        Text(viewModel.formatMoney(data.predictedTaxes), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }
                                    Column {
                                        Text("Score:", style = MaterialTheme.typography.labelSmall)
                                        Text("${data.financialHealthIndex}/100", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Automatisierte Kanzlei-Erhöhungen:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    data.insights.forEach { insight ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("• ", fontWeight = FontWeight.Bold)
                                            Text(insight, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Kanzlei ELSTER export Trigger!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isElsterExporting = true
                                viewModel.triggerElsterExportFinPulse { success, ticket, logs ->
                                    isElsterExporting = false
                                    if (success) {
                                        elsterTicket = ticket ?: "N/A"
                                        elsterLogs = logs ?: "Keine Logs vorhanden."
                                        showElsterResult = true
                                    }
                                }
                            },
                            enabled = !isElsterExporting,
                            modifier = Modifier.weight(1f).testTag("elster_export_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B5E20)
                            )
                        ) {
                            if (isElsterExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Filled.CloudUpload, contentDescription = "ELSTER Submit")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ELSTER Senden")
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadFinPulseCockpit() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Cockpit aktualisieren")
                        }

                        IconButton(
                            onClick = { viewModel.logoutFinPulse() },
                            modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = "Abmelden", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

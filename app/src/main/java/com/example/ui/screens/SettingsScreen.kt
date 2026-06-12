package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ShirinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiAboSettingsScreen(
    viewModel: ShirinViewModel,
    onClose: () -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    Scaffold(
        containerColor = Color(0xFF0A0E1A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Zentrale / Einstellungen", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // FINANZEN & KASSE
            CategoryTitle("Finanzen & Kasse")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.Green) },
                    headlineContent = { Text("Netto-Kassenbestand", color = Color.White) },
                    supportingContent = { Text("32.154,12 €", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            // ANWENDUNGEN & TOOLS
            CategoryTitle("Anwendungen & Tools")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.School, contentDescription = null, tint = Color(0xFF536DFE)) },
                        headlineContent = { Text("🎓 Diegos Lerntool", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF2196F3)) },
                        headlineContent = { Text("📚 LernBoss Pro", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.ColorLens, contentDescription = null, tint = Color(0xFFFF9800)) },
                        headlineContent = { Text("🎨 Shirin AI Vision (Bildgenerierung)", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null, tint = Color.Gray) },
                        headlineContent = { Text("📂 Familien-Bibliothek (Legacy)", color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))

            // ADMINISTRATION
            CategoryTitle("Administration")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color(0xFFFFC107)) },
                        headlineContent = { Text("ENTER ADMIN PANEL", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Color(0xFFFF5252)) },
                        headlineContent = { Text("SYSTEM AKTIVIEREN", color = Color.White.copy(alpha = 0.54f)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))

            // VERWALTUNG & DOKUMENTE
            CategoryTitle("Verwaltung & Dokumente")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.FolderSpecial, contentDescription = null, tint = Color(0xFF81D4FA)) },
                        headlineContent = { Text("Alle App-Dokumentordner", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null, tint = Color(0xFFA5D6A7)) },
                        headlineContent = { Text("Erstellungsordner", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Archive, contentDescription = null, tint = Color(0xFFBCAAA4)) },
                        headlineContent = { Text("Ablageordner", color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))

            // STEUERN & FINANZEN
            CategoryTitle("Steuern & Finanzen")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    modifier = Modifier.clickable { },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Color(0xFFFFAB91)) },
                    headlineContent = { Text("Abrufbare Steuererklärungen", color = Color.White) }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            // RECHTLICHES
            CategoryTitle("Rechtliches")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Policy, contentDescription = null, tint = Color(0xFFB0BEC5)) },
                        headlineContent = { Text("AGBs", color = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFB0BEC5)) },
                        headlineContent = { Text("Impressum", color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CategoryTitle(title: String) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.38f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
    )
}

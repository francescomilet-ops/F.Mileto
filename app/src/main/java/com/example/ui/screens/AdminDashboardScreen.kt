package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "ADMIN CONTROL PANEL", 
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
                HorizontalDivider(color = Color(0xFFD4AF37), thickness = 0.5.dp)
            }
        },
        containerColor = Color.Black
    ) { padding ->
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val columns = if (screenWidth > 600) 3 else 2

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "MANAGEMENT CONSOLE",
                color = Color(0xFFD4AF37),
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    AdminCard(
                        icon = Icons.Filled.EditNote,
                        title = "SEITEN & APPS",
                        subtitle = "Inhalte & Module bearbeiten",
                        onTap = {}
                    )
                }
                item {
                    AdminCard(
                        icon = Icons.Outlined.AddPhotoAlternate,
                        title = "BILDER / AR",
                        subtitle = "Galerie & Previews verwalten",
                        onTap = {}
                    )
                }
                item {
                    AdminCard(
                        icon = Icons.Outlined.Sell,
                        title = "PRODUKTE & PREISE",
                        subtitle = "Kollektionen & Tarife anpassen",
                        onTap = {}
                    )
                }
                item {
                    AdminCard(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "BESTELLUNGEN",
                        subtitle = "Eingegangene Anfragen einsehen",
                        onTap = {}
                    )
                }
                item {
                    AdminCard(
                        icon = Icons.Filled.CardMembership,
                        title = "PREMIUM ABOS",
                        subtitle = "Mitglieder & Abostatus prüfen",
                        onTap = {}
                    )
                }
                item {
                    AdminCard(
                        icon = Icons.Filled.Gavel,
                        title = "VERTRÄGE",
                        subtitle = "Kundenverträge & Abschlüsse",
                        onTap = {}
                    )
                }
            }
        }
    }
}

@Composable
fun AdminCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onTap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // to make the cards roughly square or at least consistent height
            .clickable(onClick = onTap),
        color = Color(0xFF1E1E1E),
        shape = RectangleShape,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(28.dp)
            )
            
            Column {
                Text(
                    text = title,
                    color = Color(0xFFF5F5F7),
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.54f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

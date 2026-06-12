package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ShirinViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShirinViewModel,
    onNavigateToSettings: () -> Unit,
    onActivateVoice: () -> Unit
) {
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var expandedAnmeldedaten by remember { mutableStateOf(false) }
    var showEmailLogin by remember { mutableStateOf(false) }

    // Animation for Shirin Sphere
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulseElevation by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (showEmailLogin) {
        AlertDialog(
            onDismissRequest = { showEmailLogin = false },
            containerColor = Color(0xFF161B2A),
            title = { Text("Mit E-Mail anmelden", color = Color.White) },
            text = {
                Column(modifier = Modifier.wrapContentHeight()) {
                    Text(
                        "Egal ob Yahoo, Outlook oder Gmail – geben Sie Ihre Adresse ein:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    var email by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("beispiel@yahoo.de", color = Color.White.copy(alpha = 0.38f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Cyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.24f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEmailLogin = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7))
                ) {
                    Text("Weiter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailLogin = false }) {
                    Text("Abbrechen", color = Color.White.copy(alpha = 0.54f))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0A0E1A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Shirin AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // SHIRIN AI SPHÄRE
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onActivateVoice,
                        onClickLabel = "Sprachsteuerung aktivieren",
                        role = androidx.compose.ui.semantics.Role.Button
                    )
                    .shadow(
                        elevation = pulseElevation.dp,
                        shape = CircleShape,
                        ambientColor = Color.Cyan,
                        spotColor = Color(0xFF9C27B0)
                    )
                    .rotate(rotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color.Cyan, Color(0xFF9C27B0), Color.Blue, Color.Cyan)
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp), // Inner padding
                contentAlignment = Alignment.Center
            ) {
                // Inner dark circle to reverse the rotation visually or static? I'll keep it rotating with outer, just the icon static
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0E1A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotated back icon so it stays upright
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Sprachsteuerung",
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                            .rotate(-rotation)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Tippe auf den Orb, um zu sprechen...",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(30.dp))

            // DIE 4 ERLAUBTEN FUNKTIONEN (Direkt unter Shirin)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2A)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = if (isPremiumActive) BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                    )
                ) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column {
                    // KACHEL 1: ANMELDEDATEN
                    ListItem(
                        modifier = Modifier.clickable { expandedAnmeldedaten = !expandedAnmeldedaten },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = Color.Cyan) },
                        headlineContent = { Text("Anmeldedaten", color = Color.White, fontWeight = FontWeight.Medium) },
                        trailingContent = {
                            Icon(
                                if (expandedAnmeldedaten) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    if (expandedAnmeldedaten) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            ListItem(
                                modifier = Modifier.clickable {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Google Anmeldung wird gestartet...") }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = { Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(30.dp)) },
                                headlineContent = { Text("Mit Google anmelden", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) }
                            )
                            ListItem(
                                modifier = Modifier.clickable {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Apple ID Anmeldung wird gestartet...") }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = { Icon(Icons.Filled.PhoneIphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) },
                                headlineContent = { Text("Mit Apple ID anmelden", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) }
                            )
                            ListItem(
                                modifier = Modifier.clickable { showEmailLogin = true },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFFFFAB40), modifier = Modifier.size(22.dp)) },
                                headlineContent = { Text("Mit Yahoo / Sonstige E-Mails", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) }
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    // KACHEL 2: ZUGANGS-VERWALTUNG
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Outlined.VpnKey, contentDescription = null, tint = Color(0xFF448AFF)) },
                        headlineContent = { Text("Zugangs-Verwaltung", color = Color.White, fontWeight = FontWeight.Medium) },
                        trailingContent = { Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp)) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    // KACHEL 3: REGISTRIERUNG
                    ListItem(
                        modifier = Modifier.clickable { showEmailLogin = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = Color(0xFF69F0AE)) },
                        headlineContent = { Text("Registrierung", color = Color.White, fontWeight = FontWeight.Medium) },
                        trailingContent = { Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp)) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    // KACHEL 4: ABOS
                    ListItem(
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Outlined.CardMembership, contentDescription = null, tint = Color(0xFFFFD740)) },
                        headlineContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Meine Abonnements", color = Color.White, fontWeight = FontWeight.Medium)
                                if (isPremiumActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    PremiumBadge()
                                }
                            }
                        },
                        trailingContent = { Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PremiumBadge() {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
            )
        )
    ) {
        Text(
            text = "PREMIUM",
            color = Color(0xFFFFD700),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

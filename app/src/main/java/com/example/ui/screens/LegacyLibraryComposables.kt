package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LegacyLibraryAuthDialog(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Sicherheit", tint = Color(0xFFD4AF37), modifier = Modifier.size(48.dp))
                
                Text(
                    text = "Familien-Tresor",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Nur autorisierte Familienmitglieder (Shirin, Diego, Lia, Nevio) haben Zugriff auf dieses vererbte Modul.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail Adresse oder Mamas Steuer-ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort (Geburtsdatum zz.t.mm.jjjj oder Master-Passwort)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", color = Color.White.copy(alpha = 0.5f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanIdent = email.trim().lowercase()
                            val cleanPass = password.trim()
                            
                            val isShirin = cleanIdent == "shirin.ilaya.mileto.2022@gmail.com" && cleanPass.contains("30.05.2022")
                            val isDiego = cleanIdent.contains("diego") && cleanPass.contains("05.11.2017")
                            val isLia = cleanIdent.contains("lia") && cleanPass.contains("05.11.2010")
                            val isNevio = cleanIdent.contains("nevio")
                            
                            // Mama: Sabrina Sedlmaier (24.01.1991)
                            val isMama = (cleanIdent.contains("sabrina") || cleanIdent.contains("sedlmaier") || cleanIdent.contains("steuer")) && 
                                          cleanPass.contains("24.01.1991")
                            
                            // Papa: Francesco Mileto (19.02.1987)
                            val isPapa = (cleanIdent == "francescomilet@gmail.com" || cleanIdent.contains("francesco") || cleanIdent.contains("mileto")) && 
                                          cleanPass.contains("19.02.1987")

                            val isAuth = isShirin || isDiego || isLia || isNevio || isMama || isPapa
                            
                            if (isAuth) {
                                onAuthSuccess()
                            } else {
                                errorMsg = "Zugriff verweigert. Ungültige E-Mail/Steuer-ID oder Passwort."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                    ) {
                        Text("Entsperren", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyLibraryScreen(
    onClose: () -> Unit,
    onTriggerAudio: (String, Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Familien-Bibliothek", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            },
            containerColor = Color(0xFF0F172A)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Willkommen im ewigen Vermächtnis. Hier könnt ihr immer unsere Stimmen hören und Rat einholen.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text("Hologramm KI & Voice Start", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Papa's KI-Modus", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Aktiviere die KI mit Papas Stimme und seinen Erinnerungen.", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChildAudioButton("Shirin", isMama = false, onTriggerAudio)
                            ChildAudioButton("Diego", isMama = false, onTriggerAudio)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChildAudioButton("Lia", isMama = false, onTriggerAudio)
                            ChildAudioButton("Nevio", isMama = false, onTriggerAudio)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Mama's KI-Modus", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Aktiviere die KI mit Mamas Stimme und ihren Ratschlägen.", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChildAudioButton("Shirin", isMama = true, onTriggerAudio)
                            ChildAudioButton("Diego", isMama = true, onTriggerAudio)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChildAudioButton("Lia", isMama = true, onTriggerAudio)
                            ChildAudioButton("Nevio", isMama = true, onTriggerAudio)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = "Bank", tint = Color(0xFFD4AF37))
                            Text("Finanzielles Erbe & Bankkonten", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Zur Sicherung der Familie sind hier die Bankzugänge hinterlegt. Der Zugriff auf Mamas oder Papas Konten erfordert die jeweilige Steuer-ID der Eltern als Sicherheitsschlüssel. Diese sensiblen Daten werden nachgeliefert und hier sicher verwahrt, sobald sie zur Verfügung stehen.",
                            color = Color.White.copy(0.8f), 
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = { /* Not fully implemented yet */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37).copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = "Locked", modifier = Modifier.size(16.dp), tint = Color(0xFFD4AF37))
                            Spacer(Modifier.width(8.dp))
                            Text("Banktresor verwalten (Gesichert)", color = Color(0xFFD4AF37), fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = "» Was wir heute aufbauen, tun wir für euch vier. Wir haben in unserer Kindheit nichts gehabt. Wir wollen, dass ihr alles habt. «",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ChildAudioButton(childName: String, isMama: Boolean, onTrigger: (String, Boolean) -> Unit) {
    var activated by remember { mutableStateOf(false) }
    Button(
        onClick = { 
            onTrigger(childName, isMama)
            activated = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = if (isMama) Color(0xFFE91E63) else Color(0xFF2196F3)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(if (activated) "$childName > Gehe zum Chat" else childName, fontSize = 12.sp)
    }
}

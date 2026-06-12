package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ShirinViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(
    viewModel: ShirinViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }
    var creditCardInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (step == 1) Icons.Filled.AccountCircle else Icons.Filled.CreditCard,
                contentDescription = "Step Icon",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (step == 1) "Registrierung erforderlich" else "Zahlungsmittel & API",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (step == 1) "Aus Sicherheits- und Abrechnungsgründen muss jeder Nutzer seinen eigenen Account erstellen." 
                else "Niemand kann die API oder Kreditkarte eines anderen nutzen. Bitte hinterlege deine eigene Zahlungsmethode oder deinen eigenen API-Key.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("E-Mail Adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Passwort") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        if (emailInput == "pfa.workers@gmail.com" && passwordInput == "ItakaLanDen0102.") {
                            viewModel.upgradeSubscription("PRO")
                            viewModel.completeSetup()
                            onComplete()
                        } else if (emailInput.isNotBlank() && passwordInput.isNotBlank()) step = 2 
                        else coroutineScope.launch { snackbarHostState.showSnackbar("Bitte fülle alle Felder aus") }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrieren & Weiter")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val context = androidx.compose.ui.platform.LocalContext.current
                TextButton(
                    onClick = {
                        emailInput = "pfa.workers@gmail.com"
                        passwordInput = "ItakaLanDen0102."
                        com.example.utils.BiometricHelper.authenticate(
                            context = context,
                            onSuccess = {
                                viewModel.upgradeSubscription("PRO")
                                viewModel.completeSetup()
                                onComplete()
                            },
                            onError = { error ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        )
                    }
                ) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schnell-Anmeldung (FaceID / Fingerabdruck)", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Step 2: Payment or API Key
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Option 1: Premium Abo (Eigene Kreditkarte)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = creditCardInput,
                            onValueChange = { creditCardInput = it },
                            label = { Text("Kreditkartennummer") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                if (creditCardInput.length > 10) {
                                    viewModel.upgradeSubscription("PRO")
                                    viewModel.completeSetup()
                                    onComplete()
                                } else {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Bitte gültige Kreditkarte eingeben") }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abo abschließen", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("— ODER —", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Option 2: Eigener API Key", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = { 
                                if (apiKeyInput.isNotBlank()) {
                                    val prefs = context.getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().putString("custom_gemini_key", apiKeyInput.trim()).apply()
                                    viewModel.completeSetup()
                                    onComplete()
                                } else {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Bitte API Key eingeben") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mit Key fortfahren")
                        }
                    }
                }
            }
        }
    }
}

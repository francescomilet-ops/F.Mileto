package com.example.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Liste der weltweit wichtigsten Sprachen (erweiterbar)
data class Language(val code: String, val name: String, val flag: String)
val allLanguages = listOf(
    Language("de", "Deutsch", "🇩🇪"),
    Language("en", "English", "🇺🇸"),
    Language("it", "Italiano", "🇮🇹"),
    Language("tr", "Türkçe", "🇹🇷"),
    Language("es", "Español", "🇪🇸"),
    Language("fr", "Français", "🇫🇷")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcherButton() {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Der Haupt-Button für deine App
    Button(onClick = { showDialog = true }) {
        Text(text = "🌐 Sprache wechseln / Change Language")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Sprache auswählen / Select Language") },
            text = {
                Column {
                    // Suchfeld, damit man nicht scrollen muss
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Suchen / Search...") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        val filteredLanguages = allLanguages.filter { 
                            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
                        }
                        
                        items(filteredLanguages) { lang ->
                            TextButton(
                                onClick = {
                                    // Hier passiert die Android-Magie: Systemweite App-Sprache ändern
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang.code)
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                    showDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${lang.flag} ${lang.name} (${lang.code.uppercase()})", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Schließen") }
            }
        )
    }
}

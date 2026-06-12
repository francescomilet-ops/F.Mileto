package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.viewmodel.ShirinImageGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenerationScreen(
    onBack: () -> Unit,
    viewModel: ShirinImageGenViewModel = viewModel()
) {
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Hyperrealistisch") }
    var selectedResolution by remember { mutableStateOf("1024x1024") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val imageUrl by viewModel.generatedImageUrl.collectAsState()

    val styles = listOf("Hyperrealistisch", "Anime", "Ölgemälde", "Cyberpunk", "Minimalistisch", "3D Render")
    val resolutions = listOf("1024x1024", "1024x1792")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shirin AI Vision", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color(0xFFD4AF37)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Beschreibe deine Vision",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Z.B. Ein fliegendes Auto über einer futuristischen Wüstenstadt...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFD4AF37)
                ),
                maxLines = 5
            )

            // Styles Selector
            Text(
                "Kunststil",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Chips for styles
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        label = { Text(style, color = if (selectedStyle == style) Color.Black else Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD4AF37),
                            containerColor = Color(0xFF1E293B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedStyle == style) Color(0xFFD4AF37) else Color(0xFF334155),
                            enabled = true,
                            selected = selectedStyle == style
                        )
                    )
                }
            }

            // Resolution
            Text(
                "Auflösung (Dall-E 3)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                resolutions.forEach { res ->
                    FilterChip(
                        selected = selectedResolution == res,
                        onClick = { selectedResolution = res },
                        label = { Text(res, color = if (selectedResolution == res) Color.Black else Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD4AF37),
                            containerColor = Color(0xFF1E293B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedResolution == res) Color(0xFFD4AF37) else Color(0xFF334155),
                            enabled = true,
                            selected = selectedResolution == res
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateImage(prompt, selectedStyle, selectedResolution) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = prompt.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generiert Vision...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Generieren", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bild generieren", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            if (!error.isNullOrEmpty()) {
                Surface(
                    color = Color(0xFF450a0a),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = Color(0xFFfca5a5),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // Image Result
            if (imageUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Deine Vision",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Generiertes KI Bild",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

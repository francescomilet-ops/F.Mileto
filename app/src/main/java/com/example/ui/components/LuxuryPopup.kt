package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LuxuryPopup(
    title: String,
    message: String,
    confirmLabel: String = "BESTÄTIGEN",
    onDismissRequest: () -> Unit,
    onConfirm: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    val popupWidth = when {
        screenWidth >= 840 -> 500.dp // Web/Desktop anpassen
        screenWidth >= 600 -> 450.dp // Tablet/Windows/MacOS anpassen
        else -> 300.dp // Standard Smartphone
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // to have more control over the width
        )
    ) {
        Surface(
            modifier = Modifier
                .width(popupWidth)
                .wrapContentHeight(),
            color = Color(0xFF1E1E1E), // Surface-Dunkel
            shape = RoundedCornerShape(2.dp), // Sehr kantig, modern
            border = BorderStroke(1.dp, Color(0xFFD4AF37)) // Goldener Rand
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color(0xFFF5F5F7),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 21.sp // Approximate 1.5 height
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = "SCHLIESSEN",
                            color = Color.White.copy(alpha = 0.54f),
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onConfirm?.invoke()
                        },
                        shape = RoundedCornerShape(0.dp), // Keine Rundungen (LinearBorder)
                        border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = confirmLabel,
                            color = Color(0xFFD4AF37),
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

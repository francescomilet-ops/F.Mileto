package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.ShirinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreAndAppCreatorScreen(
    viewModel: ShirinViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: AR-Vorschau, 1: AI Builder & Store, 2: AI Studios
    val subscriptionTier by viewModel.subscriptionTier.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ViewInAr,
                contentDescription = "AR Hub",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "F.M Management & Shop",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Augmented Reality Studio & AI App-Distributor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Segment Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("AR-Vorschau", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.BlurOn, contentDescription = "AR-Vorschau") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("AI-Builder", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.Architecture, contentDescription = "AI-Builder & Store") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("AI Studios", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Stars, contentDescription = "AI Studios Premium") }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> ArPreviewContent(viewModel)
                1 -> BuilderAndStoreContent(viewModel, subscriptionTier, scrollState, scope)
                2 -> AIStudiosDashboardScreen(viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// AR PREVIEW TAB COMPONENT CONTENT
@Composable
fun ArPreviewContent(viewModel: ShirinViewModel) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // 1. Interactive States
    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var scaleFactor by remember { mutableFloatStateOf(1.2f) }
    var illuminationLevel by remember { mutableFloatStateOf(0.8f) }
    var isArCameraActive by remember { mutableStateOf(false) }
    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationProgress by remember { mutableFloatStateOf(1f) }

    // 2. Foto state
    var showScreenshotDialog by remember { mutableStateOf(false) }
    var isCapturingPhoto by remember { mutableStateOf(false) }

    val productList = listOf(
        ArProduct("F.M Chronograph Noir", "Premium Roségold Luxusuhr mit Chronometer-Lünette", Icons.Filled.Watch, GoldPremium),
        ArProduct("F.M Cyber-Goggles 2026", "Holographische Brille mit integrierter Netzhautprojektion", Icons.Filled.Camera, Color.Cyan),
        ArProduct("F.M Atmos-Glide Sneaker", "Futuristischer Laufschuh mit adaptiven Laser-Dichtungen", Icons.Filled.Pets, EmeraldAlert),
        ArProduct("Glowing Quantum Sphere", "Rotierende 3D-Farb-Sphäre mit holographischem Fluss", Icons.Filled.Lightbulb, Color.Magenta)
    )

    // Infinite transition for continuous 3D model rotation
    val infiniteTransition = rememberInfiniteTransition(label = "ar_model_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Infinite indicator bounce
    val pulseBounceRef by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_bounce"
    )

    LaunchedEffect(isCalibrating) {
        if (isCalibrating) {
            calibrationProgress = 0f
            while (calibrationProgress < 1f) {
                delay(120)
                calibrationProgress += 0.1f
            }
            isCalibrating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Descriptive Header (Mirrors Flutter Request)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Augmented Reality Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hier wird die AR-Visualisierung für deine Kollektionen geladen. Richte die Kamera auf flache Oberflächen, um Kollektionsobjekte im Raum zu projizieren.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Live 3D / AR Viewport Container Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .border(2.dp, productList[selectedProductIndex].color.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Simulated Viewport Background (Black or simulated room)
            if (isArCameraActive) {
                // Draw camera scanning wave
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Dynamic grid lines to represent ground plane
                    val spacing = 35.dp.toPx()
                    var offset = 0f
                    while (offset < w) {
                        drawLine(
                            color = Color.Green.copy(alpha = 0.12f),
                            start = Offset(offset, 0f),
                            end = Offset(offset, h),
                            strokeWidth = 1f
                        )
                        offset += spacing
                    }

                    offset = 0f
                    while (offset < h) {
                        drawLine(
                            color = Color.Green.copy(alpha = 0.12f),
                            start = Offset(0f, offset),
                            end = Offset(w, offset),
                            strokeWidth = 1f
                        )
                        offset += spacing
                    }

                    // AR Surface anchor ring
                    drawCircle(
                        color = Color.Green.copy(alpha = 0.2f * pulseBounceRef),
                        center = Offset(w / 2, h / 2 + 60.dp.toPx()),
                        radius = 80.dp.toPx() * scaleFactor
                    )

                    drawCircle(
                        color = Color.Green.copy(alpha = 0.5f),
                        center = Offset(w / 2, h / 2 + 60.dp.toPx()),
                        radius = 8.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else {
                // Studio Blueprint background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val r = minOf(w, h) / 3

                    // Radial concentric target rings for alignment
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        center = Offset(w / 2, h / 2),
                        radius = r
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f),
                        center = Offset(w / 2, h / 2),
                        radius = r * 1.5f
                    )
                }
            }

            // CORE: Live canvas rendering of high-fashion 3D models with rotation, scale & illumination
            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scaleFactor)
            ) {
                val cx = size.width / 2
                val cy = size.height / 2
                val currentProductColor = productList[selectedProductIndex].color
                val activeOpacity = illuminationLevel

                // Selected Product Vector Graphics Drawing
                when (selectedProductIndex) {
                    0 -> { // Watch Model Draw
                        // Outer Gear / Bezel
                        drawCircle(
                            color = currentProductColor.copy(alpha = activeOpacity),
                            center = Offset(cx, cy),
                            radius = 65.dp.toPx(),
                            style = Stroke(width = 4.dp.toPx())
                        )
                        // Inner ring
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f * activeOpacity),
                            center = Offset(cx, cy),
                            radius = 55.dp.toPx()
                        )

                        // Sub registers
                        drawCircle(
                            color = currentProductColor.copy(alpha = 0.4f),
                            center = Offset(cx, cy - 20.dp.toPx()),
                            radius = 12.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Crown & pushers
                        drawRect(
                            color = currentProductColor.copy(alpha = activeOpacity),
                            topLeft = Offset(cx + 64.dp.toPx(), cy - 6.dp.toPx()),
                            size = Size(8.dp.toPx(), 12.dp.toPx())
                        )

                        // Rotating internal hour markings & brand logos
                        val numMarks = 12
                        for (i in 0 until numMarks) {
                            val angleRad = Math.toRadians((i * 30 + rotationAngle).toDouble())
                            val startX = cx + cos(angleRad).toFloat() * 45.dp.toPx()
                            val startY = cy + sin(angleRad).toFloat() * 45.dp.toPx()
                            val endX = cx + cos(angleRad).toFloat() * 52.dp.toPx()
                            val endY = cy + sin(angleRad).toFloat() * 52.dp.toPx()
                            drawLine(
                                color = currentProductColor.copy(alpha = 0.8f * activeOpacity),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Chronograph ticking needle (animated fast)
                        val needleAngle = Math.toRadians((rotationAngle * 3).toDouble())
                        drawLine(
                            color = Color.Red.copy(alpha = activeOpacity),
                            start = Offset(cx, cy),
                            end = Offset(
                                cx + cos(needleAngle).toFloat() * 48.dp.toPx(),
                                cy + sin(needleAngle).toFloat() * 48.dp.toPx()
                            ),
                            strokeWidth = 2.5f
                        )

                        // Center gear point
                        drawCircle(
                            color = Color.White,
                            center = Offset(cx, cy),
                            radius = 4.dp.toPx()
                        )
                    }
                    1 -> { // Cyber Goggles Drawing
                        val rotationRads = Math.toRadians(rotationAngle.toDouble())
                        // Dynamic skew width representing 3D rotation aspect
                        val stretchWidth = cos(rotationRads).toFloat() * 60.dp.toPx()

                        val path = Path().apply {
                            // Left Lens
                            moveTo(cx - 35.dp.toPx() + (stretchWidth*0.25f), cy - 10.dp.toPx())
                            lineTo(cx - 5.dp.toPx() + (stretchWidth*0.25f), cy - 10.dp.toPx())
                            lineTo(cx - 10.dp.toPx() + (stretchWidth*0.1f), cy + 15.dp.toPx())
                            lineTo(cx - 30.dp.toPx() + (stretchWidth*0.1f), cy + 15.dp.toPx())
                            close()

                            // Right Lens
                            moveTo(cx + 5.dp.toPx() - (stretchWidth*0.25f), cy - 10.dp.toPx())
                            lineTo(cx + 35.dp.toPx() - (stretchWidth*0.25f), cy - 10.dp.toPx())
                            lineTo(cx + 30.dp.toPx() - (stretchWidth*0.1f), cy + 15.dp.toPx())
                            lineTo(cx + 10.dp.toPx() - (stretchWidth*0.1f), cy + 15.dp.toPx())
                            close()
                        }

                        // Draw path (Cyber Lens frame)
                        drawPath(
                            path = path,
                            color = currentProductColor.copy(alpha = 0.25f * activeOpacity)
                        )
                        drawPath(
                            path = path,
                            color = currentProductColor.copy(alpha = activeOpacity),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Holographic lens lines
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f * activeOpacity),
                            start = Offset(cx - stretchWidth, cy),
                            end = Offset(cx + stretchWidth, cy),
                            strokeWidth = 2.dp.toPx()
                        )

                        // Connection Bridge
                        drawLine(
                            color = Color.White.copy(alpha = activeOpacity),
                            start = Offset(cx - 5.dp.toPx(), cy),
                            end = Offset(cx + 5.dp.toPx(), cy),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    2 -> { // Atmos-Glide Sneaker silhouette
                        val slideX = cos(Math.toRadians(rotationAngle.toDouble())).toFloat() * 10.dp.toPx()

                        // Custom wireframe shoe styling
                        val shoePath = Path().apply {
                            // Sole Back
                            moveTo(cx - 50.dp.toPx() + slideX, cy + 25.dp.toPx())
                            // Sole Front
                            lineTo(cx + 50.dp.toPx() + slideX, cy + 25.dp.toPx())
                            // Toe box
                            quadraticTo(
                                cx + 60.dp.toPx() + slideX, cy + 15.dp.toPx(),
                                cx + 45.dp.toPx() + slideX, cy
                            )
                            // Tongue / Ankle opening
                            lineTo(cx + 15.dp.toPx() + slideX, cy - 25.dp.toPx())
                            // Heel collar
                            lineTo(cx - 40.dp.toPx() + slideX, cy - 10.dp.toPx())
                            // Heel Counter
                            quadraticTo(
                                cx - 55.dp.toPx() + slideX, cy + 10.dp.toPx(),
                                cx - 50.dp.toPx() + slideX, cy + 25.dp.toPx()
                            )
                            close()
                        }

                        drawPath(
                            path = shoePath,
                            color = currentProductColor.copy(alpha = 0.15f * activeOpacity)
                        )
                        drawPath(
                            path = shoePath,
                            color = currentProductColor.copy(alpha = activeOpacity),
                            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Swoosh/Logo
                        drawLine(
                            color = Color.White.copy(alpha = activeOpacity),
                            start = Offset(cx - 20.dp.toPx() + slideX, cy + 5.dp.toPx()),
                            end = Offset(cx + 12.dp.toPx() + slideX, cy - 10.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    3 -> { // Glowing Color Sphere Draw
                        val angle1 = rotationAngle
                        val angle2 = -rotationAngle
                        val pulseGlowVal = pulseBounceRef

                        rotate(angle1, pivot = Offset(cx, cy)) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Cyan,
                                        Color.Magenta,
                                        Color.Yellow,
                                        Color(0xFFCCFF00), // Lime
                                        Color.Cyan
                                    ),
                                    center = Offset(cx, cy)
                                ),
                                radius = 65.dp.toPx() * pulseGlowVal,
                                center = Offset(cx, cy),
                                alpha = 0.5f * activeOpacity
                            )
                        }

                        rotate(angle2, pivot = Offset(cx, cy)) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Blue,
                                        Color(0xFF800080), // Purple
                                        Color.Red,
                                        Color(0xFFFFA500), // Orange
                                        Color.Blue
                                    ),
                                    center = Offset(cx, cy)
                                ),
                                radius = 50.dp.toPx(),
                                center = Offset(cx, cy),
                                alpha = activeOpacity
                            )
                        }

                        // Center white core
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
                                center = Offset(cx, cy),
                                radius = 25.dp.toPx()
                            ),
                            radius = 25.dp.toPx(),
                            center = Offset(cx, cy),
                            alpha = 0.9f * activeOpacity
                        )
                    }
                }
            }

            // Interactive Viewport Overlay Elements
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sektion Upper HUD row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isArCameraActive) EmeraldAlert else Color.Gray,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArCameraActive) "AR LIVE" else "STUDIO-RENDER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isArCameraActive) EmeraldAlert else Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "60 FPS • SMART HDR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }

                // Callibration Progress Indicator Overlay
                if (isCalibrating) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = EmeraldAlert
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                              "Kalibriere Oberfläche... ${(calibrationProgress * 100).toInt()}%",
                              color = Color.White,
                              style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Lower HUD overlay labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Skalierung: ${(scaleFactor * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Licht: ${(illuminationLevel * 100).toInt()} Lux",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Product Catalog Selection Rows
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "1. Kollektionsobjekt wählen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                productList.forEachIndexed { index, item ->
                    val isSelected = selectedProductIndex == index
                    OutlinedCard(
                        onClick = {
                            selectedProductIndex = index
                        },
                        modifier = Modifier
                            .width(135.dp)
                            .testTag("ar_product_tab_$index"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) item.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.color else Color.LightGray.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title.replace("F.M ", ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Object Adjustments (Sliders & Toggles)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "2. AR-Rendering Parameter kalibrieren",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Slider A: Scale Factor
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Objekt-Größe (Maßstab):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("${(scaleFactor * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = scaleFactor,
                        onValueChange = { scaleFactor = it },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = productList[selectedProductIndex].color,
                            activeTrackColor = productList[selectedProductIndex].color
                        )
                    )
                }

                // Slider B: Illumination (Lux levels)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Beleuchtungsintensität (Lux):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("${(illuminationLevel * 100).toInt()} Lux", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = illuminationLevel,
                        onValueChange = { illuminationLevel = it },
                        valueRange = 0.2f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = productList[selectedProductIndex].color,
                            activeTrackColor = productList[selectedProductIndex].color
                        )
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Toggle for simulated Live AR Screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Live AR Kamera-Projektion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Projiziert das 3D-Modell in dein reales Sichtfeld", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isArCameraActive,
                        onCheckedChange = {
                            isArCameraActive = it
                            if (it) {
                                isCalibrating = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = productList[selectedProductIndex].color,
                            checkedTrackColor = productList[selectedProductIndex].color.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // Primary Action Button (AR Live-Foto aufnehmen / Screenshot)
        Button(
            onClick = {
                isCapturingPhoto = true
                coroutineScope.launch {
                    delay(1200) // simulated shutter
                    isCapturingPhoto = false
                    showScreenshotDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("ar_photo_button"),
            colors = ButtonDefaults.buttonColors(containerColor = productList[selectedProductIndex].color),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = "AR Foto")
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isCapturingPhoto) "Belichte AR-Hologramm..." else "AR Live-Foto aufnehmen",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (selectedProductIndex == 0) Color.Black else Color.White
            )
        }
    }

    // Success Screen Dialogue Box
    if (showScreenshotDialog) {
        val activeProd = productList[selectedProductIndex]
        AlertDialog(
            onDismissRequest = { showScreenshotDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudUpload, "Schnittstelle", tint = EmeraldAlert, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Geräte-Foto synchronisiert!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Das hochauflösende 3D AR-Modell von '${activeProd.title}' wurde erfolgreich kalibriert, belichtet und in deinen Medien-Speicher abgelegt.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Card modeling visual preview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(activeProd.icon, activeProd.title, tint = activeProd.color, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("PROJEKTIONSBERICHT:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = activeProd.color)
                            Text("• Datei: F_M_Kollektion_AR_${(1000..9999).random()}.png", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Format: 3840 x 2160 Px (UltraHD)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Shader-Licht: ${(illuminationLevel * 100).toInt()}% Lux", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Skalierung: ${(scaleFactor * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScreenshotDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = activeProd.color)
                ) {
                    Text(
                        "Schließen",
                        color = if (selectedProductIndex == 0) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// ORIGINAL BUILDER & STORE CONTENT ENVELOPE
@Composable
fun BuilderAndStoreContent(
    viewModel: ShirinViewModel,
    subscriptionTier: String,
    scrollState: androidx.compose.foundation.ScrollState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    var promptInput by remember { mutableStateOf("") }
    var isAssembling by remember { mutableStateOf(false) }
    var assemblyLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var assembledAppResult by remember { mutableStateOf<String?>(null) }
    var showCheckoutDialogForTier by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MembershipStatusCard(subscriptionTier)

        // Segment A: Gemini Studios App Generator Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_creator_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Code,
                        contentDescription = "Builder Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini Studios AI App Creator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gib Shirin per Sprache oder Text die Anweisung, eine vollwertige Android-App zu kompilieren und diese in die Stores einzupflegen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("z.B. Erstelle mir eine Finanz-App mit PDF-Export...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("creator_prompt_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            isAssembling = true
                            assembledAppResult = null
                            assemblyLogs = emptyList()
                            scope.launch {
                                // Simulate App Compilation Sequence Live
                                val steps = listOf(
                                    "Strukturanalyse: Extrahiere Benutzeroberfläche...",
                                    "Kompiliere in Kotlin & Jetpack Compose Layout...",
                                    "Integriere sichere SQLite (Room) Datenbank...",
                                    "Bereite Ende-zu-Ende Verschlüsselung vor...",
                                    "Kompiliere APK und lade in App-Store (Google Play) hoch..."
                                )
                                steps.forEach { step ->
                                    assemblyLogs = assemblyLogs + step
                                    delay(900)
                                }
                                isAssembling = false
                                assembledAppResult = "✓ App erfolgreich erstellt und in die Stores hochgeladen! Name der Test-App: '${promptInput.split(" ").take(3).joinToString(" ")} App'"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assemble_app_button"),
                    enabled = !isAssembling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isAssembling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kompiliere App...")
                    } else {
                        Text("App generieren und veröffentlichen")
                    }
                }

                // Streaming Compilation Logs Terminal Preview
                if (assemblyLogs.isNotEmpty() || assembledAppResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(10.dp)
                    ) {
                        Column {
                            assemblyLogs.forEach { log ->
                                Text(
                                    text = "> $log",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Green,
                                    textAlign = TextAlign.Start
                                )
                            }
                            assembledAppResult?.let { res ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Cyan,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section B: Premium Shop
        Text(
            text = "Shirin Premium Tarife",
            style = MaterialTheme.typography.titleLarge
        )

        PricingTierCard(
            title = "Bronze Premium",
            price = "4,99 € / mtl.",
            advantages = listOf(
                "Keine API Einschränkungen",
                "Schnellere Steuer-Simulationen",
                "Export für bis zu 50 PDFs/Google Drive"
            ),
            buttonLabel = "Bronze abonnieren",
            isRecommended = false,
            onPurchase = { showCheckoutDialogForTier = "PRO" }
        )

        PricingTierCard(
            title = "Gold VIP-Lizenz",
            price = "49,99 € einmalig",
            advantages = listOf(
                "Komplette Alltags- & Steueroptimierung",
                "Unbegrenzter Gemini Studios App-KREATOR",
                "Ende-zu-Ende-verschlüsselte Backups unlimitiert",
                "Deutsch- & internationaler Support bevorzugt"
            ),
            buttonLabel = "Gold VIP freischalten",
            isRecommended = true,
            onPurchase = { showCheckoutDialogForTier = "VIP" }
        )

        val context = androidx.compose.ui.platform.LocalContext.current
        val loggedInEmail by viewModel.finPulseEmail.collectAsState()
        val isSabrina = loggedInEmail?.trim()?.equals("Sedisabrina@gmail.com", ignoreCase = true) == true

        var isAgbExpanded by remember { mutableStateOf(false) }
        var isImpressumExpanded by remember { mutableStateOf(false) }

        fun copyToClipboard(label: String, text: String) {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "$label in die Zwischenablage kopiert!", android.widget.Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PART 3: Warum sich unser Abo für dich lohnt!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("value_pitch_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Vorteile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Warum sich unser Abo lohnt! 🚀",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Das All-in-One Werkzeug für deinen Alltag!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Warum solltest du dich für unser Premium-Abo entscheiden? Ganz einfach: Weil wir Schluss machen mit dem App-Chaos auf deinem Smartphone!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val benefits = listOf(
                        "Riesige Palette an smarten Tools an einem einzigen Ort: Keine Notwendigkeit mehr, für fünf voneinander getrennte App-Abos separat zu bezahlen.",
                        "Alltag & Dokumente nahtlos koordinieren: Rechnungsprüfungen, Finanz-Szenarien und behördliche Syncs gebündelt steuern.",
                        "Echtzeit-Anfragen an Shirin AI: Nutze den speziellen Live-Sport-Button für zügige, hochgradig personalisierte Reaktionen und Datenabgleiche.",
                        "Von Handwerksprofis mitgedacht: Entwickelt für echte Macher – optimiert, um dir täglich kostbare Zeit, Geld und vor allem Nerven zu sparen."
                    )
                    
                    benefits.forEach { benefit ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 8.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Hol dir jetzt das Upgrade für dein Leben! ✨",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Special Sabrina Sedlmaier Exemption greeting active state indicator
        if (isSabrina) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sabrina_exemption_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = "Active Privilege",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Sonderregelung aktiv! 👑",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            "Sehr geehrte Frau Sabrina Sedlmaier (geb. 24.01.1991), gem. § 2 Abs. 1 AGB genießen Sie über Ihre verifizierte Adresse 'Sedisabrina@gmail.com' lebenslang unbegrenztes und kostenloses Nutzungsrecht für alle Tools.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ACCORDION 1: IMPRESSUM (Anbieterkennzeichnung)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("impressum_accordion"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isImpressumExpanded = !isImpressumExpanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = "Anbieterkennzeichnung",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Impressum & Anbieter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (isImpressumExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggeln"
                    )
                }

                if (isImpressumExpanded) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Betreiber & Company Info
                        Column {
                            Text(
                                "Dienstanbieter & Medieninhaber:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Francesco Mileto",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Unternehmen: Pfaffenhofen Workers / F.M Elektro in Pfaffenhofen",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Specialization
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "🔧 Spezialisiert auf:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Trockenbau, Design-Decken, modernste Beleuchtungssysteme, Kabelverlegung, Plug & Play-Installationen sowie Video-Überwachungskameras.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Anschrift & Email Address
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Anschrift:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Dorfstraße 24b, Göbelsbach\n85276 Pfaffenhofen an der Ilm\nDeutschland",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Kontakt:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Pfa.workers@gmail.com",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { copyToClipboard("E-Mail", "Pfa.workers@gmail.com") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kopieren", fontSize = 10.sp)
                                }
                            }
                        }

                        // Taxes Info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "🏛️ Finanzamt Pfaffenhofen Steuerdaten:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Identifikationsnummer:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("48 617 092 632", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    IconButton(
                                        onClick = { copyToClipboard("Steuer-IdNr.", "48 617 092 632") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, "Kopieren", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Steuernummer:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("154/251/60823", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    IconButton(
                                        onClick = { copyToClipboard("Steuernummer", "154/251/60823") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, "Kopieren", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Premium Revolut Bank Bankverbindung
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "💳 REVOLUT BANKVERBINDUNG (Premium-Abos)",
                                        color = Color.Cyan,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.LocalAtm,
                                        contentDescription = "Bank",
                                        tint = Color.Cyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Empfänger / Kontoinhaber:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                                Text(
                                    "Francesco Mileto",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("IBAN:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                        Text("DE58 1001 0178 5341 3019 02", fontWeight = FontWeight.ExtraBold, color = Color.Green, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(
                                        onClick = { copyToClipboard("IBAN", "DE58 1001 0178 5341 3019 02") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, "Kopieren", tint = Color.Cyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("BIC:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                        Text("REVODEB2", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(
                                        onClick = { copyToClipboard("BIC", "REVODEB2") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, "Kopieren", tint = Color.Cyan, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Institut: Revolut Bank UAB, Zweigniederlassung Deutschland",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "Anschrift: FORA Linden Palais, Unter den Linden 40, 10117 Berlin, Germany",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ACCORDION 2: ALLGEMEINE GESCHÄFTSBEDINGUNGEN (AGB)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agb_accordion"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAgbExpanded = !isAgbExpanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = "Nutzungsbedingungen",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Allg. Geschäftsbedingungen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (isAgbExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggeln"
                    )
                }

                if (isAgbExpanded) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title
                        Text(
                            "Allgemeine Geschäftsbedingungen (AGB)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        // § 1
                        Column {
                            Text(
                                "§ 1 Geltungsbereich & Urheberrecht",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Diese Allgemeinen Geschäftsbedingungen gelten für die Nutzung der mobilen Anwendung (nachfolgend „App“ genannt). " +
                                "Die App wurde eigenständig und vollständig von Francesco Mileto programmiert. Francesco Mileto ist der alleinige Eigentümer, " +
                                "Rechteinhaber und Betreiber der App. Alle Rechte am Code, dem Design und den integrierten Funktionen liegen ausschließlich bei ihm.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // § 2
                        Column {
                            Text(
                                "§ 2 Nutzungslizenzen & Sonderregelung (Exklusiv-Freischaltung)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Highlight block
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GoldPremium.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, GoldPremium),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "Sonderregelung für Frau Sabrina Sedlmaier (geb. 24.01.1991):",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC2410C)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Frau Sabrina Sedlmaier erhält aufgrund ihrer besonderen Verbindung zum Unternehmen ein lebenslanges, " +
                                        "uneingeschränktes und vollkommen kostenloses Nutzungsrecht für alle Funktionen dieser App über ihre verifizierte E-Mail-Adresse: Sedisabrina@gmail.com.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Reguläre Nutzer (Abonnement):\n" +
                                "Für alle anderen Personen und Endkunden ist die Nutzung der App sowie der erweiterten Tools kostenpflichtig. " +
                                "Die genauen Preise, Testphasen und Abolaufzeiten werden direkt in der App vor dem Kauf ausgewiesen. " +
                                "Die Zahlung erfolgt über die im Impressum hinterlegten Bankdaten (Revolut Bank) oder die angebundenen In-App-Zahlungsdienstleister.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // § 3
                        Column {
                            Text(
                                "§ 3 Leistungsumfang & Schnittstellen (Shirin AI)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Die App vereint eine Vielzahl von Werkzeugen für den Alltag, die Handwerkskoordination und die digitale Assistenz. " +
                                "Sport-Button & Shirin AI Live-Schnittstelle: Die App enthält einen speziellen \"Sport-Button\". " +
                                "Dieser stellt eine Live-Verbindung zu der KI-Komponente „Shirin AI“ her. Kunden können hierüber Anfragen stellen, " +
                                "welche direkt verarbeitet und synchronisiert werden, um einen schnellen Informationsfluss zu gewährleisten.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // § 4
                        Column {
                            Text(
                                "§ 4 Wichtiger Haftungsausschluss für Handwerksdienstleistungen",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Über die App vermittelte oder im Namen von Pfaffenhofen Workers / F.M Elektro angebotene Leistungen im Bereich der Elektrotechnik sind strikt als „Plug and Play“-Installationen beziehungsweise als rein mechanische Kabel- und Systemverlegungen (z. B. für Überwachungskameras und Beleuchtungssysteme) definiert. " +
                                    "Es werden keine Eingriffe in das öffentliche Stromnetz oder Arbeiten an Hauptzähleranlagen vorgenommen. " +
                                    "Eine Haftung für Schäden, die durch unsachgemäße Eingriffe des Nutzers in das Stromnetz entstehen, ist ausgeschlossen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // § 5
                        Column {
                            Text(
                                "§ 5 Kündigung & Laufzeit von Abonnements",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Kostenpflichtige Abonnements verlängern sich automatisch um die gewählte Laufzeit, " +
                                "sofern sie nicht vor Ablauf des Abrechnungszeitraums in den Einstellungen der App oder gegenüber dem Betreiber gekündigt werden.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        showCheckoutDialogForTier?.let { tier ->
            CheckoutDialog(
                tier = tier,
                viewModel = viewModel,
                onDismiss = { showCheckoutDialogForTier = null }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Data Classes
data class ArProduct(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun MembershipStatusCard(tier: String) {
    val tierColor = when (tier) {
        "VIP" -> GoldPremium
        "PRO" -> MaterialTheme.colorScheme.primary
        else -> Color.Gray
    }

    val tierDisplayName = when (tier) {
        "VIP" -> "Gold VIP Mitglied"
        "PRO" -> "Bronze Premium Mitglied"
        else -> "Kostenfreier Tarif"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = tierColor.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (tier == "VIP") Icons.Filled.Stars else Icons.Filled.AccountCircle,
                contentDescription = "Mitglied",
                tint = tierColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Aktueller Tarif: $tierDisplayName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tier == "VIP") GoldPremium else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (tier == "VIP") "Du hast vollen Zugriff auf den unbegrenzten App-Creator und E2EE." else "In-App Updates verwalten oder rechts abonnieren um Limitierungen aufzuheben.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PricingTierCard(
    title: String,
    price: String,
    advantages: List<String>,
    buttonLabel: String,
    isRecommended: Boolean,
    onPurchase: () -> Unit
) {
    val borderBrush = if (isRecommended) {
        Brush.linearGradient(listOf(GoldPremium, MaterialTheme.colorScheme.secondary))
    } else {
        Brush.linearGradient(listOf(Color.LightGray.copy(alpha = 0.4f), Color.LightGray.copy(alpha = 0.4f)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isRecommended) 2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("pricing_card_$title"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isRecommended) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPremium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPremium.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EMPFOHLEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPremium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                price,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                advantages.forEach { adv ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Vorteil",
                            tint = if (isRecommended) GoldPremium else EmeraldAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(adv, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPurchase,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pricing_card_button_${title.replace(" ", "_")}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecommended) GoldPremium else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isRecommended) Color.Black else Color.White
                )
            ) {
                Text(buttonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    tier: String,
    viewModel: ShirinViewModel,
    onDismiss: () -> Unit
) {
    val tierName = if (tier == "VIP") "Gold VIP-Lizenz" else "Bronze Premium"
    val tierPrice = if (tier == "VIP") "49,99 € einmalig" else "4,99 € / mtl."
    val pointsToCollect = if (tier == "VIP") 49 else 4
    
    var paymentMethod by remember { mutableStateOf("paypal") } // paypal, payback, vorkasse, sofort
    
    // Form fields
    var paypalEmail by remember { mutableStateOf("") }
    var paypalPassword by remember { mutableStateOf("") }
    var paybackNumber by remember { mutableStateOf("") }
    var collectPaybackPoints by remember { mutableStateOf(true) }
    
    // Sofort fields
    var selectedBank by remember { mutableStateOf("Sparkasse") }
    val bankOptions = listOf("Sparkasse", "Volksbank", "Deutsche Bank", "Commerzbank", "Postbank", "ING Diba", "Revolut")
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var onlineBankingPin by remember { mutableStateOf("") }
    var onlineBankingPinVisible by remember { mutableStateOf(false) }
    var isTanSent by remember { mutableStateOf(false) }
    var onlineBankingTan by remember { mutableStateOf("") }

    // Simulation states
    var isProcessing by remember { mutableStateOf(false) }
    var simStep by remember { mutableIntStateOf(0) }
    var isSuccess by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    if (isSuccess) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAlert)
                ) {
                    Text("Loslegen! 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldAlert,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Zahlung erfolgreich!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Glückwunsch! Deine Zahlung über $tierPrice wurde erfolgreich autorisiert.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Du hast den Status $tierName ab sofort freigeschaltet. Sämtliche Limitierungen in Shirin AI sowie im App-Creator (Gemini Studios) wurden aufgehoben.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (paymentMethod == "payback" && collectPaybackPoints && paybackNumber.length == 10) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Payback Points",
                                    tint = Color(0xFF0038A8),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Dir wurden $pointsToCollect Payback-Punkte auf das Konto $paybackNumber gutgeschrieben!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF0038A8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        )
    } else if (isProcessing) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { /* Don't dismiss during payment loop */ }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    val stepText = when (simStep) {
                        0 -> "Sichere Verbindung zu ${paymentMethod.uppercase()} wird aufgebaut..."
                        1 -> "Zahlungsdetails werden verschlüsselt übertragen (AES-256)..."
                        2 -> "Warungsprüfung & Autorisierung beim Anbieter..."
                        3 -> "Erstelle digitale Steuerquittung für $tierName..."
                        else -> "Zahlungsabwicklung läuft..."
                    }
                    
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Bitte schließe die App nicht.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    } else {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Kasse / Checkout 🔒",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, "Abbrechen")
                        }
                    }

                    // Product overview card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tierName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Upgrade für Shirin & Gemini Studios App Creator",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                tierPrice,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        "Wähle deine Zahlungsmethode:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Selection of internal payments
                    val methods = listOf(
                        Triple("paypal", "PayPal", Icons.Filled.Payment),
                        Triple("payback", "Payback", Icons.Filled.Stars),
                        Triple("vorkasse", "Vorkasse (Bank)", Icons.Filled.AccountBalance),
                        Triple("sofort", "Sofortüberweisung", Icons.Filled.FlashOn)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.forEach { (id, label, icon) ->
                            val isSelected = paymentMethod == id
                            Card(
                                onClick = { 
                                    paymentMethod = id 
                                    // Reset specific states
                                    isTanSent = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .width(135.dp)
                                    .height(72.dp)
                                    .testTag("checkout_method_$id")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Form section corresponding to the selected payment method
                    when (paymentMethod) {
                        "paypal" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "PayPal Login",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF003087)
                                )
                                OutlinedTextField(
                                    value = paypalEmail,
                                    onValueChange = { paypalEmail = it },
                                    label = { Text("E-Mail-Adresse") },
                                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("paypal_email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF003087),
                                        focusedLabelColor = Color(0xFF003087)
                                    )
                                )
                                OutlinedTextField(
                                    value = paypalPassword,
                                    onValueChange = { paypalPassword = it },
                                    label = { Text("Passwort") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("paypal_pass_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF003087),
                                        focusedLabelColor = Color(0xFF003087)
                                    )
                                )
                                Text(
                                    "Du wirst verschlüsselt an die PayPal-Schnittstelle weitergeleitet. Es gelten die PayPal Inc. Richtlinien.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        "payback" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Payback Kundennummer",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0038A8)
                                )
                                OutlinedTextField(
                                    value = paybackNumber,
                                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) paybackNumber = it },
                                    label = { Text("10-stellige Kundennummer") },
                                    leadingIcon = { Icon(Icons.Filled.CreditCard, null) },
                                    singleLine = true,
                                    supportingText = { Text("Zahlungsabwicklung & Punktegutschrift") },
                                    placeholder = { Text("z.B. 2123456789") },
                                    modifier = Modifier.fillMaxWidth().testTag("payback_number_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0038A8),
                                        focusedLabelColor = Color(0xFF0038A8)
                                    )
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Zusatzpunkte sichern 🎁",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "Sammle 1 Punkt pro 1€ Einkaufswert.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                            Switch(
                                                checked = collectPaybackPoints,
                                                onCheckedChange = { collectPaybackPoints = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0038A8))
                                            )
                                        }
                                        if (collectPaybackPoints) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "+$pointsToCollect Payback Punkte werden gutgeschrieben!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0038A8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "vorkasse" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Vorkasse Banküberweisung",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                val randomId = remember { (1000..9999).random() }
                                val referenceCode = "SHIRIN-$tier-$randomId"

                                Text(
                                    "Bitte überweise den fälligen Betrag manuell an die Revolut Bankverbindung von Francesco Mileto. Dein Premium-Tarif wird sofort nach Absenden deiner Zahlungsabsicht freigeschaltet (Simulation):",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Revolut Bank UAB, Germany", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Inhaber: Francesco Mileto", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                        Text("IBAN: DE58 1001 0178 5341 3019 02", color = Color.Green, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("BIC: REVODEB2", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Divider(color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Verwendungszweck (WICHTIG):", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                        Text(referenceCode, color = Color.Cyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                // Interactive GiroCode QR Code Generator
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val qrData = "BCD\n001\n1\nSCT\nREVODEB2\nFrancesco Mileto\nDE58100101785341301902\n$tierPrice\n\n\n$referenceCode"
                                    SimulatedQrCode(
                                        content = qrData,
                                        modifier = Modifier
                                            .size(105.dp)
                                            .border(1.dp, Color.LightGray)
                                            .padding(4.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "EPC-GiroCode",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Scanne diesen QR-Code mit deiner Banking-App, um alle Überweisungsdaten sofort automatisch auszufüllen.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        "sofort" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Sofortüberweisung via SOFORT GmbH",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE50051)
                                )
                                
                                if (!isTanSent) {
                                    // Step 1: Select Bank & Pin
                                    Box {
                                        OutlinedTextField(
                                            value = selectedBank,
                                            onValueChange = {},
                                            label = { Text("Wähle deine Bank") },
                                            trailingIcon = { 
                                                IconButton(onClick = { bankDropdownExpanded = true }) {
                                                    Icon(Icons.Filled.ArrowDropDown, "Wählen")
                                                }
                                            },
                                            readOnly = true,
                                            modifier = Modifier.fillMaxWidth().testTag("sofort_bank_select")
                                        )
                                        DropdownMenu(
                                            expanded = bankDropdownExpanded,
                                            onDismissRequest = { bankDropdownExpanded = false }
                                        ) {
                                            bankOptions.forEach { bank ->
                                                DropdownMenuItem(
                                                    text = { Text(bank) },
                                                    onClick = {
                                                        selectedBank = bank
                                                        bankDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = onlineBankingPin,
                                        onValueChange = { onlineBankingPin = it },
                                        label = { Text("Anmeldename / PIN für Online-Banking") },
                                        leadingIcon = { Icon(Icons.Filled.AccountBalance, null) },
                                        singleLine = true,
                                        visualTransformation = if (onlineBankingPinVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { onlineBankingPinVisible = !onlineBankingPinVisible }) {
                                                Icon(
                                                    imageVector = if (onlineBankingPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = "Sichtbarkeit umschalten"
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("sofort_pin_input")
                                    )
                                    
                                    Button(
                                        onClick = {
                                            if (onlineBankingPin.isNotBlank()) {
                                                isTanSent = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50051)),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        enabled = onlineBankingPin.isNotBlank()
                                    ) {
                                        Text("SMS-TAN anfragen", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    // Step 2: Tan input
                                    Text(
                                        "Es wurde ein SMS-Sicherheitscode (TAN) an deine bei der $selectedBank hinterlegte Telefonnummer versendet. Bitte gib den Code ein:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    OutlinedTextField(
                                        value = onlineBankingTan,
                                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) onlineBankingTan = it },
                                        label = { Text("6-stellige SMS-TAN") },
                                        leadingIcon = { Icon(Icons.Filled.Key, null) },
                                        singleLine = true,
                                        placeholder = { Text("z.B. 123456") },
                                        modifier = Modifier.fillMaxWidth().testTag("sofort_tan_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE50051),
                                            focusedLabelColor = Color(0xFFE50051)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TextButton(onClick = { isTanSent = false }) {
                                            Text("Zurück zur Bankauswahl", color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Final Buy Button
                    val isFormValid = when (paymentMethod) {
                        "paypal" -> paypalEmail.contains("@") && paypalPassword.isNotBlank()
                        "payback" -> paybackNumber.length == 10
                        "vorkasse" -> true
                        "sofort" -> isTanSent && onlineBankingTan.length == 6
                        else -> false
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                delay(900)
                                simStep = 1
                                delay(900)
                                simStep = 2
                                delay(900)
                                simStep = 3
                                delay(700)
                                viewModel.upgradeSubscription(tier)
                                isProcessing = false
                                isSuccess = true
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (paymentMethod) {
                                "paypal" -> Color(0xFF003087)
                                "payback" -> Color(0xFF0038A8)
                                "sofort" -> Color(0xFFE50051)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("checkout_confirm_btn")
                    ) {
                        val paymentLabelText = when (paymentMethod) {
                            "vorkasse" -> "Zahlungsabsicht bestätigen"
                            else -> "Kostenpflichtig bestellen"
                        }
                        Text(paymentLabelText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedQrCode(
    content: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val cellsCount = 21 // Version 1 QR code is 21x21 cells
        val cellSize = sizePx / cellsCount

        // 1. Draw a white background for high contrast
        drawRect(color = Color.White, size = Size(sizePx, sizePx))

        // 2. Helper function to draw finder pattern at (row, col) in cells
        fun drawFinderPattern(startRow: Int, startCol: Int) {
            // Outer black 7x7
            drawRect(
                color = Color.Black,
                topLeft = Offset(startCol * cellSize, startRow * cellSize),
                size = Size(cellSize * 7, cellSize * 7)
            )
            // Inner white 5x5
            drawRect(
                color = Color.White,
                topLeft = Offset((startCol + 1) * cellSize, (startRow + 1) * cellSize),
                size = Size(cellSize * 5, cellSize * 5)
            )
            // Innermost black 3x3
            drawRect(
                color = Color.Black,
                topLeft = Offset((startCol + 2) * cellSize, (startRow + 2) * cellSize),
                size = Size(cellSize * 3, cellSize * 3)
            )
        }

        // Top-Left Finder
        drawFinderPattern(0, 0)
        // Top-Right Finder
        drawFinderPattern(0, cellsCount - 7)
        // Bottom-Left Finder
        drawFinderPattern(cellsCount - 7, 0)

        // 3. Populate rest of the cells with pseudorandom blocks determined by the content hash
        val contentHash = content.hashCode()
        for (r in 0 until cellsCount) {
            for (c in 0 until cellsCount) {
                // Skip the areas of the finder patterns (top-left 7x7, top-right 7x7, bottom-left 7x7)
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= cellsCount - 8
                val inBottomLeft = r >= cellsCount - 8 && c < 8
                
                if (inTopLeft || inTopRight || inBottomLeft) continue

                // Add timing patterns (row 6, column 6 are alternating dots)
                if (r == 6 || c == 6) {
                    if ((r + c) % 2 == 0) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                    continue
                }

                // Determinant generator using coordinates and content hash string
                val isBlack = (((r * 31) xor (c * 17) xor contentHash) % 3) == 0 || 
                              (((r * c) + (contentHash and 0xFF)) % 5) == 0
                
                if (isBlack) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

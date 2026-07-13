package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.Project
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneAccentGreen
import com.example.ui.theme.StoneGold
import com.example.ui.theme.StoneBronze
import kotlin.math.sqrt

@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveStoneMap(
    projects: List<Project>,
    customers: List<Customer>,
    onProjectSelect: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map bounds of Iran roughly: Longitude 44 to 63, Latitude 25 to 40
    val minLng = 44.0
    val maxLng = 63.0
    val minLat = 25.0
    val maxLat = 40.0

    // Touch Interaction State
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(1.0f, 6.0f)
                        panOffset += pan
                    }
                }
                .pointerInput(projects, zoomLevel, panOffset) {
                    detectTapGestures { tapOffset ->
                        // Calculate tap location in geographic space to find the clicked marker
                        val sizeWidth = size.width.toFloat()
                        val sizeHeight = size.height.toFloat()

                        var closestProject: Project? = null
                        var closestDist = 50.0f // click tolerance radius in dp/pixels

                        for (project in projects) {
                            val lat = project.latitude ?: continue
                            val lng = project.longitude ?: continue

                            // Convert geographic to canvas coordinates
                            val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                            val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat()) // invert Y for Screen space

                            val basePx = normX * sizeWidth
                            val basePy = normY * sizeHeight

                            // Apply zoom and pan
                            val markerPx = (basePx - sizeWidth / 2f) * zoomLevel + sizeWidth / 2f + panOffset.x
                            val markerPy = (basePy - sizeHeight / 2f) * zoomLevel + sizeHeight / 2f + panOffset.y

                            val dx = tapOffset.x - markerPx
                            val dy = tapOffset.y - markerPy
                            val dist = sqrt(dx * dx + dy * dy)

                            if (dist < closestDist) {
                                closestDist = dist
                                closestProject = project
                            }
                        }

                        closestProject?.let {
                            onProjectSelect(it)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw stylized topo map background (Stone company slate color style)
            drawRect(color = Color(0xFF1E2225)) // Dark dark slate background

            // Draw beautiful concentric coordinate background grid
            val gridColor = Color(0xFF282E33)
            val gridStep = 50f * zoomLevel
            val startX = (panOffset.x % gridStep)
            var x = startX
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            val startY = (panOffset.y % gridStep)
            var y = startY
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // 2. Draw stylized land outline / landmarks of Iran region
            drawStylizedIranMap(width, height, zoomLevel, panOffset)

            // 3. Draw Project Markers and Label annotations
            for (project in projects) {
                val lat = project.latitude ?: continue
                val lng = project.longitude ?: continue

                // Normalize coordinate
                val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat())

                val basePx = normX * width
                val basePy = normY * height

                // Map onto zoomed and panned plane
                val markerX = (basePx - width / 2f) * zoomLevel + width / 2f + panOffset.x
                val markerY = (basePy - height / 2f) * zoomLevel + height / 2f + panOffset.y

                // Skip drawing if outside screen bounds
                if (markerX < -50 || markerX > width + 50 || markerY < -50 || markerY > height + 50) continue

                // Decide color based on status/temperature
                val customer = customers.find { it.id == project.customerId }
                val isHot = customer?.temperature == "Hot" || project.projectStatus == "Near Stone Purchase"
                val isWon = project.projectStatus == "Purchased"
                val markerColor = when {
                    isWon -> StoneAccentGreen
                    isHot -> StoneAccentRed
                    else -> StoneGold
                }

                // Draw pulsing outer glow circle for Hot projects
                if (isHot) {
                    drawCircle(
                        color = markerColor.copy(alpha = 0.3f),
                        radius = 24f * zoomLevel.coerceIn(1f, 3f),
                        center = Offset(markerX, markerY)
                    )
                }

                // Draw main pin base
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 12f,
                    center = Offset(markerX + 2f, markerY + 2f) // drop shadow
                )
                drawCircle(
                    color = markerColor,
                    radius = 9f,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(markerX, markerY)
                )

                // Draw clean localized labels beside markers at higher zoom levels
                if (zoomLevel >= 1.5f) {
                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(project.projectName),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            background = Color.Black.copy(alpha = 0.6f)
                        )
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(markerX + 15f, markerY - 20f)
                    )
                }
            }
        }

        // Mini Compass / Orientation Key overlay
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
        ) {
            // Draw visual compass marker in UI
        }
    }
}

private fun DrawScope.drawStylizedIranMap(
    width: Float,
    height: Float,
    zoom: Float,
    pan: Offset
) {
    // Stylized shape paths representing major geological features of natural stone CRM (Caspian Sea & Persian Gulf)
    val mapCenter = Offset(width / 2f, height / 2f)

    // Helper to project geographic coordinates to canvas point
    fun geoToCanvas(lng: Double, lat: Double): Offset {
        val minLng = 44.0
        val maxLng = 63.0
        val minLat = 25.0
        val maxLat = 40.0

        val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
        val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat())

        val px = normX * width
        val py = normY * height

        // Zoom and Pan calculation
        return Offset(
            (px - width / 2f) * zoom + width / 2f + pan.x,
            (py - height / 2f) * zoom + height / 2f + pan.y
        )
    }

    // 1. Draw Caspian Sea (Top Water Body)
    val caspianPath = Path().apply {
        val p1 = geoToCanvas(48.0, 41.0)
        val p2 = geoToCanvas(52.0, 41.0)
        val p3 = geoToCanvas(54.0, 37.0)
        val p4 = geoToCanvas(50.0, 37.0)
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        close()
    }
    drawPath(caspianPath, color = Color(0xFF1B3B4C).copy(alpha = 0.4f))

    // 2. Draw Persian Gulf (Bottom Left Water Body)
    val gulfPath = Path().apply {
        val p1 = geoToCanvas(48.0, 30.0)
        val p2 = geoToCanvas(52.0, 27.0)
        val p3 = geoToCanvas(56.0, 26.0)
        val p4 = geoToCanvas(57.0, 27.0)
        val p5 = geoToCanvas(56.0, 24.0)
        val p6 = geoToCanvas(50.0, 26.0)
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        lineTo(p5.x, p5.y)
        lineTo(p6.x, p6.y)
        close()
    }
    drawPath(gulfPath, color = Color(0xFF1B3B4C).copy(alpha = 0.4f))

    // 3. Highlight Stone Resource Hotspots (e.g. Mahallat - Travertine Center, Dehbid - Beige Marble)
    val mahallat = geoToCanvas(50.45, 33.91)
    val dehbid = geoToCanvas(53.21, 30.65)
    val yazd = geoToCanvas(54.36, 31.89)

    // Draw stone quarries labels/markers for contextual sales richness
    drawHotspot(mahallat, "معادن تراورتن محلات", zoom)
    drawHotspot(dehbid, "معادن مرمریت دهبید", zoom)
    drawHotspot(yazd, "معادن لایمستون و گرانیت یزد", zoom)
}

private fun DrawScope.drawHotspot(center: Offset, label: String, zoom: Float) {
    if (center.x < 0 || center.x > size.width || center.y < 0 || center.y > size.height) return

    // Small dashed quarry icon
    drawCircle(
        color = StoneGold.copy(alpha = 0.15f),
        radius = 35f * zoom.coerceIn(1f, 2f),
        center = center,
        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    )
    drawCircle(
        color = StoneGold.copy(alpha = 0.6f),
        radius = 4f,
        center = center
    )
}

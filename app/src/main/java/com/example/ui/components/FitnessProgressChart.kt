package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FitnessLog
import com.example.ui.theme.OnDarkText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChartPoint(
    val label: String,
    val value: Double,
    val timestamp: Long
)

@Composable
fun FitnessProgressChart(
    logs: List<FitnessLog>,
    metricType: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Generate the last 7 days of data points
    val chartPoints = remember(logs, metricType) {
        val calendar = Calendar.getInstance()
        val points = mutableListOf<ChartPoint>()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        // Create entries for the last 7 days in chronological order
        for (i in 6 downTo 0) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -i)
            
            val startOfDay = dateCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = dateCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayLogs = logs.filter {
                it.type == metricType && it.timestamp in startOfDay..endOfDay
            }

            val dayValue = when (metricType) {
                "WEIGHT" -> {
                    // For weight, carry over the last known weight log before endOfDay if none today
                    if (dayLogs.isNotEmpty()) {
                        dayLogs.first().value
                    } else {
                        val pastLogs = logs.filter { it.type == "WEIGHT" && it.timestamp < startOfDay }
                        pastLogs.firstOrNull()?.value ?: 0.0
                    }
                }
                else -> {
                    // For other metrics, sum the values of the day
                    dayLogs.sumOf { it.value }
                }
            }

            points.add(
                ChartPoint(
                    label = sdf.format(dateCal.time),
                    value = dayValue,
                    timestamp = startOfDay
                )
            )
        }
        points
    }

    // Filter out zero entries if we want to determine min/max range correctly
    val activeValues = chartPoints.map { it.value }
    val maxVal = activeValues.maxOrNull() ?: 0.0
    val minVal = if (metricType == "WEIGHT") {
        val nonZero = activeValues.filter { it > 0.0 }
        if (nonZero.isNotEmpty()) nonZero.minOrNull() ?: 0.0 else 0.0
    } else {
        0.0
    }

    val chartMax = if (maxVal == 0.0) 100.0 else maxVal * 1.15
    val chartMin = if (metricType == "WEIGHT" && minVal > 0.0) minVal * 0.9 else 0.0

    val range = chartMax - chartMin

    // Empty State Check
    val hasData = logs.any { it.type == metricType }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        if (!hasData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Draw a subtle placeholder grid
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        
                        // Faint gridlines
                        for (i in 0..3) {
                            val y = height * (i / 3f)
                            drawLine(
                                color = onSurfaceColor.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No log records for $metricType yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the '+' button in Goals or Logs to add some stats!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Selected point for interaction
            var selectedIndex by remember(metricType) { mutableStateOf<Int?>(null) }
            
            // Animation for line reveal
            var animationProgress by remember { mutableStateOf(0f) }
            val animatedProgress by animateFloatAsState(
                targetValue = animationProgress,
                animationSpec = tween(durationMillis = 800),
                label = "chartReveal"
            )

            LaunchedEffect(metricType) {
                animationProgress = 0f
                animationProgress = 1f
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header info of Selected Log Point
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (selectedIndex != null && selectedIndex!! in chartPoints.indices) {
                        val pt = chartPoints[selectedIndex!!]
                        Text(
                            text = "${pt.label}: ${String.format(Locale.getDefault(), "%.1f", pt.value)} $unit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    } else {
                        Text(
                            text = "Last 7 Days (Tap nodes to view values)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chartPoints) {
                                detectTapGestures { offset ->
                                    val width = size.width
                                    val xOffsetStart = 40.dp.toPx()
                                    val xOffsetEnd = 16.dp.toPx()
                                    val usableWidth = width - xOffsetStart - xOffsetEnd
                                    val stepX = usableWidth / 6f

                                    // Find closest point
                                    var closestIdx = -1
                                    var minDist = Float.MAX_VALUE

                                    for (i in 0..6) {
                                        val nodeX = xOffsetStart + (i * stepX)
                                        val dist = Math.abs(offset.x - nodeX)
                                        if (dist < minDist) {
                                            minDist = dist
                                            closestIdx = i
                                        }
                                    }

                                    if (closestIdx != -1 && minDist < 40.dp.toPx()) {
                                        selectedIndex = closestIdx
                                    } else {
                                        selectedIndex = null
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height

                        val paddingLeft = 40.dp.toPx()
                        val paddingBottom = 24.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val paddingRight = 16.dp.toPx()

                        val chartHeight = height - paddingTop - paddingBottom
                        val chartWidth = width - paddingLeft - paddingRight

                        // Draw Grid Lines (Y axis splits)
                        val gridCount = 4
                        for (i in 0 until gridCount) {
                            val ratio = i.toFloat() / (gridCount - 1)
                            val y = paddingTop + chartHeight * (1f - ratio)
                            val gridValue = chartMin + (range * ratio)

                            // Grid line
                            drawLine(
                                color = onSurfaceColor.copy(alpha = 0.08f),
                                start = Offset(paddingLeft, y),
                                end = Offset(width - paddingRight, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // Y value labels
                            drawText(
                                textMeasurer = textMeasurer,
                                text = String.format(Locale.getDefault(), "%.0f", gridValue),
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = onSurfaceColor.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium
                                ),
                                topLeft = Offset(4.dp.toPx(), y - 6.dp.toPx())
                            )
                        }

                        // Calculate pixel coordinates for all nodes
                        val points = mutableListOf<Offset>()
                        val stepX = chartWidth / 6f

                        for (i in 0..6) {
                            val valRatio = if (range != 0.0) {
                                ((chartPoints[i].value - chartMin) / range).toFloat()
                            } else {
                                0.5f
                            }
                            // Keep value within animated progression
                            val animatedRatio = valRatio * animatedProgress
                            
                            val x = paddingLeft + (i * stepX)
                            val y = paddingTop + chartHeight * (1f - animatedRatio)
                            points.add(Offset(x, y))
                        }

                        // Draw Gradient fill below curve
                        if (points.isNotEmpty()) {
                            val fillPath = Path().apply {
                                moveTo(points[0].x, paddingTop + chartHeight)
                                for (i in 0 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                                lineTo(points.last().x, paddingTop + chartHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.00f)
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        // Draw Curve line
                        if (points.size > 1) {
                            val strokePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                // Standard curve tracing
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    // Control points for smooth bezier cubic curve
                                    val controlX1 = (p0.x + p1.x) / 2f
                                    val controlY1 = p0.y
                                    val controlX2 = (p0.x + p1.x) / 2f
                                    val controlY2 = p1.y
                                    
                                    cubicTo(
                                        controlX1, controlY1,
                                        controlX2, controlY2,
                                        p1.x, p1.y
                                    )
                                }
                            }

                            drawPath(
                                path = strokePath,
                                color = primaryColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // Draw Interaction overlay
                        selectedIndex?.let { index ->
                            if (index in points.indices) {
                                val pt = points[index]
                                
                                // Draw vertical tracker line
                                drawLine(
                                    color = secondaryColor.copy(alpha = 0.6f),
                                    start = Offset(pt.x, paddingTop),
                                    end = Offset(pt.x, paddingTop + chartHeight),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                // Highlight node ring
                                drawCircle(
                                    color = secondaryColor,
                                    radius = 7.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 3.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }

                        // Draw Standard Nodes
                        for (i in points.indices) {
                            if (selectedIndex != i) {
                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = points[i]
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 1.5.dp.toPx(),
                                    center = points[i]
                                )
                            }
                        }

                        // Draw X Labels (Days)
                        for (i in 0..6) {
                            val x = paddingLeft + (i * stepX)
                            val label = chartPoints[i].label

                            drawText(
                                textMeasurer = textMeasurer,
                                text = label,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = onSurfaceColor.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                ),
                                topLeft = Offset(x - 10.dp.toPx(), paddingTop + chartHeight + 6.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.FitnessGoal
import com.example.data.models.FitnessLog
import com.example.ui.components.FitnessProgressChart
import com.example.ui.viewmodel.FitnessViewModel
import com.example.ui.viewmodel.GoalWithProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DashboardTab {
    GOALS, LOGS, CHARTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessDashboardScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goalsWithProgress.collectAsStateWithLifecycle()
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(DashboardTab.GOALS) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddLogDialog by remember { mutableStateOf(false) }
    var preselectedLogType by remember { mutableStateOf<String?>(null) }
    var preselectedGoalId by remember { mutableStateOf<Int?>(null) }

    // Onboarding starter data seed if empty database
    LaunchedEffect(goals, logs) {
        if (goals.isEmpty() && logs.isEmpty()) {
            // Seed 3 common starter goals
            viewModel.addGoal("Daily Steps target", "STEPS", 10000.0, "steps", 30)
            viewModel.addGoal("Keep Hydrated", "WATER", 2500.0, "ml", 30)
            viewModel.addGoal("Burn calories", "CALORIES", 600.0, "kcal", 14)
            // Seed a couple of starter logs so charts show beautiful progress from the get go
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            viewModel.addLog(null, "STEPS", 6500.0, "Morning brisk walk")
            viewModel.addLog(null, "STEPS", 8200.0, "Afternoon park stroll")
            viewModel.addLog(null, "WATER", 1500.0, "Hydrating through work")
            viewModel.addLog(null, "CALORIES", 450.0, "Cardio session")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Fitness Goals",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.GOALS,
                    onClick = { selectedTab = DashboardTab.GOALS },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Goals Dashboard") },
                    label = { Text("Goals", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.LOGS,
                    onClick = { selectedTab = DashboardTab.LOGS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Activity Logs") },
                    label = { Text("Logs", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.CHARTS,
                    onClick = { selectedTab = DashboardTab.CHARTS },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Progress Analytics") },
                    label = { Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                DashboardTab.GOALS -> {
                    GoalsTabScreen(
                        goals = goals,
                        onAddGoalClick = { showAddGoalDialog = true },
                        onQuickLogClick = { type, goalId ->
                            preselectedLogType = type
                            preselectedGoalId = goalId
                            showAddLogDialog = true
                        },
                        onDeleteGoal = { viewModel.deleteGoal(it) }
                    )
                }
                DashboardTab.LOGS -> {
                    LogsTabScreen(
                        logs = logs,
                        onAddLogClick = {
                            preselectedLogType = null
                            preselectedGoalId = null
                            showAddLogDialog = true
                        },
                        onDeleteLog = { viewModel.deleteLog(it) }
                    )
                }
                DashboardTab.CHARTS -> {
                    ChartsTabScreen(
                        logs = logs,
                        goals = goals
                    )
                }
            }
        }
    }

    // Interactive Dialogs
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, type, target, unit, days ->
                viewModel.addGoal(title, type, target, unit, days)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddLogDialog) {
        AddLogDialog(
            preselectedType = preselectedLogType,
            preselectedGoalId = preselectedGoalId,
            onDismiss = { showAddLogDialog = false },
            onConfirm = { goalId, type, value, notes ->
                viewModel.addLog(goalId, type, value, notes)
                showAddLogDialog = false
            }
        )
    }
}

// ==========================================
// GOALS DASHBOARD SCREEN
// ==========================================
@Composable
fun GoalsTabScreen(
    goals: List<GoalWithProgress>,
    onAddGoalClick: () -> Unit,
    onQuickLogClick: (String, Int) -> Unit,
    onDeleteGoal: (FitnessGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Hero Streak summary
        GoalsSummaryHero(goals = goals)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Core Targets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = onAddGoalClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Goal")
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No goals added yet.", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Define a target like 10k steps to jumpstart your fitness journey!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(goals) { goalWithProgress ->
                    GoalCard(
                        item = goalWithProgress,
                        onQuickLog = { onQuickLogClick(goalWithProgress.goal.type, goalWithProgress.goal.id) },
                        onDelete = { onDeleteGoal(goalWithProgress.goal) }
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsSummaryHero(goals: List<GoalWithProgress>) {
    val stepsGoal = goals.find { it.goal.type == "STEPS" }
    val stepsValue = stepsGoal?.currentValue?.toInt() ?: 8432
    val stepsTarget = stepsGoal?.goal?.targetValue?.toInt() ?: 10000
    val stepsRemaining = (stepsTarget - stepsValue).coerceAtLeast(0)
    val progressPercent = if (stepsTarget > 0) (stepsValue.toFloat() / stepsTarget).coerceIn(0f, 1f) else 0.84f

    val caloriesGoal = goals.find { it.goal.type == "CALORIES" }
    val caloriesValue = caloriesGoal?.currentValue?.toInt() ?: 482

    val waterGoal = goals.find { it.goal.type == "WATER" }
    val waterLiters = if (waterGoal != null) waterGoal.currentValue / 1000.0 else 1.8

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Activity Metric
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "DAILY ACTIVITY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%,d", stepsValue),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "STEPS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Custom Progress Bar matching html: bg-[#EADDFF] h-3 rounded-full mt-2 overflow-hidden
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEADDFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressPercent)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (stepsRemaining > 0) {
                    "$stepsRemaining steps to reach your daily goal"
                } else {
                    "Daily step goal fully smashed! Great work!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }

        // Quick Stats Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Kcal Burned
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFF1D192B),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "$caloriesValue",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF1D192B),
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "KCAL BURNED",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1D192B).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Card 2: Liters Water
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E3FD))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF041E49),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", waterLiters),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF041E49),
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "LITERS WATER",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF041E49).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalCard(
    item: GoalWithProgress,
    onQuickLog: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = item.goal
    val isCompleted = item.progressPercent >= 1.0f

    val icon = when (goal.type) {
        "STEPS" -> Icons.Default.DirectionsRun
        "WORKOUTS" -> Icons.Default.FitnessCenter
        "WEIGHT" -> Icons.Default.Scale
        "WATER" -> Icons.Default.WaterDrop
        "CALORIES" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.Star
    }

    val iconColor = when (goal.type) {
        "STEPS" -> Color(0xFFFECA57)
        "WORKOUTS" -> Color(0xFFFF6B6B)
        "WEIGHT" -> Color(0xFF10AC84)
        "WATER" -> Color(0xFF2E86DE)
        "CALORIES" -> Color(0xFFFF9F43)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goal Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Target: ${goal.targetValue.toInt()} ${goal.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Delete Action Button (Minimum 48dp Touch Area)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: ${String.format(Locale.getDefault(), "%.1f", item.currentValue)} ${goal.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(item.progressPercent * 100).toInt()}% completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Custom Styled Progress Bar
            LinearProgressIndicator(
                progress = { item.progressPercent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) MaterialTheme.colorScheme.primary else iconColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Quick add log towards this specific goal
            Button(
                onClick = onQuickLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor.copy(alpha = 0.1f),
                    contentColor = iconColor
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Progress", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ==========================================
// ACTIVITY LOGSCREEN
// ==========================================
@Composable
fun LogsTabScreen(
    logs: List<FitnessLog>,
    onAddLogClick: () -> Unit,
    onDeleteLog: (FitnessLog) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity Logs Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = onAddLogClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Entry", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No activities logged yet.", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Keep track of your workout achievements here!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(logs) { log ->
                    LogItemRow(log = log, onDelete = { onDeleteLog(log) })
                }
            }
        }
    }
}

@Composable
fun LogItemRow(log: FitnessLog, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(log.timestamp))

    val icon = when (log.type) {
        "STEPS" -> Icons.Default.DirectionsRun
        "WORKOUTS" -> Icons.Default.FitnessCenter
        "WEIGHT" -> Icons.Default.Scale
        "WATER" -> Icons.Default.WaterDrop
        "CALORIES" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.Star
    }

    val iconColor = when (log.type) {
        "STEPS" -> Color(0xFFFECA57)
        "WORKOUTS" -> Color(0xFFFF6B6B)
        "WEIGHT" -> Color(0xFF10AC84)
        "WATER" -> Color(0xFF2E86DE)
        "CALORIES" -> Color(0xFFFF9F43)
        else -> MaterialTheme.colorScheme.primary
    }

    val unitLabel = when (log.type) {
        "STEPS" -> "steps"
        "WORKOUTS" -> "sessions"
        "WEIGHT" -> "kg"
        "WATER" -> "ml"
        "CALORIES" -> "kcal"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${log.value.toInt()} $unitLabel",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (log.notes.isNotEmpty()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Deletion area - complies with touch standards
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Log",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// PROGRESS CHARTS & ANALYTICS
// ==========================================
@Composable
fun ChartsTabScreen(
    logs: List<FitnessLog>,
    goals: List<GoalWithProgress>
) {
    var selectedMetric by remember { mutableStateOf("STEPS") }

    val metricOptions = listOf(
        "STEPS" to "Steps",
        "WATER" to "Hydration",
        "CALORIES" to "Calories",
        "WEIGHT" to "Weight"
    )

    val currentUnit = when (selectedMetric) {
        "STEPS" -> "steps"
        "WATER" -> "ml"
        "CALORIES" -> "kcal"
        "WEIGHT" -> "kg"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Progress Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Segmented Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            metricOptions.forEach { (type, label) ->
                val isSelected = selectedMetric == type
                val chipColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipColor)
                        .clickable { selectedMetric = type },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Custom Interactive Progress Chart
        FitnessProgressChart(
            logs = logs,
            metricType = selectedMetric,
            unit = currentUnit,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats summary list
        Text(
            text = "Metric Achievements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // Calculate Average & High Values for summary card
                val metricLogs = logs.filter { it.type == selectedMetric }
                val totalValue = metricLogs.sumOf { it.value }
                val highestValue = if (metricLogs.isNotEmpty()) metricLogs.maxOf { it.value } else 0.0
                
                val avgLabel = when (selectedMetric) {
                    "WEIGHT" -> "Latest Weight"
                    else -> "Total Volume"
                }
                
                val avgVal = when (selectedMetric) {
                    "WEIGHT" -> metricLogs.firstOrNull()?.value ?: 0.0
                    else -> totalValue
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Summary statistics for $selectedMetric", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = avgLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", avgVal)} $currentUnit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Highest Log", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", highestValue)} $currentUnit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE ADD GOAL DIALOG
// ==========================================
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("STEPS") }
    var targetValue by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf("30") }

    val types = listOf(
        "STEPS" to "Steps (steps)",
        "WORKOUTS" to "Workouts (sessions)",
        "WATER" to "Hydration (ml)",
        "CALORIES" to "Calories (kcal)",
        "WEIGHT" to "Weight Target (kg)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Establish Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Goal Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Morning jogging)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Goal Type Chips Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Goal Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    types.forEach { (tCode, tDesc) ->
                        val isSelected = type == tCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { type = tCode }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { type = tCode })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tDesc, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Value
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Goal Value") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Days
                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { durationDays = it },
                    label = { Text("Goal Duration (Days)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val targetD = targetValue.toDoubleOrNull() ?: 0.0
                            val days = durationDays.toIntOrNull() ?: 30
                            val unit = when (type) {
                                "STEPS" -> "steps"
                                "WORKOUTS" -> "sessions"
                                "WATER" -> "ml"
                                "CALORIES" -> "kcal"
                                "WEIGHT" -> "kg"
                                else -> ""
                            }
                            if (title.isNotEmpty() && targetD > 0.0) {
                                onConfirm(title, type, targetD, unit, days)
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE ADD LOG DIALOG
// ==========================================
@Composable
fun AddLogDialog(
    preselectedType: String?,
    preselectedGoalId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?, String, Double, String) -> Unit
) {
    var type by remember { mutableStateOf(preselectedType ?: "STEPS") }
    var valueStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val types = listOf(
        "STEPS" to "Steps (steps)",
        "WORKOUTS" to "Workout (sessions)",
        "WATER" to "Water (ml)",
        "CALORIES" to "Calories Burned (kcal)",
        "WEIGHT" to "Body Weight (kg)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Entry Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Log Category Selector (only if not preselected)
                if (preselectedType == null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Select Entry Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        types.forEach { (tCode, tDesc) ->
                            val isSelected = type == tCode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { type = tCode }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                                ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { type = tCode },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = tDesc, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // Show preselected label
                    Text(
                        text = "Adding progress for: ${types.find { it.first == type }?.second ?: type}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Log Value
                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text("Recorded Value") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Log notes / details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val loggedValue = valueStr.toDoubleOrNull() ?: 0.0
                            if (loggedValue > 0.0) {
                                onConfirm(preselectedGoalId, type, loggedValue, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log Entry", color = Color.White)
                    }
                }
            }
        }
    }
}

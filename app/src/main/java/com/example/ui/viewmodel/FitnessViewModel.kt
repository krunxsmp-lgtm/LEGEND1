package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.FitnessGoal
import com.example.data.models.FitnessLog
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class GoalWithProgress(
    val goal: FitnessGoal,
    val currentValue: Double,
    val progressPercent: Float
)

class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    val allGoals: StateFlow<List<FitnessGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<FitnessLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine goals and logs to calculate live progress
    val goalsWithProgress: StateFlow<List<GoalWithProgress>> = combine(allGoals, allLogs) { goals, logs ->
        goals.map { goal ->
            // Filter logs relevant to this goal
            val relevantLogs = logs.filter { log ->
                log.goalId == goal.id || (log.type == goal.type && log.timestamp >= goal.startDate && log.timestamp <= goal.targetDate)
            }

            val currentValue = when (goal.type) {
                "WEIGHT" -> {
                    // For weight, take the latest logged value, or the target if none exists yet
                    relevantLogs.firstOrNull()?.value ?: 0.0
                }
                else -> {
                    // For steps, workouts, water, calories, sum the values
                    relevantLogs.sumOf { it.value }
                }
            }

            val progressPercent = if (goal.targetValue > 0) {
                if (goal.type == "WEIGHT") {
                    // Weight goal: progress is based on reaching the goal.
                    // If we don't have previous logs, just show a completion status if current <= target (for weight loss)
                    // or current >= target (for weight gain).
                    // To make it simple and visual, let's check if they have reached the goal:
                    if (currentValue == 0.0) {
                        0f
                    } else {
                        // Let's assume progress is closer to 100% as current gets closer to target
                        val diff = Math.abs(currentValue - goal.targetValue)
                        if (diff == 0.0) {
                            1f
                        } else {
                            val ratio = goal.targetValue / currentValue
                            if (ratio > 1.0) (1.0 / ratio).toFloat() else ratio.toFloat()
                        }
                    }
                } else {
                    (currentValue / goal.targetValue).toFloat()
                }
            } else {
                0f
            }

            GoalWithProgress(
                goal = goal,
                currentValue = currentValue,
                progressPercent = progressPercent.coerceIn(0f, 1.2f) // cap visual progress bar but allow slight overflow for extra credit!
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CRUD for Goals
    fun addGoal(title: String, type: String, targetValue: Double, unit: String, daysDuration: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, daysDuration)
            val targetDate = calendar.timeInMillis

            val goal = FitnessGoal(
                title = title,
                type = type,
                targetValue = targetValue,
                unit = unit,
                startDate = startDate,
                targetDate = targetDate
            )
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(goal: FitnessGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // CRUD for Logs
    fun addLog(goalId: Int?, type: String, value: Double, notes: String) {
        viewModelScope.launch {
            val log = FitnessLog(
                goalId = goalId,
                type = type,
                value = value,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertLog(log)
        }
    }

    fun deleteLog(log: FitnessLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }
}

class FitnessViewModelFactory(private val repository: FitnessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

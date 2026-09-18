package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_goals")
data class FitnessGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "STEPS", "WEIGHT", "WORKOUTS", "WATER", "CALORIES"
    val targetValue: Double,
    val unit: String, // "steps", "kg", "sessions", "ml", "kcal"
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long,
    val isCompleted: Boolean = false
)

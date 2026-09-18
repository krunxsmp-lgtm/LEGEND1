package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_logs")
data class FitnessLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int? = null,
    val type: String, // "STEPS", "WEIGHT", "WORKOUTS", "WATER", "CALORIES"
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

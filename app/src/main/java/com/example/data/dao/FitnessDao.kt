package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.FitnessGoal
import com.example.data.models.FitnessLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // --- Goals Queries ---
    @Query("SELECT * FROM fitness_goals ORDER BY startDate DESC")
    fun getAllGoals(): Flow<List<FitnessGoal>>

    @Query("SELECT * FROM fitness_goals WHERE id = :id")
    fun getGoalById(id: Int): Flow<FitnessGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FitnessGoal): Long

    @Update
    suspend fun updateGoal(goal: FitnessGoal)

    @Delete
    suspend fun deleteGoal(goal: FitnessGoal)

    // --- Logs Queries ---
    @Query("SELECT * FROM fitness_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FitnessLog>>

    @Query("SELECT * FROM fitness_logs WHERE type = :type ORDER BY timestamp DESC")
    fun getLogsForType(type: String): Flow<List<FitnessLog>>

    @Query("SELECT * FROM fitness_logs WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getLogsForGoal(goalId: Int): Flow<List<FitnessLog>>

    @Query("SELECT * FROM fitness_logs WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLogForType(type: String): Flow<FitnessLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FitnessLog): Long

    @Delete
    suspend fun deleteLog(log: FitnessLog)
}

package com.example.data.repository

import com.example.data.dao.FitnessDao
import com.example.data.models.FitnessGoal
import com.example.data.models.FitnessLog
import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val allGoals: Flow<List<FitnessGoal>> = fitnessDao.getAllGoals()
    val allLogs: Flow<List<FitnessLog>> = fitnessDao.getAllLogs()

    fun getGoalById(id: Int): Flow<FitnessGoal?> {
        return fitnessDao.getGoalById(id)
    }

    suspend fun insertGoal(goal: FitnessGoal): Long {
        return fitnessDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: FitnessGoal) {
        fitnessDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: FitnessGoal) {
        fitnessDao.deleteGoal(goal)
    }

    fun getLogsForType(type: String): Flow<List<FitnessLog>> {
        return fitnessDao.getLogsForType(type)
    }

    fun getLogsForGoal(goalId: Int): Flow<List<FitnessLog>> {
        return fitnessDao.getLogsForGoal(goalId)
    }

    fun getLatestLogForType(type: String): Flow<FitnessLog?> {
        return fitnessDao.getLatestLogForType(type)
    }

    suspend fun insertLog(log: FitnessLog): Long {
        return fitnessDao.insertLog(log)
    }

    suspend fun deleteLog(log: FitnessLog) {
        fitnessDao.deleteLog(log)
    }
}

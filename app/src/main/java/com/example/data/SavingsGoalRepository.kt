package com.example.data

import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository(private val dao: SavingsGoalDao) {
    val allGoals: Flow<List<SavingsGoal>> = dao.getAllGoals()

    suspend fun insert(goal: SavingsGoal) {
        dao.insertGoal(goal)
    }

    suspend fun update(goal: SavingsGoal) {
        dao.updateGoal(goal)
    }

    suspend fun delete(goal: SavingsGoal) {
        dao.deleteGoal(goal)
    }

    suspend fun deleteAll() {
        dao.deleteAllGoals()
    }
}

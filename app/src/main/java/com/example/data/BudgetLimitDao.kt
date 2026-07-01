package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetLimitDao {
    @Query("SELECT * FROM budget_limits")
    fun getAllBudgetLimits(): Flow<List<BudgetLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetLimit(limit: BudgetLimit)

    @Update
    suspend fun updateBudgetLimit(limit: BudgetLimit)

    @Delete
    suspend fun deleteBudgetLimit(limit: BudgetLimit)
    
    @Query("SELECT * FROM budget_limits WHERE methodName = :methodName LIMIT 1")
    suspend fun getLimitByMethod(methodName: String): BudgetLimit?
}

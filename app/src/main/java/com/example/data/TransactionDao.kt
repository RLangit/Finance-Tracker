package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT DISTINCT category FROM transactions WHERE type = :type")
    fun getDistinctCategories(type: TransactionType): Flow<List<String>>

    @Query("SELECT DISTINCT paymentMethod FROM transactions")
    fun getDistinctPaymentMethods(): Flow<List<String>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

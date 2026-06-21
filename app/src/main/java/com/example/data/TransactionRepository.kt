package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    fun getDistinctCategories(type: TransactionType): Flow<List<String>> {
        return transactionDao.getDistinctCategories(type)
    }

    fun getDistinctPaymentMethods(): Flow<List<String>> {
        return transactionDao.getDistinctPaymentMethods()
    }

    suspend fun deleteAll() {
        transactionDao.deleteAllTransactions()
    }
}

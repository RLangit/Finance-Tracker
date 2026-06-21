package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    PEMASUKAN, // Income
    PENGELUARAN, // Expense
    TRANSFER // Transfer between accounts
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val paymentMethod: String,
    val category: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

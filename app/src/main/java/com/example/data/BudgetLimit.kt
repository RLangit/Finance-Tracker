package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class BudgetLimit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val methodName: String,
    val limitAmount: Double? = null,
    val limitPercentage: Double? = null
)

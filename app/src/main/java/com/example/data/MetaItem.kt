package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
    METHOD, INCOME_CATEGORY, EXPENSE_CATEGORY
}

@Entity(tableName = "meta_items")
data class MetaItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: ItemType,
    val colorHex: String
)

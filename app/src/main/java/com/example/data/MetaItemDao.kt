package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaItemDao {
    @Query("SELECT * FROM meta_items")
    fun getAllMetaItems(): Flow<List<MetaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetaItem(item: MetaItem)

    @Update
    suspend fun updateMetaItem(item: MetaItem)

    @Delete
    suspend fun deleteMetaItem(item: MetaItem)
}

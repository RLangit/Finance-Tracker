package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaItemDao {
    @Query("SELECT * FROM meta_items ORDER BY displayOrder ASC")
    fun getAllMetaItems(): Flow<List<MetaItem>>

    @Query("SELECT * FROM meta_items WHERE type = :type ORDER BY displayOrder ASC")
    suspend fun getMetaItemsByTypeSnapshot(type: ItemType): List<MetaItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetaItem(item: MetaItem)

    @Update
    suspend fun updateMetaItem(item: MetaItem)

    @Update
    suspend fun updateAll(items: List<MetaItem>)

    @Delete
    suspend fun deleteMetaItem(item: MetaItem)
}

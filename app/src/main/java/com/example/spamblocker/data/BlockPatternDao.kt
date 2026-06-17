package com.example.spamblocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockPatternDao {

    @Query("SELECT * FROM block_patterns ORDER BY createdAt DESC")
    fun getAllPatterns(): Flow<List<BlockPattern>>

    @Query("SELECT * FROM block_patterns WHERE enabled = 1")
    suspend fun getEnabledPatterns(): List<BlockPattern>

    @Insert
    suspend fun insert(pattern: BlockPattern): Long

    @Update
    suspend fun update(pattern: BlockPattern)

    @Delete
    suspend fun delete(pattern: BlockPattern)

    @Query("DELETE FROM block_patterns WHERE id = :id")
    suspend fun deleteById(id: Long)
}

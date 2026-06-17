package com.example.spamblocker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockSettingsDao {

    @Query("SELECT * FROM block_settings WHERE id = 1")
    fun getSettings(): Flow<BlockSettings?>

    @Query("SELECT * FROM block_settings WHERE id = 1")
    suspend fun getSettingsOnce(): BlockSettings?

    @Upsert
    suspend fun upsert(settings: BlockSettings)
}
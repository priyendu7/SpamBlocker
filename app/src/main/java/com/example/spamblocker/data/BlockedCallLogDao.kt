package com.example.spamblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallLogDao {

    @Query("SELECT * FROM blocked_call_logs ORDER BY blockedAt DESC")
    fun getAllBlockedCalls(): Flow<List<BlockedCallLog>>

    @Insert
    suspend fun insert(log: BlockedCallLog)

    @Query("DELETE FROM blocked_call_logs")
    suspend fun clearAll()
}
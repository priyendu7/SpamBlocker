package com.example.spamblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_call_logs")
data class BlockedCallLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val matchedPattern: String,
    val matchType: MatchType,
    val isUnknownNumberBlock: Boolean = false,
    val blockedAt: Long = System.currentTimeMillis()
)

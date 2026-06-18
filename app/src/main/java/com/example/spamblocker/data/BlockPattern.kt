package com.example.spamblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MatchType {
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS
}

@Entity(tableName = "block_patterns")
data class BlockPattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    val matchType: MatchType,
    val createdAt: Long = System.currentTimeMillis(),
    val enabled: Boolean = true
)

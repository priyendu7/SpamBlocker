package com.example.spamblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_settings")
data class BlockSettings(
    @PrimaryKey
    val id: Int = 1, // singleton row
    val blockUnknownSim1: Boolean = false,
    val blockUnknownSim2: Boolean = false
)
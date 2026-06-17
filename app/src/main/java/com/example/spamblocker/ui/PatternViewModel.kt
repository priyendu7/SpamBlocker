package com.example.spamblocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spamblocker.data.BlockPattern
import com.example.spamblocker.data.BlockRepository
import com.example.spamblocker.data.MatchType
import com.example.spamblocker.data.SimTarget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatternViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BlockRepository(application)

    val patterns = repository.getAllPatterns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPattern(pattern: String, matchType: MatchType, simTarget: SimTarget) {
        viewModelScope.launch {
            repository.addPattern(pattern, matchType, simTarget)
        }
    }

    fun updatePattern(pattern: BlockPattern) {
        viewModelScope.launch {
            repository.updatePattern(pattern)
        }
    }

    fun deletePattern(pattern: BlockPattern) {
        viewModelScope.launch {
            repository.deletePattern(pattern)
        }
    }
}
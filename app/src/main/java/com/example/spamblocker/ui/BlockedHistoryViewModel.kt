package com.example.spamblocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spamblocker.data.BlockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class BlockedHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BlockRepository(application)

    val blockedCalls = repository.getAllBlockedCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
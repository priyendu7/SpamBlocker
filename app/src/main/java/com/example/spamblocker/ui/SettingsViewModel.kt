package com.example.spamblocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spamblocker.data.BlockRepository
import com.example.spamblocker.data.BlockSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BlockRepository(application)

    val settings = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBlockUnknownSim1(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: BlockSettings()
            repository.updateSettings(current.copy(blockUnknownSim1 = enabled))
        }
    }

    fun setBlockUnknownSim2(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: BlockSettings()
            repository.updateSettings(current.copy(blockUnknownSim2 = enabled))
        }
    }
}
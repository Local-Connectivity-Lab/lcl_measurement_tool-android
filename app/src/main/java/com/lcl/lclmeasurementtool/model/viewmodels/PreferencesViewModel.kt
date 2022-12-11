package com.lcl.lclmeasurementtool.model.viewmodels

import androidx.lifecycle.ViewModel
import com.lcl.lclmeasurementtool.model.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class PreferencesViewModel constructor(private val preferencesRepository: PreferencesRepository)
    : ViewModel() {
}
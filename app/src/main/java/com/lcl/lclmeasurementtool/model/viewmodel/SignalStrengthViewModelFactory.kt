package com.lcl.lclmeasurementtool.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lcl.lclmeasurementtool.model.dao.SignalStrengthDao


class SignalStrengthViewModelFactory(private val signalStrengthDao: SignalStrengthDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignalStrengthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignalStrengthViewModel(signalStrengthDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
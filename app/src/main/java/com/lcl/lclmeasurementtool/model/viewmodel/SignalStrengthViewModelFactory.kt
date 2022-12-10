package com.lcl.lclmeasurementtool.model.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao


class SignalStrengthViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignalStrengthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignalStrengthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
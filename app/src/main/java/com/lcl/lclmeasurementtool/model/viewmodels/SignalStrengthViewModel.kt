package com.lcl.lclmeasurementtool.model.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.lcl.lclmeasurementtool.database.db.AppDatabase
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.repository.MeasurementsRepository
import kotlinx.coroutines.launch

class SignalStrengthViewModel(application: Application): ViewModel() {
    private val repository = MeasurementsRepository(AppDatabase.getDatabase(application))
    val signalStrengthData = repository.signalStrengthData

    fun insert(data: SignalStrengthReportModel) {
        viewModelScope.launch {
            repository.insertSignalStrengthData(data)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(SignalStrengthViewModel::class.java)) {
                    val application = checkNotNull(extras[APPLICATION_KEY])

                    @Suppress("UNCHECKED_CAST")
                    return SignalStrengthViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
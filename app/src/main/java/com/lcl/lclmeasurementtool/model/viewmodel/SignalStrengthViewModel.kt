package com.lcl.lclmeasurementtool.model.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.database.db.AppDatabase
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.repository.MeasurementsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SignalStrengthViewModel(application: Application): ViewModel() {
    private val repository = MeasurementsRepository(AppDatabase.getDatabase(application))
    val signalStrengthData = repository.signalStrengthData

    fun insert(data: SignalStrengthReportModel) {
        viewModelScope.launch {
            repository.insertSignalStrengthData(data)
        }
    }
}
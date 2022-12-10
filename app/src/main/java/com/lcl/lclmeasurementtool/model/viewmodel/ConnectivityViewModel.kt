package com.lcl.lclmeasurementtool.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.database.db.AppDatabase
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.repository.MeasurementsRepository
import kotlinx.coroutines.launch

class ConnectivityViewModel(application: Application): AndroidViewModel(application) {

    private val repository = MeasurementsRepository(AppDatabase.getDatabase(application))
    val connectivityData = repository.connectivityData

    fun insert(data: ConnectivityReportModel) {
        viewModelScope.launch {
            repository.insertConnectivityData(data)
        }
    }
}
package com.lcl.lclmeasurementtool.model.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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



    class Factory(private val application: Application): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConnectivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConnectivityViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
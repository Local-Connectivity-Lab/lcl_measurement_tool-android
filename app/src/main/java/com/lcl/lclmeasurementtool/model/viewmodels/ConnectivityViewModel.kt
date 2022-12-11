package com.lcl.lclmeasurementtool.model.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.lcl.lclmeasurementtool.database.db.AppDatabase
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.repository.MeasurementsRepository
import kotlinx.coroutines.launch

class ConnectivityViewModel(application: Application): ViewModel() {

    private val repository = MeasurementsRepository(AppDatabase.getDatabase(application))
    val connectivityData = repository.connectivityData

    fun insert(data: ConnectivityReportModel) {
        viewModelScope.launch {
            repository.insertConnectivityData(data)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(ConnectivityViewModel::class.java)) {
                    val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    @Suppress("UNCHECKED_CAST")
                    return ConnectivityViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
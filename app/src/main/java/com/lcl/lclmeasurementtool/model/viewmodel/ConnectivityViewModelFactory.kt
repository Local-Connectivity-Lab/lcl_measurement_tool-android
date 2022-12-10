package com.lcl.lclmeasurementtool.model.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao

class ConnectivityViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectivityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
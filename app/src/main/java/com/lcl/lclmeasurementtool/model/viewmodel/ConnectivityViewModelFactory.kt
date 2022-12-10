package com.lcl.lclmeasurementtool.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lcl.lclmeasurementtool.model.dao.ConnectivityDao

class ConnectivityViewModelFactory(private val connectivityDao: ConnectivityDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectivityViewModel(connectivityDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
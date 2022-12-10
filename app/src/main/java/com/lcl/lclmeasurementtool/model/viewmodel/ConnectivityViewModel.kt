package com.lcl.lclmeasurementtool.model.viewmodel

import androidx.lifecycle.ViewModel
import com.lcl.lclmeasurementtool.model.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel

class ConnectivityViewModel(private val connectivityDao: ConnectivityDao): ViewModel() {
    fun fullConnectivityList(): List<ConnectivityReportModel> = connectivityDao.getAll()
}
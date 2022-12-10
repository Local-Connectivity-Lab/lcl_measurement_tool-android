package com.lcl.lclmeasurementtool.model.viewmodel

import androidx.lifecycle.ViewModel
import com.lcl.lclmeasurementtool.model.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel

class SignalStrengthViewModel(private val signalStrengthDao: SignalStrengthDao): ViewModel() {
    fun fullSignalStrengthList(): List<SignalStrengthReportModel> = signalStrengthDao.getAll()
}
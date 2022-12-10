package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.database.db.AppDatabase
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeasurementsRepository(private val database: AppDatabase) {

    val signalStrengthData = database.signalStrengthDao().getAll()
    val connectivityData = database.connectivityDao().getAll()

    suspend fun insertSignalStrengthData(signalStrength: SignalStrengthReportModel) {
        withContext(Dispatchers.IO) {
            database.signalStrengthDao().insert(signalStrength)
        }
    }

    suspend fun insertConnectivityData(connectivity: ConnectivityReportModel) {
        withContext(Dispatchers.IO) {
            database.connectivityDao().insert(connectivity)
        }
    }

}
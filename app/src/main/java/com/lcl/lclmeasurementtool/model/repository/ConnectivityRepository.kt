package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectivityRepository @Inject constructor(
    private val connectivityDao: ConnectivityDao
): HistoryDataRepository<ConnectivityReportModel> {
    override fun getAll(): Flow<List<ConnectivityReportModel>> = connectivityDao.getAll()
    override suspend fun insert(data: ConnectivityReportModel) = connectivityDao.insert(data)
}
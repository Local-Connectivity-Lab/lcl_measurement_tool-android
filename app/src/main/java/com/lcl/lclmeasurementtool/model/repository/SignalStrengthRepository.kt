package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignalStrengthRepository @Inject constructor(
    private val signalStrengthDao: SignalStrengthDao,
): HistoryDataRepository<SignalStrengthReportModel> {
    override fun getAll(): Flow<List<SignalStrengthReportModel>> = signalStrengthDao.getAll()
    override suspend fun insert(data: SignalStrengthReportModel) = signalStrengthDao.insert(data)
}
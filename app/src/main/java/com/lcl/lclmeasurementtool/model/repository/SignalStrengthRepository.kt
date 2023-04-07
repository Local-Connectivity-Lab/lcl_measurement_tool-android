package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.sync.Synchronizer
import com.lcl.lclmeasurementtool.util.prepareReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SignalStrengthRepository @Inject constructor(
    private val signalStrengthDao: SignalStrengthDao,
    private val networkApi: NetworkApiRepository,
    private val userDataRepository: UserDataRepository,
): HistoryDataRepository<SignalStrengthReportModel> {
    override fun getAll(): Flow<List<SignalStrengthReportModel>> = signalStrengthDao.getAll()
    override suspend fun update(data: SignalStrengthReportModel) = signalStrengthDao.updateReportStatus(data)
    override suspend fun insert(data: SignalStrengthReportModel) = signalStrengthDao.insert(data)

    override suspend fun syncWith(synchronizer: Synchronizer) = synchronizer.syncData {
        userDataRepository.userData.combine(signalStrengthDao.getAllNotReported()) { preference, signalStrengths ->
            Pair(preference, signalStrengths)
        }.collect {pair ->

            val userPreference = pair.first
            val signalStrengthList = pair.second
            signalStrengthList.forEach {signalStrengthReportModel ->
                val reportString = prepareReportData(signalStrengthReportModel, userPreference)
                networkApi.uploadSignalStrength(reportString)
            }
        }
    }
}
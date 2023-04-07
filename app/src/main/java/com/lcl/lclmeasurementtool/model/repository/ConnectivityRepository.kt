package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.sync.Synchronizer
import com.lcl.lclmeasurementtool.util.prepareReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ConnectivityRepository @Inject constructor(
    private val connectivityDao: ConnectivityDao,
    private val networkApi: NetworkApiRepository,
    private val userDataRepository: UserDataRepository,
): HistoryDataRepository<ConnectivityReportModel> {
    override fun getAll(): Flow<List<ConnectivityReportModel>> = connectivityDao.getAll()

    override suspend fun update(data: ConnectivityReportModel) = connectivityDao.updateReportStatus(data)
    override suspend fun insert(data: ConnectivityReportModel) = connectivityDao.insert(data)

    override suspend fun syncWith(synchronizer: Synchronizer) = synchronizer.syncData {
        userDataRepository.userData.combine(connectivityDao.getAllNotReported()) { preference, connectivity ->
            Pair(preference, connectivity)
        }.collect {pair ->

            val userPreference = pair.first
            val connectivityList = pair.second
            connectivityList.forEach {connectivityReportModel ->
                val reportString = prepareReportData(connectivityReportModel, userPreference)
                networkApi.uploadConnectivity(reportString)
            }
        }
    }
}
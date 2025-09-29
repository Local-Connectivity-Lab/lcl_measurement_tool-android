package com.lcl.lclmeasurementtool.model.repository

import android.util.Log
import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.util.Synchronizer
import com.lcl.lclmeasurementtool.util.prepareReportData
import com.lcl.lclmeasurementtool.util.prepareReportDataNoAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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
        val toReportList = connectivityDao.getAllNotReported()
        if (toReportList.isEmpty()) {
            Log.d(TAG, "no outstanding connectivity reports")
            return@syncData
        }

        toReportList.asFlow()
            .flowOn(Dispatchers.IO)
            .combine(userDataRepository.userData) { connectivity, preference ->
                Log.d(TAG, "upload worker will upload $connectivity")
                val reportString = if (BuildConfig.FLAVOR != "full") {
                    prepareReportDataNoAuth(connectivity)
                } else {
                    prepareReportData(connectivity, preference)
                }
                networkApi.uploadConnectivity(reportString)
            }
            .catch {
                Log.d(TAG, "upload worker encounter $it when uploading connectivity")
                throw it
            }
            .onCompletion {
                Log.d(TAG, "finish uploading all unreported connectivity data")
            }
            .collect()
    }

    companion object {
        const val TAG = "ConnectivityRepository"
    }
}
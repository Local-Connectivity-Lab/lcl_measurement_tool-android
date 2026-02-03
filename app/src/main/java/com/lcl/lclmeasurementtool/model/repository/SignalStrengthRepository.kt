package com.lcl.lclmeasurementtool.model.repository

import android.util.Log
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.util.Synchronizer
import com.lcl.lclmeasurementtool.util.prepareReportData
import com.lcl.lclmeasurementtool.util.prepareReportDataNoAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.lcl.lclmeasurementtool.BuildConfig

class SignalStrengthRepository @Inject constructor(
    private val signalStrengthDao: SignalStrengthDao,
    private val networkApi: NetworkApiRepository,
    private val userDataRepository: UserDataRepository,
): HistoryDataRepository<SignalStrengthReportModel> {
    override fun getAll(): Flow<List<SignalStrengthReportModel>> = signalStrengthDao.getAll()
    override suspend fun update(data: SignalStrengthReportModel) = signalStrengthDao.updateReportStatus(data)
    override suspend fun insert(data: SignalStrengthReportModel) = signalStrengthDao.insert(data)

    override suspend fun syncWith(synchronizer: Synchronizer) = synchronizer.syncData {
        val toReportList = signalStrengthDao.getAllNotReported()
        if (toReportList.isEmpty()) {
            Log.d(TAG, "no outstanding signal strength reports")
            return@syncData
        }

        toReportList.asFlow()
            .flowOn(Dispatchers.IO)
            .combine(userDataRepository.userData) { signalStrength, preference ->
                Log.d(TAG, "upload worker will upload $signalStrength")
                val reportString = if (BuildConfig.BUILD_TYPE == "release") {
                    prepareReportData(signalStrength, preference)
                } else {
                    prepareReportDataNoAuth(signalStrength)
                }
                networkApi.uploadSignalStrength(reportString)
            }
            .catch {
                Log.d(TAG, "upload worker encounter the following exception when uploading outstanding signal strength report")
                throw it
            }
            .onCompletion {
                Log.d(TAG, "finish uploading all unreported signal strength data")
            }
            .collect()
    }

    companion object {
        const val TAG = "SignalStrengthRepository"
    }
}
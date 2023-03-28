package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.networking.RetrofitLCLNetwork
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class LCLApiRepository @Inject constructor(
    private val dataSource: RetrofitLCLNetwork
): NetworkApiRepository {

    override suspend fun register(registration: String): Response<String> = dataSource.register(registration)
    override suspend fun uploadConnectivity(connectivityReportModel: String): Response<String> = dataSource.uploadConnectivity(connectivityReportModel)
    override suspend fun uploadSignalStrength(signalStrengthReportModel: String): Response<String> = dataSource.uploadSignalStrength(signalStrengthReportModel)
}
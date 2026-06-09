package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.model.datamodel.Site
import com.lcl.lclmeasurementtool.networking.RetrofitLCLNetwork
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class LCLApiRepository @Inject constructor(
    private val dataSource: RetrofitLCLNetwork
): NetworkApiRepository {

    override suspend fun register(registration: String): ResponseBody = dataSource.register(registration)
    override suspend fun uploadConnectivity(connectivityReportModel: String): ResponseBody = dataSource.uploadConnectivity(connectivityReportModel)
    override suspend fun uploadSignalStrength(signalStrengthReportModel: String): ResponseBody = dataSource.uploadSignalStrength(signalStrengthReportModel)
    override suspend fun getSites(): List<Site> = dataSource.getSites()
}
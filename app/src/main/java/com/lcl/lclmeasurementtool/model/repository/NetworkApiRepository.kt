package com.lcl.lclmeasurementtool.model.repository

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

interface NetworkApiRepository {
    suspend fun register(@Body registration: String): Response<String>
    suspend fun uploadSignalStrength(@Body signalStrengthReportModel: String): Response<String>
    suspend fun uploadConnectivity(@Body connectivityReportModel: String): Response<String>
}
package com.lcl.lclmeasurementtool.model.repository

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

interface NetworkApiRepository {
    suspend fun register(@Body registration: String): ResponseBody
    suspend fun uploadSignalStrength(@Body signalStrengthReportModel: String): ResponseBody
    suspend fun uploadConnectivity(@Body connectivityReportModel: String): ResponseBody
}
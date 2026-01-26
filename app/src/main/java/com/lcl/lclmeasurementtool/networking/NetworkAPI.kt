package com.lcl.lclmeasurementtool.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.lcl.lclmeasurementtool.constants.NetworkConstants
import com.lcl.lclmeasurementtool.model.datamodel.Site
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import javax.inject.Singleton

private interface NetworkAPI {

    @Headers("Content-Type: ${NetworkConstants.MEDIA_TYPE}")
    @POST(value = NetworkConstants.REGISTRATION_ENDPOINT)
    suspend fun register(@Body registration: String): ResponseBody

    @Headers("Content-Type: ${NetworkConstants.MEDIA_TYPE}")
    @POST(value = NetworkConstants.SIGNAL_ENDPOINT)
    suspend fun uploadSignalStrength(@Body signalStrengthReportModel: String): ResponseBody

    @Headers("Content-Type: ${NetworkConstants.MEDIA_TYPE}")
    @POST(value = NetworkConstants.CONNECTIVITY_ENDPOINT)
    suspend fun uploadConnectivity(@Body connectivityReportModel: String): ResponseBody

    @GET("/api/sites")
    suspend fun getSites(): List<Site>
}

@Singleton
class RetrofitLCLNetwork {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val networkApi: NetworkAPI by lazy {
        Retrofit.Builder()
            .client(OkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(NetworkConstants.URL)
            .build().create(NetworkAPI::class.java)
    }


    suspend fun register(registration: String) = networkApi.register(registration)
    suspend fun uploadSignalStrength(signalStrengthReportModel: String) = networkApi.uploadSignalStrength(signalStrengthReportModel)
    suspend fun uploadConnectivity(connectivityReportModel: String) = networkApi.uploadConnectivity(connectivityReportModel)
    suspend fun getSites() = networkApi.getSites()
}
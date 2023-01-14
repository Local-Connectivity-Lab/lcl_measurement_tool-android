package com.lcl.lclmeasurementtool.networking

import com.lcl.lclmeasurementtool.constants.NetworkConstants
import com.lcl.lclmeasurementtool.model.datamodel.RegistrationModel
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Singleton

private interface NetworkAPI {

    @POST(value = NetworkConstants.REGISTRATION_ENDPOINT)
    suspend fun register(@Body registration: RegistrationModel): Response
}


class RetrofitLCLNetwork {

    private val networkApi = Retrofit.Builder()
        .baseUrl(NetworkConstants.URL)
        .client(OkHttpClient())
        .addConverterFactory(JacksonConverterFactory.create())
        .build().create(NetworkAPI::class.java)


    suspend fun register(registration: RegistrationModel) = networkApi.register(registration)

}
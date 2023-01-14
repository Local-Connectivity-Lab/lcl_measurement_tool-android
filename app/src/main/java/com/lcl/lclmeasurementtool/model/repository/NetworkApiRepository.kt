package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.model.datamodel.RegistrationModel
import okhttp3.Response
import retrofit2.http.Body

interface NetworkApiRepository {
    suspend fun register(@Body registration: RegistrationModel): Response
}
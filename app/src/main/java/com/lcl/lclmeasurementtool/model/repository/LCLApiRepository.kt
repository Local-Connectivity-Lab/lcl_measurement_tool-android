package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.model.datamodel.RegistrationModel
import com.lcl.lclmeasurementtool.networking.RetrofitLCLNetwork
import okhttp3.Response
import javax.inject.Inject

class LCLApiRepository @Inject constructor(
    private val dataSource: RetrofitLCLNetwork
): NetworkApiRepository {

    override suspend fun register(registration: RegistrationModel): Response = dataSource.register(registration)


}
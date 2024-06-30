package com.lcl.lclmeasurementtool.model.repository

import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun toggleShowData(showData: Boolean)
    suspend fun setDeviceID(newDeviceID: String)
    suspend fun setKeys(hPKR: ByteString, skT: ByteString)
    suspend fun setR(R: ByteString)
    suspend fun logout()
}
package com.lcl.lclmeasurementtool.model.repository

import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.datasource.PreferencesDataSource
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalUserDataRepository @Inject constructor(
    private val preferenceDataSource: PreferencesDataSource
    ) : UserDataRepository {
    override val userData: Flow<UserData> = preferenceDataSource.userData
    override suspend fun toggleShowData(showData: Boolean) = preferenceDataSource.toggleShowData(showData)

    override suspend fun setDeviceID(newDeviceID: String) = preferenceDataSource.setDeviceID(newDeviceID)

    override suspend fun setKeys(hPKR: ByteString, skT: ByteString) = preferenceDataSource.setKeys(hPKR, skT)
    override suspend fun setR(R: ByteString) = preferenceDataSource.setR(R)
    override suspend fun logout() = preferenceDataSource.logout()

}
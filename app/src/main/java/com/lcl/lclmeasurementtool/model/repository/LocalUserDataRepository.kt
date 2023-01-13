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

//    override suspend fun setLogin(loggedIn: Boolean) = preferenceDataSource.setLogin(loggedIn)

//    override suspend fun setHPKR(newKey: ByteString) = preferenceDataSource.setHPKR(newKey)
//
//    override suspend fun setSKT(newKey: ByteString) = preferenceDataSource.setSKT(newKey)

    override suspend fun setKeys(hPKR: ByteString, skT: ByteString) = preferenceDataSource.setKeys(hPKR, skT)

    override suspend fun logout() = preferenceDataSource.logout()

}
package com.lcl.lclmeasurementtool.datasource

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.datastore.UserPreferences
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>){

    val userData = userPreferences.data.map {
        UserData(
            skT = it.skT,
            hPKR = it.hPkr,
            showData = it.showData,
            loggedIn = it.loggedIn,  // !(it.skT.isEmpty || it.hPkr.isEmpty)
            R = it.r
        )
    }

    suspend fun toggleShowData(showData: Boolean) {
        try {
            userPreferences.updateData {
                it.toBuilder().setShowData(showData).build()
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun setDeviceID(newDeviceID: String) {
        try {
            userPreferences.updateData {
                it.toBuilder().setDeviceId(newDeviceID).build()
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

//    suspend fun setLogin(loggedIn: Boolean) {
//        try {
//            userPreferences.updateData {
//                it.toBuilder().setLoggedIn(loggedIn).build()
//            }
//        } catch (e: IOException) {
//            // TODO: alert to user
//            throw e
//        }
//    }
//
//    suspend fun setHPKR(newKey: ByteString) {
//        try {
//            userPreferences.updateData {
//                it.toBuilder().setHPkr(newKey).build()
//                if (it.hPkr.isEmpty || it.skT.isEmpty) {
//                    it.toBuilder().setLoggedIn(false).clearHPkr().build()
//                }
//                return@updateData it
//            }
//        } catch (e: IOException) {
//            // TODO: alert to user
//            throw e
//        }
//    }
//
//    suspend fun setSKT(newKey: ByteString) {
//        try {
//            userPreferences.updateData {
//                it.toBuilder().setSkT(newKey).build()
//                if (it.hPkr.isEmpty || it.skT.isEmpty) {
//                    it.toBuilder().setLoggedIn(false).clearSkT().build()
//                }
//                return@updateData it
//            }
//        } catch (e: IOException) {
//            // TODO: alert to user
//            throw e
//        }
//    }

    suspend fun setR(R: ByteString) {
        userPreferences.updateData {
            it.toBuilder().setR(R).build()
        }
    }

    suspend fun setKeys(hPKR: ByteString, skT: ByteString) {
        userPreferences.updateData {
            if (!it.hPkr.isEmpty || !it.skT.isEmpty) {
                logout()
            }
            Log.d("PreferenceDataSource", "set login to true")
            it.toBuilder().setHPkr(hPKR).setSkT(skT).setLoggedIn(true).build()
        }
    }

    suspend fun logout() {
        try {
            userPreferences.updateData {
                Log.d("PreferenceDataSource", "set login to false")
                it.toBuilder().setLoggedIn(false).clearHPkr().clearSkT().build()
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

//    data class UserPref(private val showData: Boolean)
//    data class DeviceSettings(private val loggedIn: Boolean, private val hPKR: String, private val skT: String)
//    data class Settings(private val userPreference: UserPref?, private val deviceSettings: DeviceSettings?)
}
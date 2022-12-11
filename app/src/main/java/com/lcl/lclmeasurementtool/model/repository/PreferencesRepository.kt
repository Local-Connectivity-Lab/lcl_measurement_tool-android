package com.lcl.lclmeasurementtool.model.repository

import android.bluetooth.BluetoothClass.Device
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferenceKeys {
        val SHOW_DATA = booleanPreferencesKey("show_data")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val H_PKR = stringPreferencesKey("h_pkr")
        val SK_T = stringPreferencesKey("sk_t")
    }

    val preferences: Flow<Settings> = dataStore.data.map {
        val showData = it[PreferenceKeys.SHOW_DATA] ?: false
        val loggedIn = it[PreferenceKeys.LOGGED_IN] ?: false
        val hPKR = it[PreferenceKeys.H_PKR] ?: ""
        val skT = it[PreferenceKeys.SK_T] ?: ""

        Settings(UserPreference(showData = showData), DeviceSettings(loggedIn, hPKR, skT))
    }.catch { exception ->
        if (exception is IOException) {
            emit(Settings(null, null))
        } else {
            throw exception
        }
    }

    suspend fun setShowData(showData: Boolean) {
        try {
            dataStore.edit {
                it[PreferenceKeys.SHOW_DATA] = showData
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun setDeviceID(newDeviceID: String) {
        try {
            dataStore.edit {
                it[PreferenceKeys.DEVICE_ID] = newDeviceID
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun setLogin(loggedIn: Boolean) {
        try {
            dataStore.edit {
                it[PreferenceKeys.LOGGED_IN] = loggedIn
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun setHPKR(newKey: String) {
        try {
            dataStore.edit {
                it[PreferenceKeys.H_PKR] = newKey
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun setSKT(newKey: String) {
        try {
            dataStore.edit {
                it[PreferenceKeys.SK_T] = newKey
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    suspend fun logout() {
        try {
            dataStore.edit {
                it[PreferenceKeys.LOGGED_IN] = false
                it.remove(PreferenceKeys.H_PKR)
                it.remove(PreferenceKeys.SK_T)
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    data class UserPreference(private val showData: Boolean)
    data class DeviceSettings(private val loggedIn: Boolean, private val hPKR: String, private val skT: String)
    data class Settings(private val userPreference: UserPreference?, private val deviceSettings: DeviceSettings?)
}
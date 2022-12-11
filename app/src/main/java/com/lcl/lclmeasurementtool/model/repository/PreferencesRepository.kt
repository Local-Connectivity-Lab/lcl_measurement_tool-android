package com.lcl.lclmeasurementtool.model.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesRepository(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private object PreferenceKeys {
        val SHOW_DATA = booleanPreferencesKey("show_data")
    }

    val userPreference: Flow<UserPreference> = context.dataStore.data.map {
        val showData = it[PreferenceKeys.SHOW_DATA] ?: false
        UserPreference(showData = showData)
    }.catch { exception ->
        if (exception is IOException) {
            emit(UserPreference(false))
        } else {
            throw exception
        }
    }

    suspend fun updateShowData(showData: Boolean) {
        try {
            context.dataStore.edit {
                it[PreferenceKeys.SHOW_DATA] = showData
            }
        } catch (e: IOException) {
            // TODO: alert to user
            throw e
        }
    }

    data class UserPreference(private val showData: Boolean)
}
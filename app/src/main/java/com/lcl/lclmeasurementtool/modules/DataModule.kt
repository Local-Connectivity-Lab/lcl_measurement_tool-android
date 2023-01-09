package com.lcl.lclmeasurementtool.modules

import com.lcl.lclmeasurementtool.datasource.ConnectivityMonitorDataSource
import com.lcl.lclmeasurementtool.datasource.PreferencesDataSource
import com.lcl.lclmeasurementtool.datastore.UserPreferences
import com.lcl.lclmeasurementtool.model.repository.LocalUserDataRepository
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsUserDataRepository(
        userDataRepository: LocalUserDataRepository
    ): UserDataRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: ConnectivityMonitorDataSource
    ): NetworkMonitor
}
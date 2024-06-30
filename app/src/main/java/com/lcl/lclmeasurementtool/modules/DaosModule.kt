package com.lcl.lclmeasurementtool.modules

import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.database.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun providesSignalStrengthDao(database: AppDatabase): SignalStrengthDao =
        database.signalStrengthDao()

    @Provides
    fun providesConnectivityDao(database: AppDatabase): ConnectivityDao =
        database.connectivityDao()
}
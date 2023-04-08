package com.lcl.lclmeasurementtool

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.lcl.lclmeasurementtool.sync.Sync
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * [Application] class for LCL Measurement tool
 */
@HiltAndroidApp
class LCLApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() = Configuration.Builder()
                                                    .setMinimumLoggingLevel(Log.DEBUG)
                                                    .setWorkerFactory(workerFactory)
                                                    .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Sync; the system responsible for keeping data in the app up to date.
        Sync.initialize(context = this)
    }
}
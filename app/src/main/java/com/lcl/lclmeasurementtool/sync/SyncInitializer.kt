package com.lcl.lclmeasurementtool.sync

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer

object Sync {
    // FROM Android Doc:
    // This method is a workaround to manually initialize the sync process instead of relying on
    // automatic initialization with Androidx Startup. It is called from the app module's
    // Application.onCreate() and should be only done once.
    fun initialize(context: Context) {
        AppInitializer.getInstance(context)
            .initializeComponent(SyncInitializer::class.java)
    }
}

internal const val SyncWorkName = "LCLDataReportSync"

class SyncInitializer: Initializer<Sync> {
    override fun create(context: Context): Sync {
        WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork(
                SyncWorkName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                UploadWorker.periodicSyncWork()
            )
        }
        return Sync
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java)
}
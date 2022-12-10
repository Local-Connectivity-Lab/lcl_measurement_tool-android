package com.lcl.lclmeasurementtool

import android.app.Application
import com.lcl.lclmeasurementtool.database.db.AppDatabase

class ConnectivityApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
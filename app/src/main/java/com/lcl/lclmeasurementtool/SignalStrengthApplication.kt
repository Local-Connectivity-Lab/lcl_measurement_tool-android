package com.lcl.lclmeasurementtool

import android.app.Application
import com.lcl.lclmeasurementtool.Database.db.AppDatabase

class SignalStrengthApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
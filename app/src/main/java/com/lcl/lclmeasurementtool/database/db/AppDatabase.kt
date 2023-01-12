package com.lcl.lclmeasurementtool.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lcl.lclmeasurementtool.database.dao.ConnectivityDao
import com.lcl.lclmeasurementtool.database.dao.SignalStrengthDao
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel

@Database(entities = [SignalStrengthReportModel::class, ConnectivityReportModel::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun signalStrengthDao(): SignalStrengthDao
    abstract fun connectivityDao(): ConnectivityDao
//    companion object {
//
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room
//                    .databaseBuilder(context, AppDatabase::class.java, "measurement_db")
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}
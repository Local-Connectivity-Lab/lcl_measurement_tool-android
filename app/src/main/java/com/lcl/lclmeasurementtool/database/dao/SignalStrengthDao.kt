package com.lcl.lclmeasurementtool.database.dao

import androidx.room.*
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalStrengthDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg signalStrength: SignalStrengthReportModel)

    // READ
    @Query("SELECT * FROM signal_strength_table ORDER BY time_stamp ASC")
    fun getAll(): Flow<List<SignalStrengthReportModel>>

    // DELETE
    @Query("DELETE FROM signal_strength_table")
    fun deleteAll()
}
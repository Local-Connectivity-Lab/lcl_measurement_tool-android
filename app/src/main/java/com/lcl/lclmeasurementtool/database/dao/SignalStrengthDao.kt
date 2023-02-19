package com.lcl.lclmeasurementtool.database.dao

import androidx.room.*
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalStrengthDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(signalStrength: SignalStrengthReportModel)

    // READ
    @Transaction
    @Query("SELECT * FROM signal_strength_table ORDER BY time_stamp DESC")
    fun getAll(): Flow<List<SignalStrengthReportModel>>

    // DELETE
    @Query("DELETE FROM signal_strength_table")
    fun deleteAll()
}
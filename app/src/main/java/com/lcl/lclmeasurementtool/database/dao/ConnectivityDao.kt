package com.lcl.lclmeasurementtool.database.dao

import androidx.room.*
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectivityDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(connectivity: ConnectivityReportModel)

    // READ
    @Transaction
    @Query("SELECT * FROM connectivity_table ORDER BY time_stamp DESC")
    fun getAll(): Flow<List<ConnectivityReportModel>>

    @Transaction
    @Query("SELECT * FROM connectivity_table where reported = false")
    fun getAllNotReported(): List<ConnectivityReportModel>

    // WRITE
    @Transaction
    @Update
    suspend fun updateReportStatus(connectivity: ConnectivityReportModel)

    // DELETE
    @Query("DELETE FROM connectivity_table")
    fun deleteAll()
}
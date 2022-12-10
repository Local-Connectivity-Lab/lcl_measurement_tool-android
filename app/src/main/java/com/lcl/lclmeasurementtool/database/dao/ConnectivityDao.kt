package com.lcl.lclmeasurementtool.database.dao

import androidx.room.*
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectivityDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg connectivity: ConnectivityReportModel)

    // READ
    @Query("SELECT * FROM connectivity_table ORDER BY time_stamp ASC")
    fun getAll(): Flow<List<ConnectivityReportModel>>

    // DELETE
    @Query("DELETE FROM connectivity_table")
    fun deleteAll()
}
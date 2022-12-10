package com.lcl.lclmeasurementtool.model.dao

import androidx.room.*
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel

@Dao
interface ConnectivityDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg connectivity: ConnectivityReportModel)

    // READ
    @Query("SELECT * FROM connectivity_table ORDER BY time_stamp ASC")
    fun getAll(): List<ConnectivityReportModel>

    // DELETE
    @Query("DELETE FROM connectivity_table")
    fun deleteAll()
}
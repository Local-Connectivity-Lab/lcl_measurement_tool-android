package com.lcl.lclmeasurementtool.model.datamodel

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "signal_strength_table")
data class SignalStrengthReportModel(
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    override var cellId: String,
    override var deviceId: String,
    @get:JsonProperty("dbm") @ColumnInfo(name = "signal_strength") var dbm: Int,
    @get:JsonProperty("level_code") @ColumnInfo(name = "signal_strength_level") var levelCode: Int
    ) : BaseMeasureDataModel

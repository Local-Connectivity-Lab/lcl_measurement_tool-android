package com.lcl.lclmeasurementtool.model.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Entity(tableName = "signal_strength_table")
@kotlinx.serialization.Serializable
data class SignalStrengthReportModel(
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    override var cellId: String,
    override var deviceId: String,
    @SerialName("dbm") @ColumnInfo(name = "signal_strength") var dbm: Int,
    @SerialName("level_code") @ColumnInfo(name = "signal_strength_level") var levelCode: Int,
    @ColumnInfo("reported") @Transient override var reported: Boolean = false
) : BaseMeasureDataModel

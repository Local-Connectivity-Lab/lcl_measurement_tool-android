package com.lcl.lclmeasurementtool.model.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@kotlinx.serialization.Serializable
@Entity(tableName = "connectivity_table")
data class ConnectivityReportModel constructor(
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    override var cellId: String,
    override var deviceId: String,
    @ColumnInfo(name = "upload_speed") @SerialName("upload_speed") var uploadSpeed: Double,
    @ColumnInfo(name = "download_speed") @SerialName("download_speed") var downloadSpeed: Double,
    @ColumnInfo(name = "ping") @SerialName("ping") var ping: Double,
    @ColumnInfo(name = "package_loss") @SerialName("package_loss") var packetLoss: Double,
    @ColumnInfo(name = "reported") @Transient override var reported: Boolean = false
) : BaseMeasureDataModel

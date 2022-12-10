package com.lcl.lclmeasurementtool.model.datamodel

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

data class ConnectivityReportModel(
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    @Ignore override var cellId: String,
    @Ignore override var deviceId: String,
    @ColumnInfo(name = "upload_speed") @get:JsonProperty("upload_speed") var uploadSpeed: Double,
    @ColumnInfo(name = "download_speed") @get:JsonProperty("download_speed") var downloadSpeed: Double,
    @ColumnInfo(name = "ping") @get:JsonProperty("ping") var ping: Double
) : BaseMeasureDataModel

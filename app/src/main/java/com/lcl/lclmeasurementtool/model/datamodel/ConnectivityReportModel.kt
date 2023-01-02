package com.lcl.lclmeasurementtool.model.datamodel

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "connectivity_table")
data class ConnectivityReportModel(
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    override var cellId: String,
    override var deviceId: String,
    @ColumnInfo(name = "upload_speed") @get:JsonProperty("upload_speed") var uploadSpeed: Double,
    @ColumnInfo(name = "download_speed") @get:JsonProperty("download_speed") var downloadSpeed: Double,
    @ColumnInfo(name = "ping") @get:JsonProperty("ping") var ping: Double
) : BaseMeasureDataModel {
//    companion object {
//        val diffCallback = object: DiffUtil.ItemCallback<ConnectivityReportModel>() {
//            override fun areItemsTheSame(
//                oldItem: ConnectivityReportModel,
//                newItem: ConnectivityReportModel
//            ): Boolean {
//                return oldItem.timestamp == newItem.timestamp
//            }
//
//            override fun areContentsTheSame(
//                oldItem: ConnectivityReportModel,
//                newItem: ConnectivityReportModel
//            ): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
}

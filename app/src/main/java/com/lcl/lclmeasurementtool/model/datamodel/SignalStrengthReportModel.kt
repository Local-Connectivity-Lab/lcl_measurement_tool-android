package com.lcl.lclmeasurementtool.model.datamodel

import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.annotations.NotNull

@Entity(tableName = "signal_strength_table")
data class SignalStrengthReportModel(
    @Ignore override var deviceId: String,
    @ColumnInfo(name = "latitude") override var latitude: Double,
    @ColumnInfo(name = "longitude") override var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "time_stamp") override var timestamp: String,
    @Ignore  override var cellId: String,
    @get:JsonProperty("dbm") @ColumnInfo(name = "signal_strength") var dbm: Int,
    @get:JsonProperty("level_code") @ColumnInfo(name = "signal_strength_level") var levelCode: Int
    ) : BaseMeasureDataModel {

    companion object {
        val diffCallback = object: DiffUtil.ItemCallback<SignalStrengthReportModel>() {
            override fun areItemsTheSame(
                oldItem: SignalStrengthReportModel,
                newItem: SignalStrengthReportModel
            ): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(
                oldItem: SignalStrengthReportModel,
                newItem: SignalStrengthReportModel
            ): Boolean {
                return newItem == oldItem
            }

        }
    }
    }

package com.lcl.lclmeasurementtool.model.datamodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
sealed interface BaseMeasureDataModel {
    @SerialName("latitude")
    var latitude: Double

    @SerialName("longitude")
    var longitude: Double

    @SerialName("timestamp")
    var timestamp: String

    @SerialName("cell_id")
    var cellId: String

    @SerialName("device_id")
    var deviceId: String

    var reported: Boolean
}
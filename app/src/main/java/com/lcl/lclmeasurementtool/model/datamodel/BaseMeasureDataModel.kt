package com.lcl.lclmeasurementtool.model.datamodel

@kotlinx.serialization.Serializable
sealed interface BaseMeasureDataModel {
    var latitude: Double

    var longitude: Double

    var timestamp: String

    var cellId: String

    var deviceId: String

    var reported: Boolean
}
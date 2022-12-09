package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

interface BaseMeasureDataModel {
    @get:JsonProperty("latitude")
    var latitude: Double

    @get:JsonProperty("longitude")
    var longitude: Double

    @get:JsonProperty("timestamp")
    var timestamp: String

    @get:JsonProperty("cell_id")
    var cellId: String

    @get:JsonProperty("device_id")
    var deviceId: String
}
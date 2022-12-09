package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class ConnectivityReportModel(
    override var latitude: Double,
    override var longitude: Double,
    override var timestamp: String,
    override var cellId: String,
    override var deviceId: String,
    @get:JsonProperty("upload_speed") var uploadSpeed: Double,
    @get:JsonProperty("download_speed") var downloadSpeed: Double,
    @get:JsonProperty("ping") var ping: Double
) : BaseMeasureDataModel

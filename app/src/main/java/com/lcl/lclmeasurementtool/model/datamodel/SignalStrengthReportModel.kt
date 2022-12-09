package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class SignalStrengthReportModel(
    override var deviceId: String,
    override var latitude: Double,
    override var longitude: Double,
    override var timestamp: String,
    override var cellId: String,
    @get:JsonProperty("dbm") var dbm: Int,
    @get:JsonProperty("level_code") var levelCode: Int
    ) : BaseMeasureDataModel

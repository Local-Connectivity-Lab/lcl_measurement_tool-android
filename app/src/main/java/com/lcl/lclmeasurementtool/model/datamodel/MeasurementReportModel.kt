package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class MeasurementReportModel(
    @get:JsonProperty("sigma_m") var sigmaM: String,
    @get:JsonProperty("h_pkr") var hPKR: String,
    @get:JsonProperty("M") var M: String,
    @get:JsonProperty("show_data") var showData: Boolean)

package com.lcl.lclmeasurementtool.model.datamodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class MeasurementReportModel(
    @SerialName("sigma_m") var sigmaM: String,
    @SerialName("h_pkr") var hPKR: String,
    @SerialName("M") var M: String,
    @SerialName("show_data") var showData: Boolean)

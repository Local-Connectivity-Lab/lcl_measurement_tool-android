package com.lcl.lclmeasurementtool.model.datamodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class RegistrationModel(
    @SerialName("sigma_r") var sigmaR: String,
    @SerialName("h") var h: String,
    @SerialName("R") var R: String)

package com.lcl.lclmeasurementtool.model.datamodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class QRCodeKeysModel constructor(
    @SerialName("sigma_t") var sigmaT: String,
    @SerialName("sk_t") var skT: String,
    @SerialName("pk_a") var pk_a: String)

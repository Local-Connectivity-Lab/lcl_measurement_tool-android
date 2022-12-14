package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class QRCodeKeysModel(
    @get:JsonProperty("sigma_t") var sigmaT: String,
    @get:JsonProperty("sk_t") var skT: String,
    @get:JsonProperty("pk_a") var pk_a: String)

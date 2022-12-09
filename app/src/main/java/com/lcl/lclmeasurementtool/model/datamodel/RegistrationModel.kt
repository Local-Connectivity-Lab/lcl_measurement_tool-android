package com.lcl.lclmeasurementtool.model.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class RegistrationModel(
    @get:JsonProperty("sigma_r") var sigmaR: String,
    @get:JsonProperty("r") var h: String,
    @get:JsonProperty("R") var R: String
    )

package com.lcl.lclmeasurementtool.model.datamodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Site(
    @SerialName("identity") val identity: String,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("status") val status: String,
    @SerialName("address") val address: String,
    @SerialName("cell_ids") val cellIds: List<String>? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("boundaries") val boundaries: List<List<Double>>? = null
)

@file:JvmName("HostnameResponse")
package net.measurementlab.ndt7.android.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HostnameResponse(
    @SerialName("results")
    val results: List<Result>? = null
)

@Serializable
data class Result(
    @SerialName("location")
    val location: Location,
    @SerialName("machine")
    val machine: String,
    @SerialName("urls")
    val urls: Urls
)

@Serializable
data class Location(
    @SerialName("city")
    val city: String,
    @SerialName("country")
    val country: String
)

@Serializable
data class Urls(
    @SerialName("ws:///ndt/v7/download")
    val ndt7DownloadWS: String,
    @SerialName("ws:///ndt/v7/upload")
    val ndt7UploadWS: String,
    @SerialName("wss:///ndt/v7/download")
    val ndt7DownloadWSS: String,
    @SerialName("wss:///ndt/v7/upload")
    val ndt7UploadWSS: String
)

package net.measurementlab.ndt7.android.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientResponse(
    @SerialName("AppInfo") val appInfo: AppInfo,
    @SerialName("Origin") val origin: String = "client",
    @SerialName("Test") val test: String,
    @SerialName("Measurement") val measurement: Measurement? = null
)

@Serializable
data class AppInfo(
    @SerialName("ElapsedTime") val elapsedTime: Long,
    @SerialName("NumBytes") val numBytes: Double
)

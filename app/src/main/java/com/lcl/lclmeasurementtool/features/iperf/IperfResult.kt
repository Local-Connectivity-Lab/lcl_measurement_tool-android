package com.lcl.lclmeasurementtool.features.iperf

data class IperfResult(
    val timeStart: Float,
    val timeEnd: Float,
    val sendBytes: String,
    val bandWidth: String,
    val isDownMode: Boolean,
    val errorMSg: String?,
    val status: IperfStatus
)

enum class IperfStatus {
    RUNNING,
    FINISHED,
    ERROR
}

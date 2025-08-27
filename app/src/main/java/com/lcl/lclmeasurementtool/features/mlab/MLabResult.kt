package com.lcl.lclmeasurementtool.features.mlab

import net.measurementlab.ndt7.android.NDTTest
import net.measurementlab.ndt7.android.models.TCPInfo

data class MLabResult(
    val speed: String?,
    val type: NDTTest.TestType,
    val errorMsg: String?,
    val status: MLabTestStatus,
    val tcpInfo: TCPInfo? = null
)

enum class MLabTestStatus {
    RUNNING,
    FINISHED,
    ERROR
}

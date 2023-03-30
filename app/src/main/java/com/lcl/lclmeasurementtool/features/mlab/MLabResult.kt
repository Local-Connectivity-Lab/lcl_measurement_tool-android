package com.lcl.lclmeasurementtool.features.mlab

import net.measurementlab.ndt7.android.NDTTest

data class MLabResult(
    val speed: String?,
    val type: NDTTest.TestType,
    val errorMsg: String?,
    val status: MLabTestStatus
)

enum class MLabTestStatus {
    RUNNING,
    FINISHED,
    ERROR
}

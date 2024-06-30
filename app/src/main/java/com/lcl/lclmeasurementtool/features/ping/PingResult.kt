package com.lcl.lclmeasurementtool.features.ping

data class PingResult(
    val numLoss: String? = null,
    val min: String? = null,
    val avg: String? = null,
    val max: String? = null,
    val mdev: String? = null,
    val error: PingError
)

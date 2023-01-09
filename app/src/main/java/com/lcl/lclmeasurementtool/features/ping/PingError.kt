package com.lcl.lclmeasurementtool.features.ping

data class PingError(
    val code: PingErrorCase,
    val message: String? = null
)

enum class PingErrorCase(val exitCode: Int) {
    OK(0),
    IO(1),
    PARSING(2),
    OTHER(3);
}
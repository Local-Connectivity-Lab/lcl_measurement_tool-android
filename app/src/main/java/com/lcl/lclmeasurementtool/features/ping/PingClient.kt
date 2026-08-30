package com.lcl.lclmeasurementtool.features.ping

interface PingClient {
    sealed interface PingResult {
        data class Success(val rttMs: Double, val response: PingPacket) : PingResult
        object Timeout : PingResult
        data class Error(val message: String, val cause: Throwable? = null) : PingResult
    }

    suspend fun pingOnce(
        host: String,
        port: Int,
        timeoutMs: Long,
        requestId: Long,
        sequence: Int,
    ): PingResult
}

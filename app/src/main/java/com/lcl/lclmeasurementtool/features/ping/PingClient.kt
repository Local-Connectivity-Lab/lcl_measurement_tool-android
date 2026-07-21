package com.lcl.lclmeasurementtool.features.ping

interface PingClient {
    fun pingOnce(
        host: String,
        port: Int,
        timeoutMs: Long,
        requestId: Long,
        sequence: Int,
    ): UdpPingAttemptResult
}

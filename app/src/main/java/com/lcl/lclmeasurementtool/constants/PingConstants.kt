package com.lcl.lclmeasurementtool.constants

object PingConstants {
    // Keep google.com as default host while preserving explicit host:port parsing.
    const val PING_SERVER_ADDRESS = "google.com:443"
    const val PING_TIMES = 5
    const val PING_TIMEOUT_MS = 3000L
}

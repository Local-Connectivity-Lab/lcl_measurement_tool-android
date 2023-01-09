package com.lcl.lclmeasurementtool.features.ping

class Ping {
    companion object {
        suspend fun start(address: String, times: Int = 5, timeout: Long) : PingResult {
            if (address.isEmpty()) {
                throw IllegalArgumentException()
            }

            if (times < 0) {
                throw IllegalArgumentException()
            }

            if (timeout < 0) {
                throw IllegalArgumentException()
            }

            return PingUtil.doPing(address, times, timeout)
        }
    }
}
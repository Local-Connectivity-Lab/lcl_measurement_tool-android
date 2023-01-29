package com.lcl.lclmeasurementtool.features.ping

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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

        suspend fun cancellableStart(address: String, times: Int = 5, timeout: Long) = flow {
            if (address.isEmpty()) {
                throw IllegalArgumentException()
            }

            if (times < 0) {
                throw IllegalArgumentException()
            }

            if (timeout < 0) {
                throw IllegalArgumentException()
            }

            emit(PingUtil.doPing(address, times, timeout))
        }.flowOn(Dispatchers.IO).cancellable()
    }
}
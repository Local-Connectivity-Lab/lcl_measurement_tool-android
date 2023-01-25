package com.lcl.lclmeasurementtool.features.iperf

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.io.File

suspend fun runIperf(config: IperfConfig, cacheDir: File) {
    val client = IperfClient()
    val testResult = callbackFlow {
        val callback = object: IperfCallback {
            override fun onInterval(
                timeStart: Float,
                timeEnd: Float,
                sendBytes: String,
                bandWidth: String,
                isDown: Boolean
            ) {
                channel.trySend(IperfResult(timeStart, timeEnd, sendBytes, bandWidth, isDown, null, IperfStatus.RUNNING))
            }

            override fun onResult(
                timeStart: Float,
                timeEnd: Float,
                sendBytes: String,
                bandWidth: String,
                isDown: Boolean
            ) {
                channel.trySend(IperfResult(timeStart, timeEnd, sendBytes, bandWidth, isDown, null, IperfStatus.FINISHED))
            }

            override fun onError(errMsg: String) {
                channel.trySend(IperfResult(null, null, null, null, null, errMsg, IperfStatus.ERROR))
            }
        }

        client.exec(config, callback, cacheDir)

        awaitClose {

        }
    }.conflate()




}
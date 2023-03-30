package com.lcl.lclmeasurementtool.features.iperf

import android.util.Log
import com.lcl.lclmeasurementtool.constants.IperfConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class IperfRunner {

    companion object {
        const val TAG = "IperfRunner"
        val iperfUploadConfig: IperfConfig = IperfConfig(
            mServerAddr = IperfConstants.IC_serverAddr,
            mServerPort = IperfConstants.IC_serverPort,
            isDownMode = false,
            userName = IperfConstants.IC_test_username,
            password = IperfConstants.IC_test_password,
            rsaKey = IperfConstants.base64Encode(IperfConstants.IC_SSL_PK),
            interval = 1.0
        )

        val iperfDownloadConfig: IperfConfig = IperfConfig(
            mServerAddr = IperfConstants.IC_serverAddr,
            mServerPort = IperfConstants.IC_serverPort,
            isDownMode = true,
            userName = IperfConstants.IC_test_username,
            password = IperfConstants.IC_test_password,
            rsaKey = IperfConstants.base64Encode(IperfConstants.IC_SSL_PK),
            interval = 1.0
        )

        val regex = "(\\d+\\.\\d+)".toRegex()
    }

    private val doneSignal = CountDownLatch(1)

    fun fakeGetResult(config: IperfConfig, cacheDir: File) = flowOf(
        IperfResult(123.123f, 123.124f, "100", "45.3", true, null, IperfStatus.RUNNING),
        IperfResult(123.123f, 123.124f, "100", "45.5", true, null, IperfStatus.RUNNING),
        IperfResult(123.123f, 123.124f, "100", "42.3", true, null, IperfStatus.RUNNING),
        IperfResult(123.123f, 123.124f, "100", "45.7", true, null, IperfStatus.RUNNING),
        IperfResult(123.123f, 123.124f, "100", "41.3", true, null, IperfStatus.RUNNING),
        IperfResult(123.123f, 123.124f, "100", "40.3", true, null, IperfStatus.FINISHED),
    ).flowOn(Dispatchers.IO).cancellable()


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTestResult(config: IperfConfig, cacheDir: File) = callbackFlow {
        val client = IperfClient()
        val callback = object : IperfCallback {
            override fun onInterval(
                timeStart: Float,
                timeEnd: Float,
                sendBytes: String,
                bandWidth: String,
                isDown: Boolean
            ) {

                Log.d(TAG, "isDown = $isDown, bandWidth = $bandWidth")
                if (!channel.isClosedForSend) {
                    val match = regex.find(bandWidth)
                    match?.let {matchResult ->
                        val (speed) = matchResult.destructured
                        channel.trySend(
                            IperfResult(
                                timeStart,
                                timeEnd,
                                sendBytes,
                                speed,
                                isDown,
                                null,
                                IperfStatus.RUNNING
                            )
                        )
                    }
                } else {
                    client.cancelTest()
                }
            }

            override fun onResult(
                timeStart: Float,
                timeEnd: Float,
                sendBytes: String,
                bandWidth: String,
                isDown: Boolean
            ) {
                Log.d(TAG, "isDown = $isDown, bandWidth = $bandWidth")
                if (!channel.isClosedForSend) {
                    val match = regex.find(bandWidth)
                    match?.let {matchResult ->
                        val (speed) = matchResult.destructured
                        channel.trySend(
                            IperfResult(
                                timeStart,
                                timeEnd,
                                sendBytes,
                                speed,
                                isDown,
                                null,
                                IperfStatus.FINISHED
                            )
                        )
                    }
                } else {
                    client.cancelTest()
                }
            }

            override fun onError(errMsg: String) {
                channel.trySend(
                    IperfResult(
                        0f,
                        0f,
                        "",
                        "",
                        false,
                        errMsg,
                        IperfStatus.ERROR
                    )
                )
                onStopped {
                    client.cancelTest()
                }
            }

        }

        try {
            client.exec(config, callback, cacheDir)
        } catch (e: Exception) {
            Log.d(TAG, "Cancel test in getTestResult for config $config")
            client.cancelTest()
        } finally {
            close()
            doneSignal.countDown()
        }


        awaitClose {
            Log.d(TAG, "Closing the iperf channel ...")
            channel.close()
            onStopped { client.cancelTest() }
        }
    }.conflate().flowOn(Dispatchers.IO).cancellable()

    private fun onStopped(cancellation: (() -> Unit)?) {
        if (cancellation != null) { cancellation() }

        try {
            Log.d(TAG,
                "Awaiting shutdown notification: " + Thread.currentThread().name + ":" + Thread.currentThread().state
            )
            val shutdown = doneSignal.await(10000, TimeUnit.MILLISECONDS)
            if (!shutdown) {
                Log.e(TAG, "Iperf worker timed out on shutdown")
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Iperf worker shutdown interrupted: $e")
        }
    }
}
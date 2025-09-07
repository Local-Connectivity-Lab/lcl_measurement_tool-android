package com.lcl.lclmeasurementtool.features.mlab

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import net.measurementlab.ndt7.android.NDTTest
import net.measurementlab.ndt7.android.models.AppInfo
import net.measurementlab.ndt7.android.models.ClientResponse
import net.measurementlab.ndt7.android.models.Measurement
import net.measurementlab.ndt7.android.utils.DataConverter
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MLabRunner(httpClient: OkHttpClient, private val callback: MLabCallback): NDTTest(httpClient) {
    override fun onDownloadProgress(clientResponse: ClientResponse) {
        super.onDownloadProgress(clientResponse)
        callback.onDownloadProgress(clientResponse)
    }

    override fun onUploadProgress(clientResponse: ClientResponse) {
        super.onUploadProgress(clientResponse)
        callback.onUploadProgress(clientResponse)
    }

    override fun onMeasurementDownloadProgress(measurement: Measurement) {
        super.onMeasurementDownloadProgress(measurement)
        callback.onMeasurementDownloadProgress(measurement)
    }

    override fun onMeasurementUploadProgress(measurement: Measurement) {
        super.onMeasurementUploadProgress(measurement)
        callback.onMeasurementUploadProgress(measurement)
    }

    override fun onFinished(
        clientResponse: ClientResponse?,
        error: Throwable?,
        testType: TestType
    ) {
        super.onFinished(clientResponse, error, testType)
        callback.onFinish(clientResponse, error, testType)
    }

    companion object {
        const val TAG = "MLabRunner"

        fun runTest(testType: TestType) = callbackFlow {
            val callback = object : MLabCallback {
                override fun onDownloadProgress(clientResponse: ClientResponse) {
                    val speed = DataConverter.convertToMbps(clientResponse)
                    Log.d(TAG, "client download is $speed")
                    channel.trySend(MLabResult(speed, TestType.DOWNLOAD, null, MLabTestStatus.RUNNING, null, null, null))
                }

                override fun onUploadProgress(clientResponse: ClientResponse) {
                    val speed = DataConverter.convertToMbps(clientResponse)
                    if (speed != null && speed != "0.0") {
                        channel.trySend(MLabResult(speed, TestType.UPLOAD, null, MLabTestStatus.RUNNING, null, null, null))
                    }
                }

                override fun onMeasurementDownloadProgress(measurement: Measurement) {
                    Log.d(TAG, "on measurement download")
                    val tcpInfo = measurement.tcpInfo
                    val rttMs = tcpInfo?.rtt?.toDouble()?.div(1000.0) // Convert microseconds to milliseconds
                    val minRttMs = tcpInfo?.minRtt?.toDouble()?.div(1000.0) // Convert microseconds to milliseconds
                    
                    // Calculate packet loss percentage if possible
                    val packetLossPercent = if (tcpInfo != null) {
                        val segsOut = tcpInfo.segsOut
                        val totalRetrans = tcpInfo.totalRetrans
                        if (segsOut != null && totalRetrans != null && segsOut > 0) {
                            (totalRetrans.toDouble() / segsOut.toDouble()) * 100.0
                        } else null
                    } else null
                    
                    channel.trySend(MLabResult(null, TestType.DOWNLOAD, null, MLabTestStatus.RUNNING, rttMs, minRttMs, packetLossPercent))
                }

                override fun onMeasurementUploadProgress(measurement: Measurement) {
                    Log.d(TAG, "on measurement upload")
                    val tcpInfo = measurement.tcpInfo
                    val rttMs = tcpInfo?.rtt?.toDouble()?.div(1000.0) // Convert microseconds to milliseconds
                    val minRttMs = tcpInfo?.minRtt?.toDouble()?.div(1000.0) // Convert microseconds to milliseconds
                    
                    // Calculate packet loss percentage if possible
                    val packetLossPercent = if (tcpInfo != null) {
                        val segsOut = tcpInfo.segsOut
                        val totalRetrans = tcpInfo.totalRetrans
                        if (segsOut != null && totalRetrans != null && segsOut > 0) {
                            (totalRetrans.toDouble() / segsOut.toDouble()) * 100.0
                        } else null
                    } else null
                    
                    channel.trySend(MLabResult(null, TestType.UPLOAD, null, MLabTestStatus.RUNNING, rttMs, minRttMs, packetLossPercent))
                }

                override fun onFinish(
                    clientResponse: ClientResponse?,
                    error: Throwable?,
                    testType: TestType
                ) {
                    if (clientResponse != null) {
                        val speed = DataConverter.convertToMbps(clientResponse)
                        Log.d(TAG, "client finish test $testType with speed $speed")
                        channel.trySend(MLabResult(speed, testType, null, MLabTestStatus.FINISHED, null, null, null))
                    } else {
                        Log.e(TAG, "Error during $testType test: ${error?.message}")
                        channel.trySend(MLabResult(null, testType, error?.message, MLabTestStatus.ERROR, null, null, null))
                    }

                    // Only close the channel after both download and upload tests are complete
                    if (testType == TestType.UPLOAD || testType == TestType.DOWNLOAD_AND_UPLOAD) {
                        Log.d(TAG, "Closing channel after $testType test")
                        channel.close()
                    }
                }
            }

            val testRunner = MLabRunner(createHttpClient(), callback)

            testRunner.startTest(testType)
            Log.d(TAG, "right after the testRunner.startTest(testType) call")
            awaitClose {
                Log.d(TAG, "channel is about to close ...")
                testRunner.stopTest()
            }
        }

        private fun createHttpClient(connectTimeout: Long = 30, readTimeout: Long = 30, writeTimeout: Long = 30): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build()
        }
    }
}

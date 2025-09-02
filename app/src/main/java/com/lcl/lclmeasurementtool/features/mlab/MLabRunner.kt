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
        Log.d(TAG, "onDownloadProgress - measurement: ${clientResponse.measurement}, tcpInfo: ${clientResponse.measurement?.tcpInfo}")
        callback.onDownloadProgress(clientResponse)
    }

    override fun onUploadProgress(clientResponse: ClientResponse) {
        super.onUploadProgress(clientResponse)
        Log.d(TAG, "onUploadProgress - measurement: ${clientResponse.measurement}, tcpInfo: ${clientResponse.measurement?.tcpInfo}")
        callback.onUploadProgress(clientResponse)
    }
    
    override fun onMeasurementDownloadProgress(measurement: Measurement) {
        super.onMeasurementDownloadProgress(measurement)
        Log.d(TAG, "onMeasurementDownloadProgress - tcpInfo: ${measurement.tcpInfo}")
        // Note: We don't need to override this method to get RTT. The measurement is passed along
        // in the ClientResponse through the onDownloadProgress callback.
    }

    override fun onMeasurementUploadProgress(measurement: Measurement) {
        super.onMeasurementUploadProgress(measurement)
        Log.d(TAG, "onMeasurementUploadProgress - tcpInfo: ${measurement.tcpInfo}")
        // Note: We don't need to override this method to get RTT. The measurement is passed along
        // in the ClientResponse through the onUploadProgress callback.
    }

    override fun onFinished(
        clientResponse: ClientResponse?,
        error: Throwable?,
        testType: TestType
    ) {
        super.onFinished(clientResponse, error, testType)
        Log.d(TAG, "onFinished - type: $testType, measurement: ${clientResponse?.measurement}, tcpInfo: ${clientResponse?.measurement?.tcpInfo}")
        callback.onFinish(clientResponse, error, testType)
    }

    companion object {
        const val TAG = "MLabRunner"

        fun runTest(testType: TestType) = callbackFlow {
            val callback = object : MLabCallback {
                override fun onDownloadProgress(clientResponse: ClientResponse) {
                    val speed = DataConverter.convertToMbps(clientResponse)
                    Log.d(TAG, "client download is $speed")
                    val measurement = clientResponse.measurement
                    Log.d(TAG, "Download measurement: $measurement, tcpInfo: ${measurement?.tcpInfo}")
                    channel.trySend(MLabResult(speed, TestType.DOWNLOAD, null, MLabTestStatus.RUNNING, measurement?.tcpInfo))
                }

                override fun onUploadProgress(clientResponse: ClientResponse) {
                    val speed = DataConverter.convertToMbps(clientResponse)
                    Log.d(TAG, "client upload is $speed")
                    val measurement = clientResponse.measurement
                    Log.d(TAG, "Upload measurement: $measurement, tcpInfo: ${measurement?.tcpInfo}")
                    channel.trySend(MLabResult(speed, TestType.UPLOAD, null, MLabTestStatus.RUNNING, measurement?.tcpInfo))
                }

                override fun onFinish(
                    clientResponse: ClientResponse?,
                    error: Throwable?,
                    testType: TestType
                ) {
                    if (clientResponse != null) {
                        Log.d(TAG, "client finish test $testType")
                        val measurement = clientResponse.measurement
                        Log.d(TAG, "Finish measurement: $measurement, tcpInfo: ${measurement?.tcpInfo}")
                        channel.trySend(MLabResult(DataConverter.convertToMbps(clientResponse), testType, null, MLabTestStatus.FINISHED, measurement?.tcpInfo))
                    } else {
                        channel.trySend(MLabResult(null, testType, error?.message, MLabTestStatus.ERROR))
                    }

                    if (testType == TestType.UPLOAD) channel.close()
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

        private fun createHttpClient(connectTimeout: Long = 10, readTimeout: Long = 10, writeTimeout: Long = 10): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build()
        }
    }
}


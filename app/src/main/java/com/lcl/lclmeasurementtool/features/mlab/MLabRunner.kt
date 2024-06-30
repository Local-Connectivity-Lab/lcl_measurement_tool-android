package com.lcl.lclmeasurementtool.features.mlab

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import net.measurementlab.ndt7.android.NDTTest
import net.measurementlab.ndt7.android.models.ClientResponse
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
                    channel.trySend(MLabResult(speed, TestType.DOWNLOAD, null, MLabTestStatus.RUNNING))
                }

                override fun onUploadProgress(clientResponse: ClientResponse) {
                    val speed = DataConverter.convertToMbps(clientResponse)
                    Log.d(TAG, "client upload is $speed")
                    channel.trySend(MLabResult(speed, TestType.UPLOAD, null, MLabTestStatus.RUNNING))
                }

                override fun onFinish(
                    clientResponse: ClientResponse?,
                    error: Throwable?,
                    testType: TestType
                ) {
                    if (clientResponse != null) {
                        Log.d(TAG, "client finish test $testType")
                        channel.trySend(MLabResult(DataConverter.convertToMbps(clientResponse), testType, null, MLabTestStatus.FINISHED))
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


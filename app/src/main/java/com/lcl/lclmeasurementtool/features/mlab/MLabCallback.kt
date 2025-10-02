package com.lcl.lclmeasurementtool.features.mlab

import net.measurementlab.ndt7.android.NDTTest
import net.measurementlab.ndt7.android.models.ClientResponse
import net.measurementlab.ndt7.android.models.Measurement

interface MLabCallback {
    fun onDownloadProgress(clientResponse: ClientResponse)
    fun onUploadProgress(clientResponse: ClientResponse)
    fun onMeasurementDownloadProgress(measurement: Measurement)
    fun onMeasurementUploadProgress(measurement: Measurement)
    fun onFinish(clientResponse: ClientResponse?, error: Throwable?, testType: NDTTest.TestType)
}
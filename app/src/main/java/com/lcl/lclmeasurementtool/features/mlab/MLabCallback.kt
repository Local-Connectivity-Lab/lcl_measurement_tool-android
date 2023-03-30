package com.lcl.lclmeasurementtool.features.mlab

import net.measurementlab.ndt7.android.NDTTest
import net.measurementlab.ndt7.android.models.ClientResponse

interface MLabCallback {
    fun onDownloadProgress(clientResponse: ClientResponse)
    fun onUploadProgress(clientResponse: ClientResponse)
    fun onFinish(clientResponse: ClientResponse?, error: Throwable?, testType: NDTTest.TestType)
}
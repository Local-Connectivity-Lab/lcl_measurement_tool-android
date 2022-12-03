package com.lcl.lclmeasurementtool.networking

import android.util.Log
import com.lcl.lclmeasurementtool.Constants.NetworkConstants
import com.lcl.lclmeasurementtool.errors.UploadPostFailedException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object Upload {

    private val client: OkHttpClient = OkHttpClient()
    private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private const val TAG = "UPLOAD_MANAGER"
    private const val ERR_MSG = "Data upload failed."


    /**
     * Post payload to endpoint
     *
     * @param url: the URL of the server
     * @param endpoint: the endpoint to which the payload will post
     * @param payload: the actual content to be posted
     *
     * @throws UploadPostFailedException if the post request failed
     */
    @Throws(UploadPostFailedException::class)
    fun post(url:String, endpoint:String, payload: String) {
        val request: Request = Request.Builder().url(url + endpoint)
                .post(payload.toRequestBody(MEDIA_TYPE)).build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) call.cancel()
                throw UploadPostFailedException(ERR_MSG).also {
                    // let, run, with, apply, and also
                    Log.v(TAG, ERR_MSG)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.isSuccessful) {
                    true -> response.close()
                    else -> throw UploadPostFailedException(ERR_MSG).also {
                        // let, run, with, apply, and also
                        Log.v(TAG, ERR_MSG)
                    }
                }
            }
        })
    }
}
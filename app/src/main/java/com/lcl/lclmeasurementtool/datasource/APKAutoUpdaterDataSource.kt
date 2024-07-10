package com.lcl.lclmeasurementtool.datasource

import android.util.Log
import com.lcl.lclmeasurementtool.BuildConfig
import com.lcl.lclmeasurementtool.networking.APKAutoUpdater
import com.lcl.lclmeasurementtool.networking.Release
import com.lcl.lclmeasurementtool.networking.UpdateInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class APKAutoUpdaterDataSource @Inject constructor() : APKAutoUpdater {

    companion object {
        const val TAG = "APKAutoUpdaterDataSource"
        const val APKNAME = "app-full-debug.apk"
        const val RELEASEURL =
            "https://api.github.com/repos/Local-Connectivity-Lab/lcl_measurement_tool-android/releases/latest"
    }

    override suspend fun canUpdate(): UpdateInfo? {
        val client = OkHttpClient()
        val request: Request = Request.Builder().run {
            url(RELEASEURL)
            addHeader("Accept", "application/vnd.github.v3+json")
            build()
        }

        client.newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                Log.d(TAG, "Unexpected code $response")
                return null
            }
            Log.d(TAG, "response is $response")

            response.body?.let {
                val responseString = it.string()
                Log.d(TAG, "body is $responseString")
                val latestRelease = Json.decodeFromString<Release>(responseString)
                val currentReleaseString = BuildConfig.VERSION_NAME.substringBefore("-")
                val regex = Regex("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)")
                val latestMatchingGroup = regex.find(latestRelease.tagName)!!
                val currentMatchingGroup = regex.find(currentReleaseString)!!
                val shouldForceUpdate = isMajor(latestMatchingGroup, currentMatchingGroup) ||
                                    isMinor(latestMatchingGroup, currentMatchingGroup)


                return UpdateInfo(shouldForceUpdate, latestRelease)
            }
            return null
        }
    }

    private fun isMajor(latest: MatchResult, current: MatchResult): Boolean {
        return matchBy(latest, current, "major")
    }

    private fun isMinor(latest: MatchResult, current: MatchResult): Boolean {
        return matchBy(latest, current, "minor")
    }

    private fun matchBy(latest: MatchResult, current: MatchResult, groupName: String): Boolean {
        return (latest.groups[groupName]?.value ?: "") > (current.groups[groupName]?.value ?: "")
    }
}

suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            this.cancel()
        }
        this.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
        })
    }
}
package com.lcl.lclmeasurementtool.datasource

import android.util.Log
import com.lcl.lclmeasurementtool.BuildConfig
import com.lcl.lclmeasurementtool.networking.APKAutoUpdater
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
        const val RELEASEURL = "https://api.github.com/repos/Local-Connectivity-Lab/lcl_measurement_tool-android/releases/latest"
    }

    @Serializable
    data class Release(
        @SerialName("url") val url: String,
        @SerialName("assets_url") val assetsUrl: String,
        @SerialName("upload_url") val uploadUrl: String,
        @SerialName("html_url") val htmlUrl: String,
        @SerialName("id") val id: Long,
        @SerialName("author") val author: Author,
        @SerialName("node_id") val nodeId: String,
        @SerialName("tag_name") val tagName: String,
        @SerialName("target_commitish") val targetCommitish: String,
        @SerialName("name") val name: String,
        @SerialName("draft") val draft: Boolean,
        @SerialName("prerelease") val prerelease: Boolean,
        @SerialName("created_at") val createdAt: String,
        @SerialName("published_at") val publishedAt: String,
        @SerialName("assets") val assets: List<ReleaseAsset>,
        @SerialName("tarball_url") val tarballUrl: String,
        @SerialName("zipball_url") val zipballUrl: String,
        @SerialName("body") val body: String
    )

    @Serializable
    data class Author(
        @SerialName("login") val login: String,
        @SerialName("id") val id: Long,
        @SerialName("node_id") val nodeId: String,
        @SerialName("avatar_url") val avatarUrl: String,
        @SerialName("gravatar_id") val gravatarId: String,
        @SerialName("url") val url: String,
        @SerialName("html_url") val htmlUrl: String,
        @SerialName("followers_url") val followersUrl: String,
        @SerialName("following_url") val followingUrl: String,
        @SerialName("gists_url") val gistsUrl: String,
        @SerialName("starred_url") val starredUrl: String,
        @SerialName("subscriptions_url") val subscriptionsUrl: String,
        @SerialName("organizations_url") val organizationsUrl: String,
        @SerialName("repos_url") val reposUrl: String,
        @SerialName("events_url") val eventsUrl: String,
        @SerialName("received_events_url") val receivedEventsUrl: String,
        @SerialName("type") val type: String,
        @SerialName("site_admin") val siteAdmin: Boolean
    )

    @Serializable
    data class ReleaseAsset(
        @SerialName("url") val url: String,
        @SerialName("id") val id: Long,
        @SerialName("node_id") val nodeId: String,
        @SerialName("name") val name: String,
        @SerialName("label") val label: String,
        @SerialName("uploader") val uploader: Author,
        @SerialName("content_type") val contentType: String,
        @SerialName("state") val state: String,
        @SerialName("size") val size: Long,
        @SerialName("download_count") val downloadCount: Int,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String,
        @SerialName("browser_download_url") val browserDownloadUrl: String
    )


    override var shouldForceUpdate: Boolean = false
    override lateinit var latestRelease: Release

    override suspend fun canUpdate(): Boolean {
        val client = OkHttpClient()
        val request: Request = Request.Builder().run {
            url(RELEASEURL)
            addHeader("Accept", "application/vnd.github.v3+json")
            build()
        }

        client.newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                Log.d(TAG, "Unexpected code $response")
                return false
            }
            Log.d(TAG, "response is $response")

            response.body?.let {
                val responseString = it.string()
                Log.d(TAG, "body is $responseString")
                latestRelease = Json.decodeFromString(responseString)
                val currentReleaseString = BuildConfig.VERSION_NAME.substringBefore("-")
                val regex = Regex("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)")
                val latestMatchingGroup = regex.find(latestRelease.tagName)!!
                val currentMatchingGroup = regex.find(currentReleaseString)!!
                shouldForceUpdate = isMajor(latestMatchingGroup, currentMatchingGroup) ||
                                    isMinor(latestMatchingGroup, currentMatchingGroup)


                return true

            }
            return false
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
package com.lcl.lclmeasurementtool.sync

import android.util.Log
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (httpException: HttpException) {
    Log.d(
        "suspendRunCatching",
        "HttpException occurred. Returning failure Result",
        httpException
    )
    Result.failure(httpException)
} catch (exception: Exception) {
    Log.d(
        "suspendRunCatching",
        "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result",
        exception
    )
    Result.failure(exception)
}

interface Syncable {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

interface Synchronizer {
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
    suspend fun syncData(action: suspend () -> Unit) = suspendRunCatching { action() }.isSuccess
}

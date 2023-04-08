package com.lcl.lclmeasurementtool.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.lcl.lclmeasurementtool.datastore.Dispatcher
import com.lcl.lclmeasurementtool.datastore.LCLDispatchers
import com.lcl.lclmeasurementtool.model.repository.ConnectivityRepository
import com.lcl.lclmeasurementtool.model.repository.SignalStrengthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.concurrent.TimeUnit

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val signalStrengthRepository: SignalStrengthRepository,
    private val connectivityRepository: ConnectivityRepository,
    @Dispatcher(LCLDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
): CoroutineWorker(context, workerParameters), Synchronizer {

    override suspend fun doWork(): Result =
        withContext(ioDispatcher) {
            try {
                val syncSuccessfully = awaitAll(
                    async { signalStrengthRepository.sync() },
                    async { connectivityRepository.sync() }
                ).all { it }
                if (syncSuccessfully) {
                    Log.d(TAG, "upload successfully")
                    return@withContext Result.success()
                } else {
                    Log.d(TAG, "upload failed. some sync work failed")
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                Log.d(TAG, "upload failed. exception is $e")
                Result.failure()
            }
        }

    companion object {
        fun periodicSyncWork() =
            PeriodicWorkRequest.Builder(UploadWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(30))
            .build()

        const val TAG = "UploadWorker"
    }

}
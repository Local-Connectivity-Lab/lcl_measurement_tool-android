package com.lcl.lclmeasurementtool.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.lcl.lclmeasurementtool.datastore.Dispatcher
import com.lcl.lclmeasurementtool.datastore.LCLDispatchers
import com.lcl.lclmeasurementtool.model.repository.ConnectivityRepository
import com.lcl.lclmeasurementtool.model.repository.SignalStrengthRepository
import com.lcl.lclmeasurementtool.util.Synchronizer
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
                    async {
                        val b = signalStrengthRepository.sync()
                        Log.d(TAG, "signal strength repository finish sync")
                        b
                    },
                    async {
                        val b = connectivityRepository.sync()
                        Log.d(TAG, "connectivity repository finish sync")
                        b
                    }
                ).all { it }

                if (syncSuccessfully) {
                    Log.d(TAG, "upload successfully")
                    Result.success()
                } else {
                    handleRetryOrFallback("upload failed. some sync work failed")
                }
            } catch (e: Exception) {
                handleRetryOrFallback("upload failed. exception is $e")
            }
        }
    private fun handleRetryOrFallback(message: String): Result {
        Log.d(TAG, message)

        return if (runAttemptCount < 3) {
            Log.d(TAG, "Retry attempt $runAttemptCount")
            Result.retry()
        } else {
            Log.d(TAG, "Max retries reached, scheduling periodic work")
            val periodicWork = periodicSyncWork()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "UploadWorkerPeriodic",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWork
            )
            Result.failure()
        }
    }

    companion object {
        fun periodicSyncWork() =
            PeriodicWorkRequestBuilder<DelegatingWorker>(4, TimeUnit.HOURS)
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(10))
            .setInputData(UploadWorker::class.delegatedData())
            .build()

        fun oneTimeSyncWork() =
            OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(5))
            .setInputData(UploadWorker::class.delegatedData())
            .build()

        const val TAG = "UploadWorker"
    }
    

}
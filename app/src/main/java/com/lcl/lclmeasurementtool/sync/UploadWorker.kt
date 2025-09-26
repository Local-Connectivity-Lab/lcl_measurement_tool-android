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
            if (runAttemptCount < 5) {
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
                        return@withContext Result.success()
                    } else {
                        Log.d(TAG, "upload failed. some sync work failed")
                        return@withContext Result.failure()
                    }
                } catch (e: java.io.IOException) {
                    Log.d(TAG, "upload failed with $e, will retry")
                    return@withContext Result.retry()
                } catch (e: retrofit2.HttpException) {
                    Log.d(TAG, "upload failed with $e, will retry")
                    return@withContext Result.retry()
                } catch (e: Exception) {
                    Log.d(TAG, "upload failed with unexpected $e")
                    return@withContext Result.failure()
                }
            } else {
                Log.d(TAG, "Maximum retry attempts (5) reached. Giving up :(")
                return@withContext Result.failure()
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

        const val TAG = "UploadWorker"
    }

}
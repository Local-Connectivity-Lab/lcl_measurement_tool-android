package com.lcl.lclmeasurementtool.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestWorkerBuilder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class UploadWorkerTest {

    private lateinit var context: Context
    private lateinit var signalRepo: SignalStrengthRepository
    private lateinit var connectivityRepo: ConnectivityRepository
    private lateinit var ioDispatcher: TestCoroutineDispatcher

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        signalRepo = mockk()
        connectivityRepo = mockk()
        ioDispatcher = TestCoroutineDispatcher()
    }

    private fun createWorker(runAttemptCount: Int = 0): UploadWorker {
        return TestWorkerBuilder<UploadWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return UploadWorker(appContext, workerParameters, signalRepo, connectivityRepo, ioDispatcher)
                }
            })
            .setInputData(Data.EMPTY)
            .setRunAttemptCount(runAttemptCount)
            .build()
    }

    @Test
    fun testDoWork_success() = runTest {
        coEvery { signalRepo.sync() } returns true
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun testDoWork_failure_syncFails() = runTest {
        coEvery { signalRepo.sync() } returns false
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun testDoWork_retryOnIOException() = runTest {
        coEvery { signalRepo.sync() } throws IOException("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.retry(), result)
    }

    @Test
    fun testDoWork_retryOnHttpException() = runTest {
        coEvery { signalRepo.sync() } throws HttpException(mockk())
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.retry(), result)
    }

    @Test
    fun testDoWork_failureOnInvalidKeySpecException() = runTest {
        coEvery { signalRepo.sync() } throws java.security.spec.InvalidKeySpecException("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun testDoWork_failureOnInvalidKeyException() = runTest {
        coEvery { signalRepo.sync() } throws java.security.InvalidKeyException("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun testDoWork_failureOnIllegalStateException() = runTest {
        coEvery { signalRepo.sync() } throws IllegalStateException("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun testDoWork_failureOnGenericException() = runTest {
        coEvery { signalRepo.sync() } throws Exception("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun testDoWork_failureOnMaxRetries() = runTest {
        coEvery { signalRepo.sync() } throws IOException("Test")
        coEvery { connectivityRepo.sync() } returns true

        val worker = createWorker(runAttemptCount = 5)
        val result = worker.doWork()

        assertEquals(Result.failure(), result)
    }
}
package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractIperfWorker extends Worker {
    private static final String TAG = "IPERF_WORKER";

    Iperf3Client client;
    Iperf3Callback callback;
    Iperf3Config config;
    protected Context context;
    protected CountDownLatch doneSignal;

    Data finalData;

    abstract void prepareConfig();

    public AbstractIperfWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.client = new Iperf3Client();
        this.doneSignal = new CountDownLatch(1);
    }

    void prepareCallback() {
        this.callback = new Iperf3Callback() {
            @Override
            public void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown) {
                Log.i(TAG, "on interval " + bandWidth + " isDown=" + isDown);
                setProgressAsync(new Data.Builder()
                        .putString("INTERVAL_BANDWIDTH", bandWidth)
                        .putBoolean("IS_DOWN_MODE", isDown)
                        .build()
                );
            }

            @Override
            public void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown) {
                Log.i(TAG, "final result " + bandWidth + " isDown:" + isDown);
                finalData = new Data.Builder()
                        .putString("FINAL_RESULT", bandWidth)
                        .putBoolean("IS_DOWN_MODE", isDown)
                        .build();
            }

            @Override
            public void onError(String errMsg) {
                Log.e(TAG, errMsg);
            }
        };
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "Stopping in thread " + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());

        // Note: this might run in a different thread than the doWork context depending on the
        // WorkManager executor
        client.cancelTest();

        try {
            Log.d(TAG, "Awaiting shutdown notification" + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
            boolean shutdown = doneSignal.await(10000, TimeUnit.MILLISECONDS);
            if (!shutdown) {
                Log.e(TAG, "Iperf worker timed out on shutdown");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Iperf worker shutdown interrupted: " + e.toString());
        }
    }
}

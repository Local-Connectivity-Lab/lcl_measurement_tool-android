package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.lang.Thread;

public abstract class AbstractIperfWorker extends Worker {
    private static final String TAG = "IPERF_WORKER";

    Iperf3Client client;
    Iperf3Callback callback;
    Iperf3Config config;
    protected Context context;

    boolean isTestFailed;
    Data finalData;

    abstract void prepareConfig();

    public AbstractIperfWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.client = new Iperf3Client();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "Worker stopping in thread " + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());

        // TODO(matt9j) This might run in a different thread than the doWork context?!?
        // Cancel the iperf test first before calling the superclass method to end the test before killing the worker thread???
        client.cancelTest();
    }

    void prepareCallback() {
        this.callback = new Iperf3Callback() {
            @Override
            public void onConnecting(String destHost, int destPort) {
                Log.i(TAG, "connecting to " + destHost + " through port " + destPort);
            }

            @Override
            public void onConnected(String localAddr, int localPort, String destAddr, int destPort) {
                Log.i(TAG, localAddr + ":" + localPort + " => " + destAddr + ":" + destPort);
            }

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
                isTestFailed = true;
                client.cancelTest();
            }
        };
    }
}

package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public abstract class AbstractPingWorker extends Worker {
    private static final String TAG = "PING_WORKER";

    private Context context;
    PingListener listener;
    Ping pingClient;
    boolean isTestFailed;
    Data finalData;

    public AbstractPingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.pingClient = new Ping();
    }

    abstract void prepareConfig();

    void prepareCallback() {
        listener = new PingListener() {
            @Override
            public void onError(String ex) {
                Log.e(TAG, ex);
                isTestFailed = true;
                pingClient.stop();
            }

            @Override
            public void onStart() {
                Log.i(TAG, "ping test starts");
            }

            @Override
            public void onFinished(PingStats stats) {
                double avg = stats.getAverageLatency();
                finalData = new Data.Builder()
                        .putString("FINAL_RESULT", String.valueOf(avg) + " ms")
                        .build();
            }
        };
    }

    @Override
    public void onStopped() {
        super.onStopped();
        pingClient.stop();
    }
}

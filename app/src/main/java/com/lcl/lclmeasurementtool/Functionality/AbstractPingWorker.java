package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * An abstract worker handling the ping test
 */
public abstract class AbstractPingWorker extends Worker {

    // debugging tag
    private static final String TAG = "PING_WORKER";

    private Context context;

    // the ping listener
    PingListener listener;

    // the ping client
    Ping pingClient;

    // indicate whether the ping test failed
    boolean isTestFailed;

    // the final data output
    Data finalData;

    public AbstractPingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.pingClient = new Ping();
    }

    /**
     * Prepare necessary ping configuration
     */
    abstract void prepareConfig();

    /**
     * prepare callback functions
     */
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
                        .putString("FINAL_RESULT", avg + " ms")
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

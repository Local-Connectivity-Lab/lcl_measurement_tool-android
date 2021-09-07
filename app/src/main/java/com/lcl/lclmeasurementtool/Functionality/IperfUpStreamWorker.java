package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.lang.Thread;

public class IperfUpStreamWorker extends AbstractIperfWorker {

    private static final String TAG = "IPERF_UPSTREAM_WORKER";

    public IperfUpStreamWorker(@NonNull Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    void prepareConfig() {
        Log.i(TAG, "now preparing config for upstream");
        config = new Iperf3Config();
        config.mServerAddr = getInputData().getString("SERVER_ADDR");
        config.mServerPort = getInputData().getInt("SERVER_PORT", 5201);
        config.isDownMode = true;
        Log.i(TAG, config.mServerAddr + ":" + config.mServerPort + " isDown="+config.isDownMode);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Beginning synchronous upstream work in thread " + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
        try {
            prepareConfig();
            prepareCallback();

            if (!isStopped()) client.exec(config, callback);
            return isTestFailed ?
                    Result.failure() : ( finalData == null ? Result.success() : Result.success(finalData));
        } catch (Exception e) {
            Log.e(TAG, "failed to run iperf test: " + e);
            return Result.failure();
        }
    }
}

package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.lang.Thread;


public class IperfDownStreamWorker extends AbstractIperfWorker {

    private static final String TAG = "IPERF_DOWNSTREAM_WORKER";

    public IperfDownStreamWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    void prepareConfig() {
        Log.i(TAG, "now preparing config for downstream");
        config = new Iperf3Config();
        config.mServerAddr = getInputData().getString("SERVER_ADDR");
        config.mServerPort = getInputData().getInt("SERVER_PORT", 5201);
        config.isDownMode = false;
        Log.i(TAG, config.mServerAddr + ":" + config.mServerPort + " isDown="+config.isDownMode);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Beginning synchronous downstream work in thread " + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
        prepareConfig();
        prepareCallback();

        Log.d(TAG, "Beginning background test");
        try {
            client.exec(config, callback, context.getCacheDir());
        } catch (RuntimeException e) {
            // TODO(matt9j) Propagate the error cause to some kind of error reporting or app metrics!
            Log.e(TAG, "Background test failed");
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Fell through to generic handler");
        } finally {
            Log.d(TAG, "Work finally statement");
            doneSignal.countDown();
        }

        if (finalData == null) {
            Log.w(TAG, "Iperf worker completed successfully without returning final data");
            return Result.success();
        } else {
            Log.i(TAG, "Iperf worker completed successfully");
            return Result.success(finalData);
        }
    }
}

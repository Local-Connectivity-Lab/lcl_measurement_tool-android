package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.WorkerParameters;

import com.lcl.lclmeasurementtool.Constants.IperfConstants;

import java.lang.Thread;

/**
 * A worker responsible for handling the iperf upstream test
 */
public class IperfUpStreamWorker extends AbstractIperfWorker {

    // debugging tag
    private static final String TAG = "IPERF_UPSTREAM_WORKER";

    public IperfUpStreamWorker(@NonNull Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void prepareConfig() {
        Log.d(TAG, "Preparing upstream test config");
        config = new Iperf3Config();
        config.mServerAddr = IperfConstants.IC_serverAddr;
        config.mServerPort = IperfConstants.IC_serverPort;
        config.isDownMode = false;

        // TODO(johnnzhou) update security config
        config.userName = IperfConstants.IC_isDebug ?
                IperfConstants.IC_test_username : getInputData().getString("userName");
        config.password = IperfConstants.IC_isDebug ?
                IperfConstants.IC_test_password : getInputData().getString("password");
        config.rsaKey = IperfConstants.Base64Encode(IperfConstants.IC_SSL_PK);
        Log.d(TAG, config.mServerAddr + ":" + config.mServerPort + " isDown="+config.isDownMode);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Beginning synchronous upstream work in thread " + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());

        prepareConfig();
        prepareCallback();

        try {
            client.exec(config, callback, context.getCacheDir());
        } catch (RuntimeException e) {
            // TODO(matt9j) Propagate the error cause to some kind of error reporting or app metrics!
            Log.e(TAG, "Background test failed");
            return Result.failure();
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

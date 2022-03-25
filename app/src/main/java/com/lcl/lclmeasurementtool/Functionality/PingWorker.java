package com.lcl.lclmeasurementtool.Functionality;

import static com.lcl.lclmeasurementtool.Constants.NetworkConstants.IPERF_COUNTS;
import static com.lcl.lclmeasurementtool.Constants.NetworkConstants.IPERF_COUNTS_TAG;
import static com.lcl.lclmeasurementtool.Constants.NetworkConstants.IPERF_TEST_ADDRESS;
import static com.lcl.lclmeasurementtool.Constants.NetworkConstants.IPERF_TEST_ADDRESS_TAG;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

/**
 * A worker working on the ping task
 */
public class PingWorker extends AbstractPingWorker {

    // debugging tag
    private static final String TAG = "PING_WORKER";

    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    void prepareConfig() {
        int times = getInputData().getInt(IPERF_COUNTS_TAG, IPERF_COUNTS);
        String address = getInputData().getString(IPERF_TEST_ADDRESS_TAG);
        if (address == null) address = IPERF_TEST_ADDRESS;
        pingClient = pingClient.setTimes(times).setAddress(address).setTimeout(1000);
    }

    @NonNull
    @Override
    public Result doWork() {
        prepareConfig();
        prepareCallback();

        pingClient.start(listener);

        return isTestFailed ? Result.failure() : ( finalData == null ? Result.success() : Result.success(finalData));
    }
}

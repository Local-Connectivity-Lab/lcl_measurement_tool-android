package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public class PingWorker extends AbstractPingWorker {

    private static final String TAG = "PING_WORKER";

    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    void prepareConfig() {
        int times = getInputData().getInt("TIMES", 5);
        String address = getInputData().getString("ADDRESS");
        if (address == null) address = "google.com";
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

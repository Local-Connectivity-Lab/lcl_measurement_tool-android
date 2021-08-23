package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

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

        prepareConfig();
        prepareCallback();

        client.exec(config, callback);
        return isTestFailed ?
                Result.failure() : ( finalData == null ? Result.success() : Result.success(finalData));
    }
}

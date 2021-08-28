package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

public class NetworkTestViewModel extends ViewModel {

    private static final String TAG = "Network Test ViewModel";

    WorkManager mWorkManager;

    private LiveData<List<WorkInfo>> mSavedIperfDownInfo;
    private LiveData<List<WorkInfo>> mSavedIperfUpInfo;
    private UUID downStreamUUID;
    private UUID upStreamUUID;

    public NetworkTestViewModel(@NonNull Context context) {
        mWorkManager = WorkManager.getInstance(context);
        mWorkManager.pruneWork();
        mSavedIperfDownInfo = mWorkManager.getWorkInfosByTagLiveData("IPERF_DOWN");
        mSavedIperfUpInfo = mWorkManager.getWorkInfosByTagLiveData("IPERF_UP");
    }

    public LiveData<List<WorkInfo>> getmSavedIperfDownInfo() {
        return mSavedIperfDownInfo;
    }

    public LiveData<List<WorkInfo>> getmSavedIperfUpInfo() {
        return mSavedIperfUpInfo;
    }

    public void run() {

        OneTimeWorkRequest downStream = new OneTimeWorkRequest.Builder(IperfDownStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_DOWN")
                .build();
        downStreamUUID = downStream.getId();
        OneTimeWorkRequest upStream = new OneTimeWorkRequest.Builder(IperfUpStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_UP").build();
        upStreamUUID = upStream.getId();

        WorkContinuation continuation = mWorkManager.beginWith(downStream);
        continuation = continuation.then(upStream);

        continuation.enqueue();
    }

    public void cancel() {

        // TODO: cancel specific work with Tag
        Log.e(TAG, "cancel tests");
        mWorkManager.cancelWorkById(downStreamUUID);
        mWorkManager.cancelWorkById(upStreamUUID);
    }

    private Data prepareIperfWorkerData() {
        Data.Builder builder = new Data.Builder();
        builder.putInt("SERVER_PORT", 5203);
        builder.putString("SERVER_ADDR", "iperf.biznetnetworks.com");
        return builder.build();
    }
}

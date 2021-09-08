package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class NetworkTestViewModel extends ViewModel {

    private static final String TAG = "NetworkTestViewModel";

    private WorkManager mWorkManager;

    private LiveData<List<WorkInfo>> mSavedIperfDownInfo;
    private LiveData<List<WorkInfo>> mSavedIperfUpInfo;
    private UUID downStreamUUID;
    private UUID upStreamUUID;

    public NetworkTestViewModel(@NonNull Context context) {
        mWorkManager = WorkManager.getInstance(context);
        mWorkManager.pruneWork();
        // TODO HACK! For now cancel any pending work leftover from previous invocations of the app?
        // The app should probably eventually list the status of all pending and completed tests the user has done?
        mWorkManager.cancelAllWorkByTag("backgroundIperf");
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

        // TODO: Clarify the background work model we want exposed to end users... should these be "unique" work?
        OneTimeWorkRequest downStream = new OneTimeWorkRequest.Builder(IperfDownStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_DOWN")
                .addTag("backgroundIperf")
                .build();
        downStreamUUID = downStream.getId();
        OneTimeWorkRequest upStream = new OneTimeWorkRequest.Builder(IperfUpStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_UP")
                .addTag("backgroundIperf")
                .build();
        upStreamUUID = upStream.getId();

        WorkContinuation continuation = mWorkManager.beginWith(downStream);
        continuation = continuation.then(upStream);

        continuation.enqueue();
    }

    public void cancel() {
        Log.e(TAG, "cancel tests");
        mWorkManager.cancelWorkById(downStreamUUID);
        mWorkManager.cancelWorkById(upStreamUUID);
        // TODO: Clarify the background work model we want exposed to end users
        mWorkManager.cancelAllWorkByTag("backgroundIperf");
    }

    private Data prepareIperfWorkerData() {
        Data.Builder builder = new Data.Builder();
        builder.putInt("SERVER_PORT", 5203);
        builder.putString("SERVER_ADDR", "iperf.biznetnetworks.com");
        return builder.build();
    }
}

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
    private LiveData<List<WorkInfo>> mSavedPingInfo;
    private UUID downStreamUUID;
    private UUID upStreamUUID;
    private UUID pingUUID;

    public NetworkTestViewModel(@NonNull Context context) {
        mWorkManager = WorkManager.getInstance(context);
        mWorkManager.pruneWork();
        // TODO HACK! For now cancel any pending work leftover from previous invocations of the app?
        // The app should probably eventually list the status of all pending and completed tests the user has done?
        mWorkManager.cancelAllWorkByTag("backgroundTest");
        mSavedIperfDownInfo = mWorkManager.getWorkInfosByTagLiveData("IPERF_DOWN");
        mSavedIperfUpInfo = mWorkManager.getWorkInfosByTagLiveData("IPERF_UP");
        mSavedPingInfo = mWorkManager.getWorkInfosByTagLiveData("PING");
    }

    public LiveData<List<WorkInfo>> getmSavedPingInfo() {
        return mSavedPingInfo;
    }

    public LiveData<List<WorkInfo>> getmSavedIperfDownInfo() {
        return mSavedIperfDownInfo;
    }

    public LiveData<List<WorkInfo>> getmSavedIperfUpInfo() {
        return mSavedIperfUpInfo;
    }

    public void run() {

        // TODO: Clarify the background work model we want exposed to end users... should these be "unique" work?
        OneTimeWorkRequest ping = new OneTimeWorkRequest.Builder(PingWorker.class)
                .setInputData(preparePingWorkerData())
                .addTag("PING")
                .addTag("backgroundTest")
                .build();
        pingUUID = ping.getId();
        OneTimeWorkRequest downStream = new OneTimeWorkRequest.Builder(IperfDownStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_DOWN")
                .addTag("backgroundTest")
                .build();
        downStreamUUID = downStream.getId();
        OneTimeWorkRequest upStream = new OneTimeWorkRequest.Builder(IperfUpStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag("IPERF_UP")
                .addTag("backgroundTest")
                .build();
        upStreamUUID = upStream.getId();

        WorkContinuation continuation = mWorkManager.beginWith(ping);
        continuation = continuation.then(downStream);
        continuation = continuation.then(upStream);

        continuation.enqueue();
    }

    public void cancel() {
        Log.v(TAG, "cancel tests");
        mWorkManager.cancelWorkById(pingUUID);
        mWorkManager.cancelWorkById(downStreamUUID);
        mWorkManager.cancelWorkById(upStreamUUID);
        // TODO: Clarify the background work model we want exposed to end users
        mWorkManager.cancelAllWorkByTag("backgroundTest");
    }

    private Data prepareIperfWorkerData() {
        Data.Builder builder = new Data.Builder();
        // TODO Determine the correct server(s) based on the network we are attached to?
        builder.putInt("SERVER_PORT", 5201);
        builder.putString("SERVER_ADDR", "iperf.scottlinux.com");
        return builder.build();
    }

    private Data preparePingWorkerData() {
        Data.Builder builder = new Data.Builder();
        builder.putInt("TIMES", 5);
        builder.putString("ADDRESS", "google.com");
        return builder.build();
    }
}

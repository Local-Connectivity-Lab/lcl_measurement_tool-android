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

import com.lcl.lclmeasurementtool.Constants.NetworkConstants;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * A viewmodel handling running the ping and iperf test asynchronously
 */
public class NetworkTestViewModel extends ViewModel {

    // debugging tag
    private static final String TAG = "NetworkTestViewModel";

    // the work manager managing all the workers
    private final WorkManager mWorkManager;

    // the live data from iperf downstream test
    private final LiveData<List<WorkInfo>> mSavedIperfDownInfo;

    // the live data from iperf upstream test
    private final LiveData<List<WorkInfo>> mSavedIperfUpInfo;

    // the live data from the ping test
    private final LiveData<List<WorkInfo>> mSavedPingInfo;

    // the UUID for the downstream iperf test
    private UUID downStreamUUID;

    // the UUID for the upstream iperf test
    private UUID upStreamUUID;

    // the UUID for the ping test
    private UUID pingUUID;

    public NetworkTestViewModel(@NonNull Context context) {
        mWorkManager = WorkManager.getInstance(context);
        // TODO HACK! For now cancel any pending work leftover from previous invocations of the app?(mattj)
        // The app should probably eventually list the status of all pending and completed tests the user has done?
        mWorkManager.cancelAllWorkByTag(NetworkConstants.WORKER_TAG);
        mSavedIperfDownInfo = mWorkManager.getWorkInfosByTagLiveData(NetworkConstants.IPERF_DOWN_TAG);
        mSavedIperfUpInfo = mWorkManager.getWorkInfosByTagLiveData(NetworkConstants.IPERF_UP_TAG);
        mSavedPingInfo = mWorkManager.getWorkInfosByTagLiveData(NetworkConstants.PING_TAG);
    }

    /**
     * Retrieve the live data of ping test
     * @return the live data of the ping test
     */
    public LiveData<List<WorkInfo>> getmSavedPingInfo() {
        return mSavedPingInfo;
    }

    /**
     * Retrieve the live data of the iperf downstream test
     * @return the live data of the iperf downstream test
     */
    public LiveData<List<WorkInfo>> getmSavedIperfDownInfo() {
        return mSavedIperfDownInfo;
    }

    /**
     * Retrieve the live data of the iperf upstream test
     * @return the live data of the iperf upstream test
     */
    public LiveData<List<WorkInfo>> getmSavedIperfUpInfo() {
        return mSavedIperfUpInfo;
    }

    /**
     * Run the test
     */
    public void run() {

        // prune the remaining tests in the pipeline
        mWorkManager.pruneWork();
        // TODO: Clarify the background work model we want exposed to end users... should these be "unique" work?
        OneTimeWorkRequest ping = new OneTimeWorkRequest.Builder(PingWorker.class)
                .setInputData(preparePingWorkerData())
                .addTag(NetworkConstants.PING_TAG)
                .addTag(NetworkConstants.WORKER_TAG)
                .build();
        pingUUID = ping.getId();
        OneTimeWorkRequest downStream = new OneTimeWorkRequest.Builder(IperfDownStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag(NetworkConstants.IPERF_DOWN_TAG)
                .addTag(NetworkConstants.WORKER_TAG)
                .build();
        downStreamUUID = downStream.getId();
        OneTimeWorkRequest upStream = new OneTimeWorkRequest.Builder(IperfUpStreamWorker.class)
                .setInputData(prepareIperfWorkerData())
                .addTag(NetworkConstants.IPERF_UP_TAG)
                .addTag(NetworkConstants.WORKER_TAG)
                .build();
        upStreamUUID = upStream.getId();

        WorkContinuation continuation = mWorkManager.beginWith(ping);
        continuation = continuation.then(downStream);
        continuation = continuation.then(upStream);

        continuation.enqueue();
    }

    /**
     * Cancle the running tests
     */
    public void cancel() {
        Log.v(TAG, "cancel tests");
        mWorkManager.cancelWorkById(pingUUID);
        mWorkManager.cancelWorkById(downStreamUUID);
        mWorkManager.cancelWorkById(upStreamUUID);
        // TODO: Clarify the background work model we want exposed to end users
        mWorkManager.cancelAllWorkByTag(NetworkConstants.WORKER_TAG);
    }

    /**
     * Prepare necessary data for iperf test
     * @return data for iperf test
     */
    private Data prepareIperfWorkerData() {
        Data.Builder builder = new Data.Builder();
        return builder.build();
    }

    /**
     * Prepare necessary data for ping test
     * @return data for ping test
     */
    private Data preparePingWorkerData() {
        Data.Builder builder = new Data.Builder();
        builder.putInt(NetworkConstants.IPERF_COUNTS_TAG, NetworkConstants.IPERF_COUNTS);
        builder.putString(NetworkConstants.IPERF_TEST_ADDRESS_TAG, NetworkConstants.IPERF_TEST_ADDRESS);
        return builder.build();
    }
}

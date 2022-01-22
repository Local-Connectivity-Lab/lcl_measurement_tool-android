package com.lcl.lclmeasurementtool.Functionality;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.lcl.lclmeasurementtool.Managers.UploadManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UploadViewModel extends AndroidViewModel {

//    private UploadManager uploadManager;
    public UploadViewModel(@NonNull Application application) {
        super(application);
//        uploadManager = UploadManager.Builder();
    }

//    public void loadData(String json) {
//        uploadManager.addEndpoint(UploadManager.CONNECTIVITY_ENDPOINT);
//        uploadManager.addPayload(json);
//    }


    public void upload() throws IOException {
//        Constraints constraints = new Constraints.Builder()
//                                    .setRequiredNetworkType(NetworkType.CONNECTED).build();
//        OneTimeWorkRequest upload = new OneTimeWorkRequest.Builder(UploadWorker.class)
//                                        .setInputData(data)
//                                        .setConstraints(constraints)
//                                        .setBackoffCriteria(
//                                                BackoffPolicy.LINEAR,
//                                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                                                TimeUnit.MILLISECONDS)
//                                        .build();
//        workManager.enqueue(upload);
    }


//    @SuppressLint("RestrictedApi")
//    private Data prepareUploadData(Map<String, Object> map, int endpoint) {
//        Data.Builder builder = new Data.Builder();
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            builder.put(entry.getKey(), entry.getValue());
//        }
//        builder.putInt("TYPE", endpoint);
//
//        return builder.build();
//    }
}

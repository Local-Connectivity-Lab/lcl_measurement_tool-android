package com.lcl.lclmeasurementtool.Functionality;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.lcl.lclmeasurementtool.Managers.UploadManager;

import java.io.IOException;

public class UploadWorker extends Worker {

    private UploadManager uploadManager;
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        uploadManager = UploadManager.Builder();
    }

    private void prepareData() {
        uploadManager.addFields(getInputData().getKeyValueMap()).serialize();
    }

    @NonNull
    @Override
    public Result doWork() {

        prepareData();

        try {
            uploadManager.post();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }

        return Result.success();
    }
}

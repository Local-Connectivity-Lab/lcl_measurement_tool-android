package com.lcl.lclmeasurementtool.Managers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.lcl.lclmeasurementtool.Constants.NetworkConstants;
import com.lcl.lclmeasurementtool.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadManager {
    private static final String TAG = "UPLOAD_MANAGER";
    private static UploadManager instance;
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String ERR_MSG = "Data upload failed. Please contact the administrator at lcl@seattlecommunitynetwork.org.";

    private final OkHttpClient client;
    private String json;
    private String endpoint;

    private UploadManager() {
        client = new OkHttpClient();
    }

    public static UploadManager Builder() {
        if (instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }

    public void post() throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("JSON data should not be null");
        }

        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        Request request = new Request.Builder().url(NetworkConstants.URL + endpoint).post(body).build();

        Log.i(TAG, "prepare for upload");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
                TipDialog.show(ERR_MSG);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "error occurs on " + endpoint);
                    TipDialog.show(ERR_MSG);
                }

                response.close();
            }
        });
    }

    public UploadManager addPayload(String json) {
        this.json = json;
        return this;
    }

    public UploadManager addEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
}

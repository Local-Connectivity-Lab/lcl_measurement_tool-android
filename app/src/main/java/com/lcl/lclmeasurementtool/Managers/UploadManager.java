package com.lcl.lclmeasurementtool.Managers;

import androidx.annotation.NonNull;

import com.kongzue.dialogx.dialogs.TipDialog;
import com.lcl.lclmeasurementtool.Constants.NetworkConstants;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadManager {

    private static UploadManager instance;
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                TipDialog.show("Data upload failed. Please contact the administrator");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    TipDialog.show("Data upload failed. Please contact the administrator");
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

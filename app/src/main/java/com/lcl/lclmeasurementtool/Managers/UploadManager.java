package com.lcl.lclmeasurementtool.Managers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.lcl.lclmeasurementtool.Constants.NetworkConstants;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.AnalyticsUtils;
import com.microsoft.appcenter.analytics.Analytics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A manager handling networking(upload) with the report server
 */
public class UploadManager {
    // debugging tag
    private static final String TAG = "UPLOAD_MANAGER";

    // singleton instance
    private static UploadManager instance;

    // HTTP media type
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    // error message
    private static final String ERR_MSG = "Data upload failed. Please contact the administrator at lcl@seattlecommunitynetwork.org.";

    // HTTP client
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

    /**
     * Post data to the server
     * @throws IOException when IO error occurs during networking
     */
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
                    String body = response.body().string();
                    Log.e(TAG, body);
                    Map<String, String> reason = AnalyticsUtils.formatProperties(endpoint, body);
                    Analytics.trackEvent(AnalyticsUtils.UPLOAD_FAILED, reason);
                    TipDialog.show(ERR_MSG);
                } else {
                    Analytics.trackEvent(AnalyticsUtils.DATA_UPLOADED + " on " + endpoint);
                }

                response.close();
            }
        });
    }

    /**
     * Add json payload to the message body
     * @param json  the json string to be uploaded
     * @return      A upload manager with the json data
     */
    public UploadManager addPayload(String json) {
        this.json = json;
        return this;
    }

    /**
     * Add endpoint indicating where data will be uploaded to
     * @param endpoint  the endpoint indicating the server endpoint
     * @return          A upload manager holding the endpoint information
     */
    public UploadManager addEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
}

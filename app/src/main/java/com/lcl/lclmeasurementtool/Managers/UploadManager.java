package com.lcl.lclmeasurementtool.Managers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.jsoniter.output.JsonStream;

public class UploadManager {

    private static UploadManager instance;
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String url = "https://api-dev.seattlecommunitynetwork.org/";
    private Map<String, Object> map;

    private OkHttpClient client;
    private String json;
    private String endpoint;


    private UploadManager() {
        client = new OkHttpClient();
        map = new HashMap<>();
    }

    public void post() throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("JSON data should not be null");
        }

        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        Request request = new Request.Builder().url(url + endpoint).post(body).build();
        Response response = client.newCall(request).execute();
    }

    public static UploadManager Builder() {
        if (instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }

    public UploadManager addField(String key, Object val) {
        map.putIfAbsent(key, val);
        return this;
    }

    public UploadManager addFields(Map<String, Object> map) {
        if (!map.containsKey("TYPE")) {
            throw new IllegalArgumentException("input data doesn't specify upload type");
        }

        int type = (int) map.get("TYPE");
        map.remove("TYPE");
        switch (type) {
            case 0:
                endpoint = "api/data";
            case 1:
                endpoint = "";
        }
        this.map = map;
        return this;
    }


    public UploadManager serialize() {
        this.json = JsonStream.serialize(map);
        return this;
    }

//    public UploadManager addEndpoint(String endpoint) {
//        this.endpoint = endpoint;
//        return this;
//    }
}

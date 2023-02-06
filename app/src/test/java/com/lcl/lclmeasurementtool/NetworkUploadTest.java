//package com.lcl.lclmeasurementtool;
//
//import androidx.annotation.NonNull;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.IOException;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.HttpUrl;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//
//public class NetworkUploadTest {
//
//    static MockWebServer mockWebServer;
//    static OkHttpClient client;
//
//    static {
//        mockWebServer = new MockWebServer();
//        client = new OkHttpClient();
//        try {
//            mockWebServer.start();
//            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("hello world!"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testRegistration() {
//        HttpUrl url = mockWebServer.url("/");
//        RequestBody requestBody = RequestBody.create("abc", MediaType.get("application/json; charset=utf-8"));
//        Request request = new Request.Builder().url(url).post(requestBody).build();
//        try {
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                System.out.println(response.body().string());
//            } else {
//                Assert.fail();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @After
//    public void deinit() {
//        try {
//            mockWebServer.shutdown();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

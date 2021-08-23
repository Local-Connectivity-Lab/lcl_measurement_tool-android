package com.lcl.lclmeasurementtool.Functionality;

import android.os.Looper;
import android.util.Log;

public class Iperf3Client {

    static {
        System.loadLibrary("iperf3");
    }

    private Iperf3Callback mCallback;
    private Iperf3Config mConfig;
    private boolean stopTesting;

    private Thread iperfThread;

    public Iperf3Client(Iperf3Callback callback, Iperf3Config config) {
        this();
        mCallback = callback;
        mConfig = config;
    }

    public Iperf3Client(Iperf3Callback callback) {
        this();
        mCallback = callback;
    }

    public Iperf3Client() { }

    ///////////////////// NATIVE FUNCTION //////////////////////////

    private native void simpleTest(String serverIp, String serverPort, boolean isDownMode, Iperf3Callback callback);

    public native void exec(Iperf3Config testConfig, Iperf3Callback callback);

    ////////////////////// JAVA INVOCATION ////////////////////////

    public void exec(Iperf3Config testConfig) {
        iperfThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopTesting = false;
                Looper.prepare();
                exec(testConfig, mCallback);
                if (stopTesting) {
                    Looper.myLooper().quitSafely();
                    Log.i("IPERF3", "quit looper");
                }
                Looper.loop();
            }
        });

        iperfThread.start();
    }

    public void exec(String serverIp, String serverPort, boolean isDownMode) {
        simpleTest(serverIp, serverPort, isDownMode, mCallback);
    }

    public void stop() {
        stopTesting = true;
    }
}

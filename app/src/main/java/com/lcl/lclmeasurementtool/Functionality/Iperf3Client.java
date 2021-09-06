package com.lcl.lclmeasurementtool.Functionality;

import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;

public class Iperf3Client {

    static {
        System.loadLibrary("lcl_measurement_tool_native");
    }

    private Iperf3Callback mCallback;
    private Iperf3Config mConfig;
    private boolean stopTesting;
    Thread iperfThread;

    public Iperf3Client(Iperf3Callback callback, Iperf3Config config) {
        this();
        mCallback = callback;
        mConfig = config;
    }

    public Iperf3Client(Iperf3Callback callback) {
        this();
        mCallback = callback;
    }

    public Iperf3Client() {
        this.stopTesting = false;
    }

    ///////////////////// NATIVE FUNCTION //////////////////////////

    private native void runIperfTest(Iperf3Config testConfig, Iperf3Callback callback);

    private native void stopIperfTest();

    ////////////////////// JAVA INVOCATION ////////////////////////

    public void exec(Iperf3Config testConfig, Iperf3Callback callback) {
//        iperfThread = new Thread(() -> {
//                Looper.prepare();
//                Looper.loop();
//        });
//        System.out.println("Current thread started is " + iperfThread.getName());
//        iperfThread.start();
        Log.i("IPERF CLIENT", "Running iperf client testConfig exec");
        runIperfTest(testConfig, mCallback);
        Log.i("IPERF CLIENT", String.valueOf(stopTesting));
    }

    public void exec(String serverIp, String serverPort, boolean isDownMode) {
        throw new UnsupportedOperationException("Have not implemented simple exec configuration");
    }

    public void cancelTest() {
//        System.out.println("stop " + Thread.currentThread().getName());
//        cancelTest();
        System.out.println("cancel test");
        stopIperfTest();
//        iperfThread.interrupt();
//        if (iperfThread.getState() == Thread.State.RUNNABLE) {
//
//        }
    }
}

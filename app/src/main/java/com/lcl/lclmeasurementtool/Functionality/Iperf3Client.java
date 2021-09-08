package com.lcl.lclmeasurementtool.Functionality;

import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executor;

public class Iperf3Client {
    private static final String TAG = "Iperf3Client";

    static {
        System.loadLibrary("lcl_measurement_tool_native");
    }

    private boolean stopTesting;
    Thread iperfThread;

    public Iperf3Client() {
        this.stopTesting = false;
    }

    ///////////////////// NATIVE FUNCTION //////////////////////////

    private native void runIperfTest(Iperf3Config testConfig, Iperf3Callback callback, String cacheDir);

    private native void stopIperfTest();

    ////////////////////// JAVA INVOCATION ////////////////////////

    public void exec(Iperf3Config testConfig, Iperf3Callback callback, File cacheDir) {
//        iperfThread = new Thread(() -> {
//                Looper.prepare();
//                Looper.loop();
//        });
//        System.out.println("Current thread started is " + iperfThread.getName());
//        iperfThread.start();
        Log.i(TAG, "Running iperf client exec");
        Log.d(TAG, "testConfig: " + testConfig.toString());
        Log.d(TAG, "callback: " + callback.toString());

        String cacheTemplate = cacheDir.toString() + "/iperf3.XXXXXX";
        runIperfTest(testConfig, callback, cacheTemplate);
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

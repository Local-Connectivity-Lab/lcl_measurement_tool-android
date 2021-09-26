package com.lcl.lclmeasurementtool.Functionality;

import android.util.Log;
import java.io.File;
import java.lang.Thread;

/**
 * A wrapper narrowly translating the c-language iperf API to/from java.
 */
public class Iperf3Client {
    private static final String TAG = "Iperf3Client";

    static {
        System.loadLibrary("lcl_measurement_tool_native");
    }

    public Iperf3Client() {};

    ///////////////////// NATIVE FUNCTIONS //////////////////////////

    private native int runIperfTest(Iperf3Config testConfig, Iperf3Callback callback, String cacheDir);
    private native void stopIperfTest();

    ////////////////////// JAVA INVOCATION ////////////////////////

    public void exec(Iperf3Config testConfig, Iperf3Callback callback, File cacheDir) {
        Log.i(TAG, "Running iperf client exec");
        Log.d(TAG, "testConfig: " + testConfig.toString());
        Log.d(TAG, "callback: " + callback.toString());

        String cacheTemplate = cacheDir.toString() + "/iperf3.XXXXXX";

        // Translate from the "c-style" error return codes to java-style exceptions so calling code
        // can operate cleanly.
        if (runIperfTest(testConfig, callback, cacheTemplate) != 0) {
            // TODO(matt9j) Propagate the error cause in the exception
            throw new RuntimeException("Iperf test failed to run");
        }
    }

    public void cancelTest() {
        Log.v(TAG, "Iperf cancel in thread" + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
        stopIperfTest();
    }
}

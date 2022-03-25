package com.lcl.lclmeasurementtool.Functionality;

import android.util.Log;
import java.io.File;
import java.lang.Thread;

/**
 * A wrapper narrowly translating the c-language iperf API to/from java.
 */
public class Iperf3Client {

    // debugging tag
    private static final String TAG = "Iperf3Client";

    // load the iperf libc
    static {
        System.loadLibrary("lcl_measurement_tool_native");
    }

    public Iperf3Client() {};

    ///////////////////// NATIVE FUNCTIONS //////////////////////////

    // native function for running the test
    private native int runIperfTest(Iperf3Config testConfig, Iperf3Callback callback, String cacheDir);

    // native function for stopping the iperf test
    private native void stopIperfTest();

    ////////////////////// JAVA INVOCATION ////////////////////////

    /**
     * Run the iperf test following the configuration and callback protocol
     * @param testConfig  the test configuration
     * @param callback    the callback functions
     * @param cacheDir    the cache directory
     * @see Iperf3Config
     * @see Iperf3Callback
     */
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

    /**
     * Cancel the running iperf test
     */
    public void cancelTest() {
        Log.v(TAG, "Iperf cancel in thread" + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
        stopIperfTest();
    }
}

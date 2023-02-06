package com.lcl.lclmeasurementtool.features.iperf;

import android.util.Log;

import com.lcl.lclmeasurementtool.Functionality.Iperf3Callback;
import com.lcl.lclmeasurementtool.Functionality.Iperf3Config;

import java.io.File;

public class IperfClient {
    // debugging tag
    private static final String TAG = "IperfClient";

    // load the iperf libc
    static {
        System.loadLibrary("lcl_measurement_tool_native");
    }

    public IperfClient() {}

    ///////////////////// NATIVE FUNCTIONS //////////////////////////

    // native function for running the test
    private native int runIperfTest(IperfConfig testConfig, IperfCallback callback, String cacheDir);

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
    public void exec(IperfConfig testConfig, IperfCallback callback, File cacheDir) {
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
        Log.d(TAG, "Iperf Test with config" + testConfig.toString() + " is finished");
    }

    /**
     * Cancel the running iperf test
     */
    public void cancelTest() {
        Log.v(TAG, "Iperf cancel in thread" + Thread.currentThread().getName() + ":" + Thread.currentThread().getState());
        stopIperfTest();
    }
}

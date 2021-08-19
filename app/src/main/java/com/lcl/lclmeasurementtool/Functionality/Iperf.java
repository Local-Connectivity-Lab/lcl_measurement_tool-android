package com.lcl.lclmeasurementtool.Functionality;
import android.util.Log;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.net.InetAddress;


/**
 * Iperf a functionality module that is able to
 * test the upload and download speed from the current device.
 */
public class Iperf {
    private String serverIPAddress;
    private double numSecondsForTest;
    private boolean stopped;
    private String TAG = "IPERF";

    public Iperf() {

    }
    public void setServerIPAddress(String s) {
        serverIPAddress = s;

    }
    public void setNumSecondsForTest(int numSecondsForTest) {
        this.numSecondsForTest = numSecondsForTest;
    }
    public void stop() {
        this.stopped = true;

    }
    public void start (@NonNull IperfListener listener, String iperfPath) {
        Log.i(TAG, "Entered start");
        new Thread(() -> {
            Log.i(TAG, "entering new thread");

            if (serverIPAddress == null) {
                listener.onError(new IllegalArgumentException("ip address should not be null"));
                return;
            }
            if (numSecondsForTest == 0) {
                listener.onError(new IllegalArgumentException("time should not be 0"));
            }
            stopped = false;
            IperfStats stats = new IperfStats();
            try {
                listener.onStart();
                stats = IperfUtils.launch(iperfPath, serverIPAddress,numSecondsForTest,stats.getBandwidth());
            }
            catch (IOException | InterruptedException ex) {
                listener.onError(ex);
                return;
            }

            if (stopped) return;

            if (stats.hasError()) {
                listener.onError(new RuntimeException(String.valueOf(stats.getError())));
                return;
            }
           listener.onFinished(stats);
        }).start();

    }

}

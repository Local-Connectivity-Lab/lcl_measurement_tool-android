package com.lcl.lclmeasurementtool.Functionality;

import androidx.annotation.NonNull;
import java.io.IOException;

/**
 * class declaration that encapsulates the Ping functionality
 */

public class Ping {

    private String address;
    private int times;
    private int timeout;
    private boolean stopped;

    public Ping() {
        // probably do something
        // we can also use InetAddress to validate IP address - option
    }


    public Ping setAddress(@NonNull String address) {
        this.address = address;
        return this;
    }

    // return Ping object - Builder
    public Ping setTimes(int times) {
        if (times < 0) {
            throw new IllegalArgumentException();
        }

        this.times = times;
        return this;
    }

    public Ping setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        this.timeout = timeout;
        return this;
    }

    // TODO: find a way to cancel the test
    public void stop() { }

    public void start(@NonNull PingListener listener) {
        if (address == null) {
            listener.onError("address should not be null");
            return;
        }

        PingStats stats;
        try {
            listener.onStart();
            stats = PingUtils.ping(address, times, timeout);
        } catch (IOException | InterruptedException ex) {
            listener.onError(ex.getMessage());
            return;
        }

        if (stats.hasError()) {
            listener.onError(stats.getError().toString());
            return;
        }

        listener.onFinished(stats);
    }
}

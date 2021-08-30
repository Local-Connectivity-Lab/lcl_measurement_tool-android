package com.lcl.lclmeasurementtool.Functionality;

import android.util.Log;

import androidx.navigation.NavBackStackEntry;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.time.Clock;

/**
 * class declaration that encapsulates the Ping functionality
 */
import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;

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

    public void stop() {
        this.stopped = true;
    }

    public void start(@NonNull PingListener listener) {
        new Thread(() -> {
            if (address == null) {
                listener.onError("address should not be null");
                return;
            }

            stopped = false;
            PingStats stats;
            try {
                listener.onStart();
                stats = PingUtils.ping(address, times, timeout);
            } catch (IOException | InterruptedException ex) {
                listener.onError(ex.getMessage());
                return;
            }

            if (stopped) return;

            if (stats.hasError()) {
                listener.onError(stats.getError().toString());
                return;
            }

            listener.onFinished(stats);

        }).start();
    }
}

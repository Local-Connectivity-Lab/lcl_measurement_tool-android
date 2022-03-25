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

    /**
     * Set the address of the ping test
     * @param address  the address to test against
     * @return a ping object holding the address
     */
    public Ping setAddress(@NonNull String address) {
        this.address = address;
        return this;
    }

    /**
     * Set the times of the ping test
     * @param times  the number of times to run the ping tes
     * @throws IllegalArgumentException if the times if less than 0
     * @return a ping object holding the times
     */
    public Ping setTimes(int times) {
        if (times < 0) {
            throw new IllegalArgumentException();
        }

        this.times = times;
        return this;
    }

    /**
     * Set the timeout of the ping test
     * @param timeout  the timeout limit for the ping tes
     * @throws IllegalArgumentException if the timeout if less than 0
     * @return a ping object holding the timeout
     */
    public Ping setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        this.timeout = timeout;
        return this;
    }

    // TODO: find a way to cancel the test?
    public void stop() { }

    /**
     * Start the ping test
     * @param listener listner listening to ping test status
     */
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

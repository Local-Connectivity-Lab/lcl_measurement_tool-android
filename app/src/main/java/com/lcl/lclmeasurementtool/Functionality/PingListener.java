package com.lcl.lclmeasurementtool.Functionality;

/**
 * A listener listening to the ping test status
 */
public interface PingListener {
    /**
     * Callback when an error occur
     * @param ex the error output
     */
    void onError(String ex);

    /**
     * Callback when ping test starts
     */
    void onStart();

    /**
     * Callback when ping test finishes
     * @param stats  the stats from ping test
     * @see PingStats
     */
    void onFinished(PingStats stats);
}

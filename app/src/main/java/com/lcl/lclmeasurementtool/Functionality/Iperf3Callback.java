package com.lcl.lclmeasurementtool.Functionality;

/**
 * Callback interface for handling iperf tests
 */
public interface Iperf3Callback {

    /**
     * callback during the iperf test
     * @param timeStart    the time when the iperf test interval starts
     * @param timeEnd      the time when the iperf test interval ends
     * @param sendBytes    the number of bytes sent
     * @param bandWidth    the bandwidth
     * @param isDown       is downstream enabled
     */
    void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    /**
     * callback when the iperf test ends
     * @param timeStart   the time when the iperf test starts
     * @param timeEnd     the time when the iperf test ends
     * @param sendBytes   the number of bytes sent
     * @param bandWidth   the bandwidth
     * @param isDown      is downstream enabled
     */
    void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    /**
     * callback when an error occurs during the test
     * @param errMsg  the error message associated with the error
     */
    void onError(String errMsg);
}

package com.lcl.lclmeasurementtool.features.iperf

interface IperfCallback {
    /**
     * callback during the iperf test
     * @param timeStart    the time when the iperf test interval starts
     * @param timeEnd      the time when the iperf test interval ends
     * @param sendBytes    the number of bytes sent
     * @param bandWidth    the bandwidth
     * @param isDown       is downstream enabled
     */
    fun onInterval(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    )

    /**
     * callback when the iperf test ends
     * @param timeStart   the time when the iperf test starts
     * @param timeEnd     the time when the iperf test ends
     * @param sendBytes   the number of bytes sent
     * @param bandWidth   the bandwidth
     * @param isDown      is downstream enabled
     */
    fun onResult(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    )

    /**
     * callback when an error occurs during the test
     * @param errMsg  the error message associated with the error
     */
    fun onError(errMsg: String)
}
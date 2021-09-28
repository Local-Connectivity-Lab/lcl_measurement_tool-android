package com.lcl.lclmeasurementtool.Functionality;

public interface Iperf3Callback {
    void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onError(String errMsg);
}

package com.lcl.lclmeasurementtool.Functionality;

public interface Iperf3Callback {

    void onConnecting(String destHost, int destPort);

    void onConnected(String localAddr, int localPort, String destAddr, int destPort);

    void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onError(String errMsg);
}

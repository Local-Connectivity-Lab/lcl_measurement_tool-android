package com.lcl.lclmeasurementtool.Functionality;

public interface PingListener {
    void onError(String ex);
    void onStart();
    void onFinished(PingStats stats);
}

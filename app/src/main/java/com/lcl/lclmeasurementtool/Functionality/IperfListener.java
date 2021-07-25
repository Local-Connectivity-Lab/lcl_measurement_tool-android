package com.lcl.lclmeasurementtool.Functionality;

public interface IperfListener {
    void onError(Exception e);
    void onStart();
    void onFinished(IperfStats stats);

}

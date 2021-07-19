package com.lcl.lclmeasurementtool.Managers;

public interface NetworkChangeListener {
    void onAvailable();
    void onUnavailable();
    void onLost();
    void onCellularNetworkChanged(boolean isConnected);
}

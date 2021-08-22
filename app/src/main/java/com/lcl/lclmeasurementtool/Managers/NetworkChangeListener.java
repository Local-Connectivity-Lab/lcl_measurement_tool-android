package com.lcl.lclmeasurementtool.Managers;

/**
 * An interface that listens to the changes in the network status.
 */
public interface NetworkChangeListener {

    /**
     * callback function when the cellular network becomes available.
     */
    void onAvailable();

    /**
     * callback function when the cellular network becomes unavailable.
     */
    void onUnavailable();

    /**
     * callback function when the cellular network gets lost.
     */
    void onLost();

    /**
     * callback function when the cellular network status gets changed.
     *
     * @param isConnected boolean parameter indicating whether there is cellular connection.
     */
    void onCellularNetworkChanged(boolean isConnected);
}

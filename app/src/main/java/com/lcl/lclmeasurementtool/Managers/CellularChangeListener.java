package com.lcl.lclmeasurementtool.Managers;

import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

/**
 * Listen to the cellular network changes from the device.
 */
public interface CellularChangeListener {

    /**
     * Callback function when the cellular network changes.
     * @param level  the Signal Strength level.
     * @param dBm    the signal strength in dBm.
     */
    void onChange(SignalStrengthLevel level, int dBm);
}

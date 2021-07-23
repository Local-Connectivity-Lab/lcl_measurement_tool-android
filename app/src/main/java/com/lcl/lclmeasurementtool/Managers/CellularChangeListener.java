package com.lcl.lclmeasurementtool.Managers;

import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

public interface CellularChangeListener {
    void onChange(SignalStrengthLevel level, int dBm);
}

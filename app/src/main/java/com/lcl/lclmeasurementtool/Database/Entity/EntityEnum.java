package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.lifecycle.ViewModel;

public enum EntityEnum {
    CONNECTIVITY,
    SIGNALSTRENGTH;

    public String getFileName() {
        switch (this) {
            case CONNECTIVITY:
                return "connectivity";
            case SIGNALSTRENGTH:
                return "signal_strength";
            default: return "unknown_data";
        }
    }

    public String[] getHeader() {
        switch (this) {
            case CONNECTIVITY:
                return Connectivity.getHeader();
            case SIGNALSTRENGTH:
                return SignalStrength.getHeader();
            default: return new String[]{};
        }
    }
}

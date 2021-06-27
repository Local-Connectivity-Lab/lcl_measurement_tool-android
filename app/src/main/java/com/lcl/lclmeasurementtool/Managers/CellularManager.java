package com.lcl.lclmeasurementtool.Managers;
import android.content.Context;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

/**
 * CellularManager monitors changes in device's signal strength and
 * report changes(callback) to front-end UI
 */

public class CellularManager {

    private TelephonyManager telephonyManager;

    public CellularManager(Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public SignalStrength getSignalStrength() {
        return this.telephonyManager.getSignalStrength();
    }

    public SignalStrengthLevel getSignalStrengthLevel() {
        int level = getSignalStrength().getLevel();
        return SignalStrengthLevel.init(level);
    }
}

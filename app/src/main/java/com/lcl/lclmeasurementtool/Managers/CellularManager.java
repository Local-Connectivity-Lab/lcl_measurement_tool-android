package com.lcl.lclmeasurementtool.Managers;
import android.content.Context;
import android.os.Looper;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

import java.util.List;

/**
 * CellularManager monitors changes in device's signal strength and
 * report changes(callback) to front-end UI
 * @see <a href="https://developer.android.com/reference/android/telephony/CellSignalStrengthLte">CellSignalStrengthLte</a>
 */
public class CellularManager {

    // LOG TAG constant
    static final String TAG = "CELLULAR_MANAGER_TAG";

    private static CellularManager cellularManager = null;

    // the telephony manager that manages all access related to cellular information.
    private final TelephonyManager telephonyManager;

    // the signal strength object that stores the information
    // retrieved from the system by the time the object is accessed.
    private final SignalStrength signalStrength;

    // the LTE signal strength report that consists of all cellular signal strength information.
    private final CellSignalStrength report;

    // the flag that controls when to stop listening to signal strength change.
    private boolean stopListening;

    /**
     * Construct a new CellularManager object based on current context.
     * @param context the context of the application.
     */
    private CellularManager(@NonNull Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.signalStrength = this.telephonyManager.getSignalStrength();
        if (this.signalStrength.getCellSignalStrengths().size() > 0) {
            this.report = this.signalStrength.getCellSignalStrengths().get(0);
        } else {
            this.report = null;
        }
    }

    /**
     * Retrieve the cellular manager object from current context.
     * @return a cellular manager
     */
    public static CellularManager getManager(@NonNull Context context) {
        if (cellularManager == null) {
            cellularManager = new CellularManager(context);
        }
        return cellularManager;
    }

    /**
     * Retrieve the signal strength object from current context.
     * @return a signal strength object.
     */
    public SignalStrength getSignalStrength() {
        return this.signalStrength;
    }

    /**
     * Retrieve the signalStrengthLevel Enum from current context.
     * @return a corresponding signal strength level from the current context.
     */
    public SignalStrengthLevel getSignalStrengthLevel() {
        if (this.report != null) {
            int level = this.report.getLevel();
            return SignalStrengthLevel.init(level);
        }

        return SignalStrengthLevel.POOR;
    }

    /**
     * Retrieve the CellSignalStrength report.
     * @return a CellSignalStrength object that
     *         contains all information related to cellular signal strength.
     *         report might be null if no cellular connection.
     */
    public CellSignalStrength getCellSignalStrength() {
        return this.report;
    }

    /**
     * Retrieve the signal Strength in dBm.
     * @return an integer of signal strength in dBm.
     *         If no cellular connection, 0.
     */
    public int getDBM() {
        if (this.report != null) {
            return this.report.getDbm() == Integer.MAX_VALUE ? 0 : this.report.getDbm();
        }

        return 0;
    }

    /**
     * Return the state of sim card in the phone
     * @return true if the sim card is absent; otherwise false;
     */
    public boolean isSimCardAbsence() {
        if (this.telephonyManager != null) {
            return this.telephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT;
        }

        return true;
    }

    /**
     * Start listen to signal strength change and display onto the corresponding TextView.
     *
     */
    public void listenToSignalStrengthChange(CellularChangeListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopListening = false;
                Looper.prepare();

                telephonyManager.listen(new PhoneStateListener() {
                    @Override
                    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                        super.onSignalStrengthsChanged(signalStrength);
                        List<CellSignalStrength> reports = signalStrength.
                                                                getCellSignalStrengths();

                        int dBm;
                        SignalStrengthLevel level;
                        if (reports.size() > 0) {
                            CellSignalStrength report = reports.get(0);
                            level = SignalStrengthLevel.init(report.getLevel());
                            dBm = report.getDbm();
                        } else {
                            level = SignalStrengthLevel.POOR;
                            dBm = level.getLevelCode();
                        }

                        if (dBm == Integer.MAX_VALUE) dBm = 0;
                        listener.onChange(level, dBm);

                        if (stopListening) {
                            Looper.myLooper().quit();
                        }
                    }
                }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                Looper.loop();
            }
        }).start();
    }

    /**
     * Stop listening the changes on signal strength.
     */
    public void stopListening() {
        this.stopListening = true;
    }
}

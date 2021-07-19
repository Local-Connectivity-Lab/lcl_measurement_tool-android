package com.lcl.lclmeasurementtool.Managers;
import android.content.Context;
import android.os.Looper;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    private final CellSignalStrengthLte report;

    // the flag that controls when to stop listening to signal strength change.
    private boolean stopListening;

    /**
     * Construct a new CellularManager object based on current context.
     * @param context the context of the application.
     */
    private CellularManager(@NonNull Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.signalStrength = this.telephonyManager.getSignalStrength();
        if (this.signalStrength.getCellSignalStrengths(CellSignalStrengthLte.class).size() > 0) {
            this.report = this.signalStrength.getCellSignalStrengths(CellSignalStrengthLte.class).get(0);
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
        if (report != null) {
            int level = report.getLevel();
            return SignalStrengthLevel.init(level);
        }

        return SignalStrengthLevel.NONE;
    }

    /**
     * Retrieve the CellSignalStrength report.
     * @return a CellSignalStrength object that
     *         contains all information related to cellular signal strength.
     *         report might be null if no cellular connection.
     */
    public CellSignalStrength getCellSignalStrength() {
        return report;
    }

    /**
     * Retrieve the signal Strength in dBm.
     * @return an integer of signal strength in dBm.
     *         If no cellular connection, 0.
     */
    public int getDBM() {
        return report != null ? report.getDbm() : 0;
    }

    /**
     * Start listen to signal strength change and display onto the corresponding TextView.
     *
     * @param textView the text view that will be used to display the signal strength data.
     */
    public void listenToSignalStrengthChange(TextView textView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopListening = false;
                Looper.prepare();

                telephonyManager.listen(new PhoneStateListener() {
                    @Override
                    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                        super.onSignalStrengthsChanged(signalStrength);
                        List<CellSignalStrengthLte> reports = signalStrength
                                .getCellSignalStrengths(CellSignalStrengthLte.class);

                        String text;
                        if (reports.size() > 0) {
                            CellSignalStrengthLte report = reports.get(0);
                            text = report.getDbm() + " " + report.getLevel();
                        } else {
                            text = SignalStrengthLevel.NONE.getName();
                        }
                        textView.setText(text);

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

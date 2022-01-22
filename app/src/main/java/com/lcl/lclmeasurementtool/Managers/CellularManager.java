package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.BuildConfig;
import com.yanzhenjie.permission.runtime.Permission;

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

    private Context context;

    /**
     * Construct a new CellularManager object based on current context.
     * @param context the context of the application.
     */
    private CellularManager(@NonNull Context context) {
        this.context = context;
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public String getCellID() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AndPermission.with(this.context).runtime().permission(Permission.ACCESS_FINE_LOCATION).onDenied(data -> {
                MessageDialog.build()
                        .setTitle(R.string.location_message_title)
                        .setMessage(R.string.permission_denied_explanation)
                        .setOkButton(R.string.settings, (baseDialog, v) -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            this.context.startActivity(intent);
                            return false;
                        }).setOkButton(android.R.string.cancel).show();
            }).start();
        }
        List<CellInfo> infos = this.telephonyManager.getAllCellInfo();
        for (CellInfo info : infos) {
            if (info instanceof CellInfoGsm) {
                CellInfoGsm gsm = (CellInfoGsm) info;
                return String.valueOf(gsm.getCellIdentity().getCid());
            } else if (info instanceof CellInfoLte) {
                CellInfoLte lte = (CellInfoLte) info;
                return String.valueOf(lte.getCellIdentity().getCi());
            } else if (info instanceof CellInfoCdma) {
                CellInfoCdma cdma = (CellInfoCdma) info;
                CellIdentityCdma cellIdentity = cdma.getCellIdentity();

                // based on Android's Implementation
                return String.format("%04x%04x%04x", cellIdentity.getSystemId(), cellIdentity.getNetworkId(), cellIdentity.getBasestationId());
            } else if (info instanceof CellInfoWcdma) {
                CellInfoWcdma wcdma = (CellInfoWcdma) info;
                return String.valueOf(wcdma.getCellIdentity().getCid());
            } else if (info instanceof CellInfoTdscdma) {
                CellInfoTdscdma tdscdma = (CellInfoTdscdma) info;
                return String.valueOf(tdscdma.getCellIdentity().getCid());
            } else {
                break;
            }
        }

        return "unknown";
    }

//    /**
//     * Read the IMEI code of the sim card
//     * @return the IMEI code in string; null if CellularManager failed to initialize;
//     */
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public String getIMEI() {
//        if (this.telephonyManager != null) {
//            return this.telephonyManager.getImei();
//        }
//
//        return null;
//    }

//    /**
//     * Read the Sim card ID code of the sim card
//     * @return the Siim card ID code in string; null if CellularManager failed to initialize;
//     */
//    public String getSIMCardID() {
//        if (this.telephonyManager != null) {
//            return this.telephonyManager.getSimSerialNumber();
//        }
//
//        return null;
//    }
//
//    public String getPhoneNumber() {
//        if (this.telephonyManager != null) {
//            return this.telephonyManager.getLine1Number();
//        }
//
//        return null;
//    }

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
                            Looper.myLooper().quitSafely();
                        }
                    }
                }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                Looper.loop();
            }
        }).start();
    }

//    public void listenToSimCardState() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                telephonyManager.listen(new PhoneStateListener() {
//                    @Override
//                    public void onServiceStateChanged(ServiceState serviceState) {
//                        super.onServiceStateChanged(serviceState);
//                        serviceState.getState()
//                    }
//                });
//            }
//        });
//    }

    /**
     * Stop listening the changes on signal strength.
     */
    public void stopListening() {
        this.stopListening = true;
    }
}

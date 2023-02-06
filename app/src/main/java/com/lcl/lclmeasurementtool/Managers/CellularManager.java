package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.internal.platform.AndroidPlatform;

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
    private SignalStrength signalStrength = null;

    // the LTE signal strength report that consists of all cellular signal strength information.
    private CellSignalStrength report = null;

    // the flag that controls when to stop listening to signal strength change.
    private boolean stopListening;

    WeakReference<Context> context;

    private boolean useDeprecatedAPIs() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    /**
     * Construct a new CellularManager object based on current context.
     * @param context the context of the application.
     */
    private CellularManager(@NonNull WeakReference<Context> context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.get().getSystemService(Context.TELEPHONY_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.signalStrength = this.telephonyManager.getSignalStrength();
            if (this.signalStrength.getCellSignalStrengths().size() > 0) {
                this.report = this.signalStrength.getCellSignalStrengths().get(0);
            } else {
                this.report = null;
            }
        } else {
//            if (!AndPermission.hasPermissions(this.context.get(), Permission.ACCESS_FINE_LOCATION)) {
//                AndPermission.with(this.context.get()).runtime().permission(Permission.ACCESS_FINE_LOCATION).start();
//            }

            @SuppressLint("MissingPermission") List<CellInfo> infos = this.telephonyManager.getAllCellInfo();
            for (CellInfo info : infos) {
                if (info instanceof CellInfoGsm) {
                    CellInfoGsm gsm = (CellInfoGsm) info;
                    this.report = gsm.getCellSignalStrength();
                    break;
                } else if (info instanceof CellInfoLte) {
                    CellInfoLte lte = (CellInfoLte) info;
                    this.report = lte.getCellSignalStrength();
                    break;
                } else if (info instanceof CellInfoCdma) {
                    CellInfoCdma cdma = (CellInfoCdma) info;
                    this.report = cdma.getCellSignalStrength();
                    break;
                } else if (info instanceof CellInfoWcdma) {
                    CellInfoWcdma wcdma = (CellInfoWcdma) info;
                    this.report = wcdma.getCellSignalStrength();
                    break;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Retrieve the cellular manager object from current context.
     * @return a cellular manager
     */
    public static CellularManager getManager(@NonNull Context context) {
        if (cellularManager == null) {
            cellularManager = new CellularManager(new WeakReference<>(context));
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

    public String getCellID() {
//        if (!AndPermission.hasPermissions(this.context.get(), Permission.ACCESS_FINE_LOCATION)) {
//            AndPermission.with(this.context.get()).runtime().permission(Permission.ACCESS_FINE_LOCATION).onDenied(data -> MessageDialog.build()
//                    .setTitle(R.string.location_message_title)
//                    .setMessage(R.string.permission_denied_explanation)
//                    .setOkButton(R.string.settings, (baseDialog, v) -> {
//                        // Build intent that displays the App settings screen.
//                        Intent intent = new Intent();
//                        intent.setAction(
//                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package",
//                                BuildConfig.APPLICATION_ID, null);
//                        intent.setData(uri);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        this.context.get().startActivity(intent);
//                        return false;
//                    }).setOkButton(android.R.string.cancel).show()).start();
//        }
        @SuppressLint("MissingPermission") List<CellInfo> infos = this.telephonyManager.getAllCellInfo();
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
            } else {
                break;
            }
        }

        return "unknown";
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public String getCarrier() {
//        if (ActivityCompat.checkSelfPermission(this.context.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            AndPermission.with(this.context.get()).runtime().permission(Permission.ACCESS_FINE_LOCATION).onDenied(data -> MessageDialog.build()
//                    .setTitle(R.string.location_message_title)
//                    .setMessage(R.string.permission_denied_explanation)
//                    .setOkButton(R.string.settings, (baseDialog, v) -> {
//                        // Build intent that displays the App settings screen.
//                        Intent intent = new Intent();
//                        intent.setAction(
//                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package",
//                                BuildConfig.APPLICATION_ID, null);
//                        intent.setData(uri);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        this.context.get().startActivity(intent);
//                        return false;
//                    }).setOkButton(android.R.string.cancel).show()).start();
//        }

        @SuppressLint("MissingPermission") List<CellInfo> infos = this.telephonyManager.getAllCellInfo();
        for (CellInfo info : infos) {
            if (info instanceof CellInfoGsm) {
                CellInfoGsm gsm = (CellInfoGsm) info;
                return (String) gsm.getCellIdentity().getOperatorAlphaLong();
            } else if (info instanceof CellInfoLte) {
                CellInfoLte lte = (CellInfoLte) info;
                return (String) lte.getCellIdentity().getOperatorAlphaLong();
            } else if (info instanceof CellInfoCdma) {
                CellInfoCdma cdma = (CellInfoCdma) info;
                CellIdentityCdma cellIdentity = cdma.getCellIdentity();
                return (String) cdma.getCellIdentity().getOperatorAlphaLong();
            } else if (info instanceof CellInfoWcdma) {
                CellInfoWcdma wcdma = (CellInfoWcdma) info;
                return (String) wcdma.getCellIdentity().getOperatorAlphaLong();
            } else if (info instanceof CellInfoTdscdma) {
                CellInfoTdscdma tdscdma = (CellInfoTdscdma) info;
                return (String) tdscdma.getCellIdentity().getOperatorAlphaLong();
            } else {
                break;
            }
        }

        return "unknown";
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
                        List<CellSignalStrength> reports = null;
                        int dBm;
                        SignalStrengthLevel level;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            reports = signalStrength.getCellSignalStrengths(CellSignalStrength.class);
                            if (!reports.isEmpty()) {
                                CellSignalStrength report = reports.get(0);
                                Log.i(TAG, "report level code is:" + report.getLevel());
                                level = SignalStrengthLevel.init(report.getLevel());
                                dBm = report.getDbm();
                            } else {
                                level = SignalStrengthLevel.POOR;
                                dBm = report.getDbm();
                            }
                        } else {
                            dBm = signalStrength.getGsmSignalStrength();
                            level = SignalStrengthLevel.init(signalStrength.getLevel());
                        }

                        if (dBm == Integer.MAX_VALUE) dBm = 0;
                        listener.onChange(level, dBm);

                        if (stopListening) {
                            Log.i(TAG, "stop listening signal strength change");
                            Looper.myLooper().quitSafely();
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

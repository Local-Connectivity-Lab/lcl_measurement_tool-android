package com.lcl.lclmeasurementtool.Receivers;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lcl.lclmeasurementtool.Managers.KeyStoreManager;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.SimCardConstants;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

public class SimStatesReceiver extends BroadcastReceiver {
    private final static String TAG = "SIM_RECEIVER";
    private final static String ACTION_SIM_STATE = "android.intent.action.SIM_STATE_CHANGED";

    private Context context;
    private Activity activity;
    private KeyStoreManager securityManager;

    public SimStatesReceiver(Activity activity) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, KeyStoreException, CertificateException, IOException {
        this.activity = activity;
        securityManager = KeyStoreManager.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        if (action.equals(ACTION_SIM_STATE)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            int state = telephonyManager.getSimState();
            String extraState = intent.getStringExtra(SimCardConstants.INTENT_KEY_ICC_STATE);
            Log.i(TAG, extraState);
            if (extraState == null) {
                switch (state) {
                    case TelephonyManager.SIM_STATE_READY:
                        try {
                            if (!securityManager.contains()) {
                                Log.i(TAG, "generate new keypair");
                                securityManager.generate();
                            } else {
                                Log.i(TAG, "keypair exists");
                            }
                        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyStoreException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        try {
                            Log.i(TAG, "remove current keypair");
                            securityManager.delete();
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        }
                        showMessage();
                        break;
                }
            } else {
                if (extraState.equals(SimCardConstants.INTENT_VALUE_ICC_LOADED)) {
                    try {
                        if (!securityManager.contains()) {
                            Log.i(TAG, "generate new keypair");
                            securityManager.generate();
                        } else {
                            Log.i(TAG, "keypair exists");
                        }
                    } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyStoreException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.i(TAG, "remove current keypair");
                        securityManager.delete();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    }
                    showMessage();
                }
            }
        }
    }

    private void showMessage() {
        UIUtils.showDialog(this.context,
                R.string.sim_missing,
                R.string.sim_missing_message,
                android.R.string.ok,
                (dialog, which) -> {
                    this.activity.finishAndRemoveTask();
                    System.exit(0);
                },
                -1,
                null
        );
    }
}

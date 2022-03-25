package com.lcl.lclmeasurementtool.Receivers;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.lcl.lclmeasurementtool.Constants.SimCardConstants;
import com.lcl.lclmeasurementtool.R;

/**
 * A receiver listening to the simcard state
 */
public class SimStatesReceiver extends BroadcastReceiver {

    // debugging tag
    private final static String TAG = "SIM_RECEIVER";

    // the intent to listen to
    private final static String ACTION_SIM_STATE = "android.intent.action.SIM_STATE_CHANGED";

    private Context context;
    private final Activity activity;


    @RequiresApi(api = Build.VERSION_CODES.P)
    public SimStatesReceiver(Activity activity) {
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        if (action.equals(ACTION_SIM_STATE)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            int state = telephonyManager.getSimState();
            String extraState = intent.getStringExtra(SimCardConstants.INTENT_KEY_ICC_STATE);
            Log.i(TAG, extraState);
            if (extraState == null && state != TelephonyManager.SIM_STATE_READY) {
                Log.i(TAG, "remove current keypair");
                removeCredentials();
                showMessage();
            } else {
                // do nothing
                if (extraState.equals(SimCardConstants.INTENT_VALUE_ICC_LOADED) || extraState.equals(SimCardConstants.INTENT_VALUE_ICC_IMSI) || extraState.equals(SimCardConstants.INTENT_VALUE_ICC_READY)) {
                    // currently nothing to do
                } else {
                    Log.i(TAG, "remove current keypair");
                    removeCredentials();
                    showMessage();
                }
            }
        }
    }

    // remove credentials when detecting simcard swap
    private void removeCredentials() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // show error message when the sim card is missing
    private void showMessage() {
        MessageDialog.show(R.string.sim_missing, R.string.sim_missing_message, android.R.string.ok).setOkButton((baseDialog, v) -> {
            activity.finishAndRemoveTask();
            System.exit(0);
            return false;
        });
    }
}

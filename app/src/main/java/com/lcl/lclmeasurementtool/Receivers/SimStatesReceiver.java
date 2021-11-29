package com.lcl.lclmeasurementtool.Receivers;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.output.JsonStream;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.lcl.lclmeasurementtool.Managers.KeyStoreManager;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Constants.SimCardConstants;
import com.lcl.lclmeasurementtool.Models.RegistrationMessageModel;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SimStatesReceiver extends BroadcastReceiver {
    private final static String TAG = "SIM_RECEIVER";
    private final static String ACTION_SIM_STATE = "android.intent.action.SIM_STATE_CHANGED";

    private Context context;
    private Activity activity;
    private KeyStoreManager securityManager;

    // TODO(johnnzhou) retrieve imsi from the system
    private String imsi;   // imsi

    @RequiresApi(api = Build.VERSION_CODES.P)
    public SimStatesReceiver(Activity activity, String imsi) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, KeyStoreException, CertificateException, IOException {
        this.activity = activity;
        this.imsi = imsi;
        securityManager = KeyStoreManager.getInstance();
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
            if (extraState == null) {
                switch (state) {
                    case TelephonyManager.SIM_STATE_READY:
                        try {
                            if (!securityManager.contains()) {
                                Log.i(TAG, "generate new keypair");
                                securityManager.generate();
                                validateUser();
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
                             validateUser();
                         } else {
                             Log.i(TAG, "keypair exists");
                         }
                    } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyStoreException e) {
                        e.printStackTrace();
                    }
                } else if (extraState.equals(SimCardConstants.INTENT_VALUE_ICC_IMSI) || extraState.equals(SimCardConstants.INTENT_VALUE_ICC_READY)) {
                    // do nothing
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

    private void validateUser() {

        // TODO(sudheesh001) security check
        try {
            byte[] pk = securityManager.getPublicKey();
            byte[][] pi = securityManager.getAttestation();

            RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(pk,SecurityUtils.digest(imsi, SecurityUtils.SHA256), pi);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            byte[] registrationMessage = objectMapper.writeValueAsBytes(registrationMessageModel);

            byte[] sigma = SecurityUtils.sign(registrationMessage, securityManager.getPrivateKey(), SecurityUtils.SHA256ECDSA);
            Map<String, Object> map = new HashMap<>();
            
            map.put("message", registrationMessage);
            map.put("sig_message", sigma);
            String json = JsonStream.serialize(map);

            WaitDialog.show("Validating");

            OkHttpClient httpClient = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://api-dev.seattlecommunitynetwork.org/register")
                    .post(requestBody)
                    .build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    WaitDialog.dismiss();
                    TipDialog.show("Network Connection Lost", WaitDialog.TYPE.ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    WaitDialog.dismiss();
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Invalid user");
                        showMessage();
                    } else {
                        TipDialog.show("Success", WaitDialog.TYPE.SUCCESS);
                    }
                }
            });
        } catch (NoSuchAlgorithmException | IOException | CertificateException | KeyStoreException | UnrecoverableEntryException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        // TODO(sudheesh001) security check
    }

    private void showMessage() {
        MessageDialog.show(R.string.sim_missing, R.string.sim_missing_message, android.R.string.ok).setOkButton((baseDialog, v) -> {
            activity.finishAndRemoveTask();
            System.exit(0);
            return false;
        });
    }
}

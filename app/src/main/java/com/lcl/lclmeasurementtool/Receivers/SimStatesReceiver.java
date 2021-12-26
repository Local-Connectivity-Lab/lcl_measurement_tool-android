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

public class SimStatesReceiver extends BroadcastReceiver {
    private final static String TAG = "SIM_RECEIVER";
    private final static String ACTION_SIM_STATE = "android.intent.action.SIM_STATE_CHANGED";

    private Context context;
    private Activity activity;


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
            if (extraState == null) {
                switch (state) {
                    case TelephonyManager.SIM_STATE_READY:
                        break;
                    default:
                        Log.i(TAG, "remove current keypair");
                        removeCredentials();
                        showMessage();
                        break;
                }
            } else {
                if (extraState.equals(SimCardConstants.INTENT_VALUE_ICC_LOADED)) {
                    // currently nothing to do
                } else if (extraState.equals(SimCardConstants.INTENT_VALUE_ICC_IMSI) || extraState.equals(SimCardConstants.INTENT_VALUE_ICC_READY)) {
                    // do nothing
                } else {
                    Log.i(TAG, "remove current keypair");
                    removeCredentials();
                    showMessage();
                }
            }
        }
    }

//    private void validateUser() {
//
//        // TODO(sudheesh001) security check
//        try {
//            byte[] pk = securityManager.getPublicKey();
//            byte[][] pi = securityManager.getAttestation();
//
//            RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(pk,SecurityUtils.digest(imsi, SecurityUtils.SHA256), pi);
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//            byte[] registrationMessage = objectMapper.writeValueAsBytes(registrationMessageModel);
//
//            byte[] sigma = SecurityUtils.sign(registrationMessage, securityManager.getPrivateKey(), SecurityUtils.SHA256ECDSA);
//            Map<String, Object> map = new HashMap<>();
//
//            map.put("message", registrationMessage);
//            map.put("sig_message", sigma);
//            String json = JsonStream.serialize(map);
//
//            WaitDialog.show("Validating");
//
//            OkHttpClient httpClient = new OkHttpClient();
//            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
//            Request request = new Request.Builder()
//                    .url("https://api-dev.seattlecommunitynetwork.org/register")
//                    .post(requestBody)
//                    .build();
//            httpClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                    WaitDialog.dismiss();
//                    TipDialog.show("Network Connection Lost", WaitDialog.TYPE.ERROR);
//                }
//
//                @Override
//                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                    WaitDialog.dismiss();
//                    if (!response.isSuccessful()) {
//                        Log.e(TAG, "Invalid user");
//                        showMessage();
//                    } else {
//                        TipDialog.show("Success", WaitDialog.TYPE.SUCCESS);
//                    }
//                }
//            });
//        } catch (NoSuchAlgorithmException | IOException | CertificateException | KeyStoreException | UnrecoverableEntryException | InvalidKeyException | SignatureException e) {
//            e.printStackTrace();
//        }
//
//        // TODO(sudheesh001) security check
//    }

//    private void showLogInPage() {
//        DialogX.init(this.context);
//        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Intent data = result.getData();
//                        if (data != null) {
//                            String content = data.getStringExtra(Constant.CODED_CONTENT);
//                            System.out.println("scan result is：" + content);
//                            WaitDialog.show("Validating ...");
//
//                            OkHttpClient client = new OkHttpClient();
//                            Request request = new Request.Builder()
//                                    .url("https://www.google.com/")
//                                    .build();
//                            client.newCall(request).enqueue(new Callback() {
//                                @Override
//                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                                    TipDialog.show("Cannot validate code. Please retry or contact the administrator", WaitDialog.TYPE.ERROR);
//                                }
//
//                                @Override
//                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                                    System.out.println(response.body().string());
//                                    TipDialog.show("Success", WaitDialog.TYPE.SUCCESS);
//                                    activity.runOnUiThread(() -> fullScreenDialog.dismiss());
//                                }
//                            });
//
//                        }
//                    }
//                }
//        );
//    }
//
//    private void askPermission() {
//        AndPermission.with(this.context).runtime().permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE).onDenied(data -> {
//            Uri packageURI = Uri.parse("package:" + this.context.getPackageName());
//            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            this.activity.startActivity(intent);
//        }).start();
//    }
//
//

    private void removeCredentials() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void showMessage() {
        MessageDialog.show(R.string.sim_missing, R.string.sim_missing_message, android.R.string.ok).setOkButton((baseDialog, v) -> {
            activity.finishAndRemoveTask();
            System.exit(0);
            return false;
        });
    }
}

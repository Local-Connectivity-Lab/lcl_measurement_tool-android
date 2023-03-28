package com.lcl.lclmeasurementtool;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.lcl.lclmeasurementtool.Receivers.SimStatesReceiver;
import com.lcl.lclmeasurementtool.Utils.AnalyticsUtils;
import com.lcl.lclmeasurementtool.Utils.ECDSA;
import com.lcl.lclmeasurementtool.Utils.Hex;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.constants.NetworkConstants;
import com.lcl.lclmeasurementtool.databinding.ActivityMainBinding;
import com.lcl.lclmeasurementtool.errors.DecoderException;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN_ACTIVITY";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;
    private Activity activity;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private SimStatesReceiver simStatesReceiver;
    private FullScreenDialog fullScreenDialog;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        navController = Navigation.findNavController(this, R.id.nav_host);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.HomeFragment).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        AppCenter.start(getApplication(), AnalyticsUtils.SK, com.microsoft.appcenter.analytics.Analytics.class, Crashes.class);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains(getString(R.string.device_id))) {
            String device_id = UUID.randomUUID().toString();
            preferences.edit().putString(getString(R.string.device_id), device_id).apply();
        }

//        askPermission();
        if (!preferences.contains("sigma_t") || !preferences.contains("pk_a") || !preferences.contains("sk_t")) {
            Log.e(TAG, "key not in shared preferences");
            showLogInPage();
        }


        simStatesReceiver = new SimStatesReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SIM_STATE_CHANGED);
        this.registerReceiver(simStatesReceiver, filter);
//        activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Intent data = result.getData();
//                        if (data != null) {
//                            String content = data.getStringExtra(Constant.CODED_CONTENT);
//                            QRCodeKeysModel jsonObj;
//                            try {
//                             jsonObj = JsonIterator.deserialize(content, QRCodeKeysModel.class);
//                            } catch (JsonException e) {
//                                TipDialog.show(getString(com.lcl.lclmeasurementtool.R.string.qrcode_invalid_format), WaitDialog.TYPE.ERROR);
//                                Map<String, String> reasons = AnalyticsUtils.formatProperties(e.getMessage(), Arrays.toString(e.getStackTrace()));
//                                Analytics.trackEvent(AnalyticsUtils.QR_CODE_PARSING_FAILED, reasons);
//                                return;
//                            }
//
//                            String sigma_t = jsonObj.getSigma_t();
//                            String sk_t = jsonObj.getSk_t();
//                            String pk_a = jsonObj.getPk_a();
//
//                            WaitDialog.show(getString(R.string.validation));
//                            validate(sigma_t, pk_a, sk_t);
//                        }
//                    }
//                }
//        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: check for sim swap
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(simStatesReceiver);
    }

    // show login page and validate the credentials through QR code scan
    private void showLogInPage() {
        DialogX.init(this);

        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.login) {
            @Override
            public void onBind(FullScreenDialog dialog, View v) {
                fullScreenDialog = dialog;
                dialog.setCancelable(false);

                v.setOnTouchListener((view, event) -> {
                    view.performClick();
                    InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (inputManager.isAcceptingText()) {
                        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                    }
                    return true;
                });


                Button qrScanner = v.findViewById(R.id.qr_scanner);

                qrScanner.setOnClickListener(v1 -> {
//                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//                    ZxingConfig config = new ZxingConfig();
//                    config.setFullScreenScan(false);
//                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
//                    activityResultLauncher.launch(intent);
                });

                Button next = (Button) v.findViewById(R.id.next);
                TextInputEditText userKey = (TextInputEditText) v.findViewById(R.id.user_key);
                TextInputEditText privateKey = (TextInputEditText) v.findViewById(R.id.private_key);
                TextInputEditText sigmaKey = (TextInputEditText) v.findViewById(R.id.sigma_key);
                next.setEnabled( !TextUtils.isEmpty(userKey.getText()) && !TextUtils.isEmpty(privateKey.getText()) && !TextUtils.isEmpty(sigmaKey.getText()) );
                next.setOnClickListener(view -> {
                    if (TextUtils.isEmpty(userKey.getText()) || TextUtils.isEmpty(privateKey.getText()) || TextUtils.isEmpty(sigmaKey.getText())) {
                        showMessageOnFailure();
                    } else {
                        // validate only when all fields have been filled
                        String pk_a = userKey.getText().toString();
                        String sk_t = privateKey.getText().toString();
                        String sigma_t = sigmaKey.getText().toString();
                        validate(sigma_t, pk_a, sk_t);
                    }

                });
            }
        });
    }

    // check for necessary permission
//    private void askPermission() {
//        boolean hasPermissions = AndPermission.hasPermissions(this, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION);
//        if (hasPermissions) return;
//        AndPermission.with(this).runtime().permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION)
//                .onDenied(data -> {
//                    Uri packageURI = Uri.parse("package:" + getPackageName());
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    this.activity.startActivity(intent);
//                }).start();
//    }

    // save credential to SharedPreferences
    private void saveCredentials(String sigma_t, String pk_a, String sk_t) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sigma_t", sigma_t);
        editor.putString("sk_t", sk_t);
        editor.putString("pk_a", pk_a);
        editor.putBoolean("login", true);
        editor.apply();
    }

    // show message when failed and remove all saved keys
    private void showMessageOnFailure() {
        WaitDialog.dismiss();
        TipDialog.show(getString(R.string.validation_failure), WaitDialog.TYPE.ERROR);
        getPreferences(MODE_PRIVATE).edit().clear().apply();
    }

    // validate keys
    private void validate(String sigma_t, String pk_a, String sk_t) {
        byte[] sigma_t_hex;
        byte[] pk_a_hex;
        byte[] sk_t_hex;
        try {
            sigma_t_hex = Hex.decodeHex(sigma_t);
            pk_a_hex = Hex.decodeHex(pk_a);
            sk_t_hex = Hex.decodeHex(sk_t);

            if (!ECDSA.Verify(sk_t_hex,
                    sigma_t_hex,
                    ECDSA.DeserializePublicKey(pk_a_hex)
            )) {
                showMessageOnFailure();
                Map<String, String> reasons = AnalyticsUtils.formatProperties("Verification Failure", null);
                Analytics.trackEvent(AnalyticsUtils.INVALID_KEYS, reasons);
                return;
            }
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | DecoderException | InvalidKeySpecException | NoSuchProviderException e) {
            showMessageOnFailure();
            Map<String, String> reasons = AnalyticsUtils.formatProperties(e.getMessage(), Arrays.toString(e.getStackTrace()));
            Analytics.trackEvent(AnalyticsUtils.INVALID_KEYS, reasons);
            return;
        }

        ECPublicKey pk_t;
        ECPrivateKey ecPrivateKey;
        try {
            ecPrivateKey = ECDSA.DeserializePrivateKey(sk_t_hex);
            pk_t = ECDSA.DerivePublicKey(ecPrivateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException e) {
            showMessageOnFailure();
            return;
        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        byte[] R;
        if (preferences.contains("R")) {
            try {
                R = Hex.decodeHex(preferences.getString("R", ""));
                if (R.length == 0) {
                    showMessageOnFailure();
                    return;
                }
            } catch (DecoderException e) {
                showMessageOnFailure();
                e.printStackTrace();
                return;
            }
        } else {
            SecureRandom secureRandom = new SecureRandom();
            R = new byte[16];
            secureRandom.nextBytes(R);
            String RStringInHex = Hex.encodeHexString(R);
            preferences.edit().putString("R", RStringInHex).apply();
        }

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte[] h_pkr;
        byte[] h_sec;
        byte[] h_concat;
        byte[] sigma_r;
        try {
            byteArray.write(pk_t.getEncoded());
            byteArray.write(R);
            h_pkr = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH);
            preferences.edit().putString("h_pkr", Hex.encodeHexString(h_pkr)).apply();

            byteArray.reset();
            byteArray.write(sk_t_hex);
            byteArray.write(pk_t.getEncoded());
            h_sec = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH);

            byteArray.reset();
            byteArray.write(h_pkr);
            byteArray.write(h_sec);
            h_concat = byteArray.toByteArray();
            sigma_r = ECDSA.Sign(h_concat, ecPrivateKey);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            showMessageOnFailure();
            return;
        }

        // prepare for registration message and then register
//        RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(sigma_r, h_concat, R);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writeValueAsString(registrationMessageModel)
//        String registration = JsonStream.serialize(registrationMessageModel);

//        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = RequestBody.create(registration, JSON);
//
//        Request request = new Request.Builder()
//                .url(NetworkConstants.URL + NetworkConstants.REGISTRATION_ENDPOINT)
//                .post(requestBody)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                showMessageOnFailure();
//                Map<String, String> reasons = AnalyticsUtils.formatProperties(e.getMessage(), Arrays.toString(e.getStackTrace()));
//                Analytics.trackEvent(AnalyticsUtils.REGISTRATION_FAILED, reasons);
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    TipDialog.show(getString(com.lcl.lclmeasurementtool.R.string.validation_success), WaitDialog.TYPE.SUCCESS);
//                    saveCredentials(sigma_t, pk_a, sk_t);
//                    activity.runOnUiThread(() -> fullScreenDialog.dismiss());
//                } else {
//                    Log.e(TAG, response.body().string());
//                    Map<String, String> reasons = AnalyticsUtils.formatProperties("status code", String.valueOf(response.code()), "body", response.body().string());
//                    Analytics.trackEvent(AnalyticsUtils.REGISTRATION_FAILED, reasons);
//                    TipDialog.show(getString(com.lcl.lclmeasurementtool.R.string.registration_failure), WaitDialog.TYPE.ERROR);
//                }
//                response.close();
//            }
//        });

    }


    ////////////////// HELPER FUNCTION ///////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and we
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.i(TAG, "Location permission granted");
            } else {
                // Permission denied.

                // Notify the user via a dialog that they have rejected a core permission for the
                // app, which makes the Activity useless.

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
                            startActivity(intent);
                            return false;
                        }).setOkButton(android.R.string.cancel).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.settings) {
                Navigation.findNavController(this, R.id.nav_host).navigate(R.id.toSettings);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
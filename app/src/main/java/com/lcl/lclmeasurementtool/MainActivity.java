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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.textfield.TextInputEditText;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Models.RegistrationMessageModel;
import com.lcl.lclmeasurementtool.Receivers.SimStatesReceiver;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.databinding.ActivityMainBinding;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

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
    private final static String ACTION_SIM_STATE = "android.intent.action.SIM_STATE_CHANGED";
    private SimStatesReceiver simStatesReceiver;
    private FullScreenDialog fullScreenDialog;


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

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains("PK") && !preferences.contains("SK")) {
            Log.e(TAG, "key not in shared preferences");
            askPermission();
            showLogInPage();
        }

        MeasurementResultDatabase db = MeasurementResultDatabase.getInstance(this);

        simStatesReceiver = new SimStatesReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SIM_STATE_CHANGED);
        this.registerReceiver(simStatesReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(simStatesReceiver);
    }

    private void showLogInPage() {
        DialogX.init(this);
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String content = data.getStringExtra(Constant.CODED_CONTENT);
                            System.out.println("scan result is：" + content);

                            // TODO: write key val to shared preferences
                            content = "{\"sigma_t\": \"00AABBCCDDEEFF\", \"sk_t\": \"FFEE000A0A0B0C0D\",\"pk_a\": \"A0B0C0D0\"}";
                            Any jsonObj = JsonIterator.deserialize(content);
                            String sigma_t = jsonObj.get("sigma_t").toString();
                            String sk_t = jsonObj.get("sk_t").toString();
                            String pk_a = jsonObj.get("pk_a").toString();
                            saveCredentials(sigma_t, pk_a, sk_t);

                            WaitDialog.show("Validating ...");
                            validate(sigma_t, pk_a, sk_t);
                        }
                    }
                }
        );

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


                Button qrScanner = (Button)v.findViewById(R.id.qr_scanner);

                qrScanner.setOnClickListener(v1 -> {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    ZxingConfig config = new ZxingConfig();
                    config.setFullScreenScan(false);
                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                    activityResultLauncher.launch(intent);
                });

                Button next = (Button) v.findViewById(R.id.next);
                next.setOnClickListener(view -> {
                    TextInputEditText userKey = view.findViewById(R.id.user_key);
                    TextInputEditText privateKey = view.findViewById(R.id.private_key);
                    TextInputEditText sigmaKey = view.findViewById(R.id.sigma_key);
                    if (TextUtils.isEmpty(userKey.getText()) || TextUtils.isEmpty(privateKey.getText()) || TextUtils.isEmpty(sigmaKey.getText())) {
                        showMessageOnFailure();
                    } else {
                        String pk_a = userKey.getText().toString();
                        String sk_t = privateKey.getText().toString();
                        String sigma_t = sigmaKey.getText().toString();
                        validate(sigma_t, pk_a, sk_t);
                    }
                    // validate only when all fields have been filled

                });
            }
        });
    }

    private void askPermission() {
        AndPermission.with(this).runtime().permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE).onDenied(data -> {
            Uri packageURI = Uri.parse("package:" + getPackageName());
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.activity.startActivity(intent);
        }).start();
    }

    private void saveCredentials(String sigma_t, String pk_a, String sk_t) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sigma_t", sigma_t);
        editor.putString("sk_t", sk_t);
        editor.putString("pk_a", pk_a);
        editor.apply();
    }

    private void showMessageOnFailure() {
        TipDialog.show("Cannot validate code. Please retry or contact the administrator", WaitDialog.TYPE.ERROR);
    }

    private void validate(String sigma_t, String pk_a, String sk_t) {
        try {
            if (!SecurityUtils.verify(sk_t, sigma_t, pk_a, SecurityUtils.SHA256)) {
                showMessageOnFailure();
                return;
            }
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            showMessageOnFailure();
            return;
        }

        PublicKey pk_t;
        try {
            pk_t = SecurityUtils.genPublicKey(sk_t, SecurityUtils.RSA);
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            showMessageOnFailure();
            return;
        }

        SecureRandom secureRandom = new SecureRandom();
        byte[] R = new byte[16];
        secureRandom.nextBytes(R);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String RStringInHex = Hex.encodeHexString(R);
        preferences.edit().putString("R", RStringInHex).apply();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte[] h_pkr;
        byte[] h_sec;
        byte[] h_concat;
        byte[] sigma_r;
        try {
            byteArray.write(pk_t.getEncoded());
            byteArray.write(R);
            h_pkr = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA256);

            byteArray.reset();
            byteArray.write(Hex.decodeHex(sk_t));
            byteArray.write(pk_t.getEncoded());
            h_sec = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA256);

            byteArray.reset();
            byteArray.write(h_pkr);
            byteArray.write(h_sec);
            h_concat = byteArray.toByteArray();
            sigma_r = SecurityUtils.sign(h_concat,
                    SecurityUtils.genPrivateKey(sk_t, SecurityUtils.RSA),
                    SecurityUtils.SHA256ECDSA);
        } catch (IOException | NoSuchAlgorithmException |
                DecoderException |
                InvalidKeySpecException |
                InvalidKeyException |
                SignatureException e) {
            showMessageOnFailure();
            return;
        }

        RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(sigma_r, h_concat, R);
        String registration = JsonStream.serialize(registrationMessageModel);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(registration, JSON);

        Request request = new Request.Builder()
                .url("https://api-dev.seattlecommunitynetwork.org/register")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showMessageOnFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println(response.body().string());
                TipDialog.show("Success", WaitDialog.TYPE.SUCCESS);
                activity.runOnUiThread(() -> fullScreenDialog.dismiss());
            }
        });
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
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.HomeFragment:
                        Fragment home = new HomeFragment();
                        return NavigationUI.onNavDestinationSelected(item, navController);
//                        show(home);
                    case R.id.SpeedTestFragment:
                        SignalDataFragment fConn = new SignalDataFragment();
                        fConn.type = EntityEnum.CONNECTIVITY;
//                        show(fConn);
                        return NavigationUI.onNavDestinationSelected(item, navController);
                    case R.id.SignalStrengthFragment:
                        SignalDataFragment fSig = new SignalDataFragment();
                        fSig.type = EntityEnum.SIGNALSTRENGTH;
                        return NavigationUI.onNavDestinationSelected(item, navController);
                    default: return false;
                }
            }
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
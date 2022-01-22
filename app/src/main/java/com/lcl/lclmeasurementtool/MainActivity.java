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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.textfield.TextInputEditText;
import com.jsoniter.JsonIterator;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.lcl.lclmeasurementtool.Constants.NetworkConstants;
import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Models.QRCodeKeysModel;
import com.lcl.lclmeasurementtool.Models.RegistrationMessageModel;
import com.lcl.lclmeasurementtool.Receivers.SimStatesReceiver;
import com.lcl.lclmeasurementtool.Utils.DecoderException;
import com.lcl.lclmeasurementtool.Utils.ECDSA;
import com.lcl.lclmeasurementtool.Utils.Hex;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.databinding.ActivityMainBinding;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.BuildConfig;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

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

        // For testing only! Remove TODO:Zhennan
        String sigHex = "304502201d3ea6680d007b751d4e3c1d928a270a1e5ce06cd9b77a46a95542766bb50cb90221008666a33c7e3362a18795d5b96cc36541f8ca79d9190c4341642145d41feb6605";
        String invalidSigHex = "304502201d3ea6680d007b751d4e3c1d928a270a1e5ce06cd9b77a46a95542766bb50cb90221008666a33c7e3362a18795d5b96cc36541f8ca79d9190c4341642145d41feb6606"; // changed last byte
        String messageHex = "308184020100301006072a8648ce3d020106052b8104000a046d306b0201010420798116c5c26ccfd95e4e13fdf4df9e46cf3629223b190da6c891d48e4de5da57a144034200044552ed599a2d855f59286447d687fbd1ed05793025a7994268f29baef5ca1e3432f9b1d48301a85e4bd8ed77e2c6f3e834f947540b144dbc5a71a548c046c9e2";
        String pkHex = "3056301006072a8648ce3d020106052b8104000a03420004da754f3ede85eec8b7dec3fda5dbdc35662f807f29433e2810743c889de15e1f5d4338453fc13c45e856287cc7849554f92aca832c66a094c7f7f231c50afebf";

        try {
            // The messageHex is the SK_t encoded which is signed by the server
            byte[] skBytes = Hex.decodeHex(messageHex);
            // Obtain the PK_A from the PK sent, this is SPKI Encoded since its directly obtained from the server.
            byte[] pkABytes = Hex.decodeHex(pkHex);
            PublicKey pkA = ECDSA.DeserializePublicKey(pkABytes);
            // Convert the Signature to a byte array
            byte[] sigBytes = Hex.decodeHex(sigHex);
            byte[] invalidSigBytes = Hex.decodeHex(invalidSigHex);
            boolean verifySignature = ECDSA.Verify(skBytes, sigBytes, (ECPublicKey) pkA);
            System.out.println("Verifying valid signature :" + verifySignature);
            boolean invalidVerifySignature = ECDSA.Verify(skBytes, invalidSigBytes, (ECPublicKey) pkA);
            System.out.println("Verifying invalid signature :" + invalidVerifySignature);
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        askPermission();
        if (!preferences.contains("sigma_t") || !preferences.contains("pk_a") || !preferences.contains("sk_t")) {
            Log.e(TAG, "key not in shared preferences");
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

                            QRCodeKeysModel jsonObj = JsonIterator.deserialize(content, QRCodeKeysModel.class);
                            String sigma_t = jsonObj.getSigma_t();
                            String sk_t = jsonObj.getSk_t();
                            String pk_a = jsonObj.getPk_a();

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


                Button qrScanner = (Button) v.findViewById(R.id.qr_scanner);

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

    private void askPermission() {
        boolean hasPermissions = AndPermission.hasPermissions(this, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION);
        if (hasPermissions) return;
        AndPermission.with(this).runtime().permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION)
                .onDenied(data -> {
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
        WaitDialog.dismiss();
        TipDialog.show("Cannot validate code. Please retry or contact the administrator", WaitDialog.TYPE.ERROR);
        getPreferences(MODE_PRIVATE).edit().clear().apply();
    }

    private void validate(String sigma_t, String pk_a, String sk_t) {

        // TODO: ECDSA key check
        byte[] sigma_t_hex;
        byte[] pk_a_hex;
        byte[] sk_t_hex;
        try {
            sigma_t_hex = Hex.decodeHex(sigma_t);
            pk_a_hex = Hex.decodeHex(pk_a);
            sk_t_hex = Hex.decodeHex(sk_t);

            // TODO: ECDSA key check: the verify method failed due to public key
            if (!ECDSA.Verify(sk_t_hex,
                    sigma_t_hex,
                    ECDSA.DeserializePublicKey(pk_a_hex)
            )) {
                showMessageOnFailure();
                return;
            }
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | DecoderException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            showMessageOnFailure();
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

        RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(sigma_r, h_concat, R);
        String registration;
        try {
            registration = Hex.encodeHexString(registrationMessageModel.serializeToBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(registration, JSON);

        Request request = new Request.Builder()
                .url(NetworkConstants.URL + NetworkConstants.REGISTRATION_ENDPOINT)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showMessageOnFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    TipDialog.show("Success", WaitDialog.TYPE.SUCCESS);
                    saveCredentials(sigma_t, pk_a, sk_t);
                    activity.runOnUiThread(() -> fullScreenDialog.dismiss());
                } else {
                    TipDialog.show("Registration failed. Please try again", WaitDialog.TYPE.ERROR);
                }
                response.close();
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
                    default:
                        return false;
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
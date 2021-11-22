package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.BuildConfig;
import com.google.android.gms.maps.model.LatLng;
import com.jsoniter.output.JsonStream;
import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;
import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.KeyStoreManager;
import com.lcl.lclmeasurementtool.Receivers.SimStatesReceiver;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lcl.lclmeasurementtool.databinding.ActivityMainBinding;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN_ACTIVITY";
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private SimStatesReceiver simStatesReceiver;


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.HomeFragment).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // set up UUID
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains(getString(R.string.USER_UUID))) {
            String uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.USER_UUID), uuid);
            editor.apply();
        }

//        // set up DB
        MeasurementResultDatabase db = MeasurementResultDatabase.getInstance(this);
        try {
            simStatesReceiver = new SimStatesReceiver(this);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SIM_STATE_CHANGED);
        this.registerReceiver(simStatesReceiver, filter);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simcardID = "1234";
//                telephonyManager.getSimSerialNumber();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
////            ActivityCompat.requestPermissions(this, );
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        String phoneNumber = "5678";
//                telephonyManager.getLine1Number();
        try {
            Log.i(TAG, "generate byte array now ...");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();
            byte[] pk = keyStoreManager.getPublicKey();
            byte[][] pi = keyStoreManager.getAttestation();

            byteArrayOutputStream.write(pk);
            Arrays.stream(pi).iterator().forEachRemaining(bytes -> {
                try {
                    byteArrayOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            byteArrayOutputStream.write(SecurityUtils.digest(simcardID, "SHA-256"));
            byteArrayOutputStream.write(SecurityUtils.digest(phoneNumber, "SHA-256"));
            byte[] registrationMessage = byteArrayOutputStream.toByteArray();

            byte[] sigma = SecurityUtils.sign(registrationMessage, keyStoreManager.getPrivateKey(), "SHA256withRSA");
            Map<String, Object> map = new HashMap<>();
            map.put("sigma", sigma);
            map.put("registrationMessage", registrationMessage);
            String json = JsonStream.serialize(map);

            OkHttpClient httpClient = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://api-dev.seattlecommunitynetwork.org/register")
                    .post(requestBody)
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e(TAG, "Invalid user");
                System.exit(1);
            }


        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(simStatesReceiver);
    }


    ////////////////// HELPER FUNCTION ///////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                UIUtils.showDialog(this,
                        R.string.location_message_title,
                        R.string.permission_denied_explanation,
                        R.string.settings,
                        (dialogInterface, actionID) -> {

                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }, android.R.string.cancel, null);
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
package com.lcl.lclmeasurementtool;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Receivers.SimStatesReceiver;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import com.lcl.lclmeasurementtool.databinding.ActivityMainBinding;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.UUID;

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
        String simcardID = "1234";
        try {
            simStatesReceiver = new SimStatesReceiver(this, simcardID);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SIM_STATE_CHANGED);
        this.registerReceiver(simStatesReceiver, filter);
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

                MessageDialog.build()
                        .setTitle(R.string.location_message_title)
                        .setMessage(R.string.permission_denied_explanation)
                        .setOkButton(R.string.settings, new OnDialogButtonClickListener<MessageDialog>() {
                            @Override
                            public boolean onClick(MessageDialog baseDialog, View v) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                return true;
                            }
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
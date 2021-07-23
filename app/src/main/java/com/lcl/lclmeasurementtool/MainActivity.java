package com.lcl.lclmeasurementtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import java.util.UUID;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private Context context;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;

    private boolean isTestStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains(getString(R.string.USER_UUID))) {
            String uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.USER_UUID), uuid);
            editor.apply();
        }

        mNetworkManager = NetworkManager.getManager(this.getApplicationContext());
        mCellularManager = CellularManager.getManager(this.getApplicationContext());
        mLocationManager = LocationServiceManager.getManager(this.getApplicationContext());

        locationServiceListener = new LocationServiceListener(this.getApplicationContext(), getLifecycle());
        getLifecycle().addObserver(locationServiceListener);
        this.context = this;

        this.isTestStarted = false;




        this.mNetworkManager = new NetworkManager(this);
        this.mCellularManager = CellularManager.getManager(this);

        if (!this.mNetworkManager.isCellularConnected()) {
            updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
        }

        setUpFAB();
        updateFAB(this.mNetworkManager.isCellularConnected());

        this.mNetworkManager.addNetworkChangeListener(new NetworkManager.NetworkChangeListener() {
            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on avaliable");
                updateFAB(true);
                mCellularManager.listenToSignalStrengthChange((level, dBm) ->
                                                                updateSignalStrengthTexts(level, dBm));
            }

            @Override
            public void onLost() {
                mCellularManager.stopListening();
                updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                updateFAB(false);
            }

            @Override
            public void onUnavailable() {
                updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                updateFAB(false);
            }

            @Override
            public void onCellularNetworkChanged(boolean isConnected) {
                if (!isConnected) {
                    updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                    updateFAB(isConnected);
                }
            }
        });
    }

    private void updateSignalStrengthTexts(SignalStrengthLevel level, int dBm) {
        runOnUiThread(() -> {
            TextView signalStrengthValue = findViewById(R.id.SignalStrengthValue);
            TextView signalStrengthStatus = findViewById(R.id.SignalStrengthStatus);
            TextView signalStrengthUnit = findViewById(R.id.SignalStrengthUnit);
            ImageView signalStrengthIndicator = findViewById(R.id.SignalStrengthIndicator);
            signalStrengthValue.setText(String.valueOf(dBm));
            signalStrengthUnit.setText(UnitUtils.SIGNAL_STRENGTH_UNIT);
            signalStrengthStatus.setText(level.getName());
            signalStrengthIndicator.setColorFilter(level.getColor(context));
        });
    }

    private void setUpFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(button -> {
            ((FloatingActionButton) button).setImageResource( this.isTestStarted ? R.drawable.start : R.drawable.stop );
            fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));

            // TODO: init/cancel ping and iperf based in iTestStart

            this.isTestStarted = !isTestStarted;
            Toast.makeText(this, "test starts: " + this.isTestStarted, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFAB(boolean state) {
        runOnUiThread(() -> {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setEnabled(state);
            fab.setImageResource(R.drawable.start);
            fab.setColorFilter(state ? ContextCompat.getColor(this, R.color.purple_500) :
                    ContextCompat.getColor(this, R.color.light_gray));

//             TODO: cancel ping and iperf if started
//            if (isTestStarted) {
                // cancel test
//            }

            this.isTestStarted = false;
        });
    }


    // TODO: update FAB Icon and State when tests are done


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mCellularManager.stopListening();
        this.mNetworkManager.removeAllNetworkChangeListeners();
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

    /**
     * Fetch the last location from the device
     */
    private void getLastLocation() {
        mLocationManager.getLastLocation();
    }

}
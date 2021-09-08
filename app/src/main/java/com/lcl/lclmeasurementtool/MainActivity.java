package com.lcl.lclmeasurementtool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.WorkInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcl.lclmeasurementtool.Functionality.NetworkTestViewModel;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;

import java.util.List;
import java.util.UUID;

// https://blog.csdn.net/China_Style/article/details/109660170
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private Context context;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;

    private NetworkTestViewModel mNetworkTestViewModel;

    private boolean isTestStarted;

    @SuppressLint("RestrictedApi")
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

        mNetworkManager = NetworkManager.getManager(this);
        mCellularManager = CellularManager.getManager(this);
        mLocationManager = LocationServiceManager.getManager(this.getApplicationContext());
        locationServiceListener = new LocationServiceListener(this, getLifecycle());

        mNetworkTestViewModel = new NetworkTestViewModel(this);
        mNetworkTestViewModel.getmSavedIperfDownInfo().observe(this, this::parseWorkInfo);
        mNetworkTestViewModel.getmSavedIperfUpInfo().observe(this, this::parseWorkInfo);



        getLifecycle().addObserver(locationServiceListener);
        this.context = this;
        this.isTestStarted = false;

        if (!this.mNetworkManager.isCellularConnected()) {
            updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
        }

        setUpFAB();
//        updateFAB(this.mNetworkManager.isCellularConnected());

        // FIXME: delete this line after test
        updateFAB(true);
        setUpTestSection();

        this.mNetworkManager.addNetworkChangeListener(new NetworkChangeListener() {
            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on available");
                updateFAB(true);
                mCellularManager.listenToSignalStrengthChange((level, dBm) ->
                                                                updateSignalStrengthTexts(level, dBm));
            }

            @Override
            public void onLost() {
                mCellularManager.stopListening();
                Log.e(TAG, "on lost");
                updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                updateFAB(false);
            }

            @Override
            public void onUnavailable() {
                Log.e(TAG, "on unavailable");
                updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                updateFAB(false);
            }

            @Override
            public void onCellularNetworkChanged(boolean isConnected) {
                Log.e(TAG, "on connection lost");
                if (!isConnected) {
                    updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
                    updateFAB(false);
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

    private void setUpTestSection() {
        runOnUiThread(() -> {
            ConstraintLayout upload = findViewById(R.id.upload);
            ConstraintLayout download = findViewById(R.id.download);
            ConstraintLayout ping = findViewById(R.id.ping);

            ImageView uploadIcon = upload.findViewById(R.id.icon);
            ImageView downloadIcon = download.findViewById(R.id.icon);
            ImageView pingIcon = ping.findViewById(R.id.icon);
            TextView uploadText = upload.findViewById(R.id.data);
            TextView downloadText = download.findViewById(R.id.data);
            TextView pingText = ping.findViewById(R.id.data);

            uploadIcon.setBackgroundResource(R.drawable.upload);
            downloadIcon.setBackgroundResource(R.drawable.download);
            pingIcon.setBackgroundResource(R.drawable.ping);

            uploadText.setText("0.0 Mbit");
            uploadText.setTextColor(getColor(R.color.light_gray));
            downloadText.setText("0.0 Mbit");
            downloadText.setTextColor(getColor(R.color.light_gray));
            pingText.setText("0.0 ms");
            pingText.setTextColor(getColor(R.color.light_gray));
        });
    }

    private void setUpFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(button -> {
            ((FloatingActionButton) button).setImageResource( this.isTestStarted ? R.drawable.start : R.drawable.stop );
            fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));

            this.isTestStarted = !isTestStarted;

            if (this.isTestStarted) {
                setUpTestSection();
                mNetworkTestViewModel.run();
            } else {
                mNetworkTestViewModel.cancel();
            }

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


            this.isTestStarted = false;
        });
    }


    // TODO: update FAB Icon and State when tests are done


    // TODO: pre-test check should be here ...

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

    @SuppressLint("RestrictedApi")
    private void parseWorkInfo(List<WorkInfo> workInfoList) {
        // if there are no matching work info, do nothing
        if (workInfoList == null || workInfoList.isEmpty()) return;

        WorkInfo workInfo = workInfoList.get(0);
        Data progress = workInfo.getProgress();
        Data output = workInfo.getOutputData();
        switch (workInfo.getState()) {
            case FAILED:

                if (output.size() == 1) {
                    boolean isTestCancelled = output.getBoolean("IS_CANCELLED", false);
                    Toast.makeText(this, "test is cancelled: ", Toast.LENGTH_SHORT).show();
                }

                if (isTestStarted) {
                    UIUtils.showDialog(context, R.string.error, R.string.iperf_error, android.R.string.ok, null, android.R.string.cancel, null);
                    mNetworkTestViewModel.cancel();

                    // TODO: update based on network condition
                    updateFAB(true);
                }
            case RUNNING:
                if (progress.size() == 0) break;
                String bandWidth = progress.getString("INTERVAL_BANDWIDTH");
                boolean isDownModeInProgress = progress.getBoolean("IS_DOWN_MODE", false);
                runOnUiThread(() -> {
                    TextView speedTest = (isDownModeInProgress) ? findViewById(R.id.upload).findViewById(R.id.data) : findViewById(R.id.download).findViewById(R.id.data);
                    speedTest.setTextColor(getColor(R.color.light_gray));
                    speedTest.setText(bandWidth);
                });
            case SUCCEEDED:
                if (output.size() == 0) break;
                String finalResult = output.getString("FINAL_RESULT");
                boolean isDownModeInSucceeded = output.getBoolean("IS_DOWN_MODE", false);
                runOnUiThread(() -> {
                    TextView speedTest = (isDownModeInSucceeded) ? findViewById(R.id.upload).findViewById(R.id.data) : findViewById(R.id.download).findViewById(R.id.data);
                    speedTest.setTextColor(getColor(R.color.white));
                    speedTest.setText(finalResult);
                    if (workInfo.getTags().contains("IPERF_UP")) {
                        // TODO: update according to network status
                        updateFAB(true);
                    }
                });
        }
    }
}

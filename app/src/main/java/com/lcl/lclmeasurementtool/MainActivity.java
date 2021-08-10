package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.LocationUpdatesListener;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import java.util.UUID;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private Context context;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;
    Circle mapCircle;

    private boolean isTestStarted;
    private boolean isCellularConnected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this.getApplicationContext();

        // set up UUID
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains(getString(R.string.USER_UUID))) {
            String uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.USER_UUID), uuid);
            editor.apply();
        }

        // prepare necessary information managers
        mNetworkManager = NetworkManager.getManager(this);
        mCellularManager = CellularManager.getManager(this);
        mLocationManager = LocationServiceManager.getManager(this);
        locationServiceListener = new LocationServiceListener(this, getLifecycle());
        getLifecycle().addObserver(locationServiceListener);
        this.isTestStarted = false;
        this.isCellularConnected = false;

        // update and listen to signal strength changes
        updateSignalStrengthTexts(mCellularManager.getSignalStrengthLevel(), mCellularManager.getDBM());
        mCellularManager.listenToSignalStrengthChange(this::updateSignalStrengthTexts);

        // enable map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map Fragment is null");
        }

        // set up FAB
        setUpFAB();
        updateFAB(isCellularConnected);

        this.mNetworkManager.addNetworkChangeListener(new NetworkChangeListener() {

            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on cellular available");
                isCellularConnected = true;
                updateFAB(true);
            }

            @Override
            public void onLost() {
                mCellularManager.stopListening();
                Log.e(TAG, "on cellular lost");
                isCellularConnected = false;
                // TODO cancel test
                updateFAB(false);
            }

            @Override
            public void onUnavailable() {
                Log.e(TAG, "on cellular unavailable");
                isCellularConnected = false;
                // TODO cancel test
                updateFAB(false);
            }

            @Override
            public void onCellularNetworkChanged(NetworkCapabilities capabilities) { }

        }, new NetworkChangeListener() {
            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on wifi available");
                isCellularConnected = false;
                // TODO: cancel test
                updateFAB(false);
            }

            @Override
            public void onUnavailable() { }

            @Override
            public void onLost() { }

            @Override
            public void onCellularNetworkChanged(NetworkCapabilities capabilities) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCellularManager.isSimCardAbsence()) {
            UIUtils.showDialog(this,
                    R.string.sim_missing,
                    R.string.sim_missing_message,
                    android.R.string.ok,
                    (dialog, which) -> {
                        finishAndRemoveTask();
                        System.exit(0);
                    },
                    -1,
                    null
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mLocationManager.stop();
        this.mCellularManager.stopListening();
        this.mNetworkManager.stopListenToNetworkChanges();
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
        CircularProgressIndicator progressIndicator = findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.INVISIBLE);
        fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
        fab.setOnClickListener(button -> {

            if (!this.isCellularConnected) {
                // raise alert telling user to enable cellular data
                Log.e(TAG, "not connected to cellular network");


                UIUtils.showDialog(this,
                        R.string.cellular_on_title,
                        R.string.cellular_on_message,
                        R.string.settings,
                        (dialog, which) -> {
                            Intent networkSettings = new Intent(Settings.ACTION_SETTINGS);
                            networkSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(networkSettings);
                        },
                        android.R.string.cancel, null);

            } else {
                ((FloatingActionButton) button).setImageResource( this.isTestStarted ? R.drawable.start : R.drawable.stop );
                fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
                progressIndicator.setVisibility(this.isTestStarted ? View.INVISIBLE : View.VISIBLE);

                // TODO: init/cancel ping and iperf based in iTestStart

                this.isTestStarted = !isTestStarted;
                Toast.makeText(this, "test starts: " + this.isTestStarted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFAB(boolean state) {
        runOnUiThread(() -> {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setSelected(state);
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
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMaxZoomPreference(20.0f);
        googleMap.setMinZoomPreference(6.0f);
        CircleOptions circleOption = new CircleOptions()
                                        .radius(10)
                                        .strokeWidth(4)
                                        .strokeColor(Color.WHITE)
                                        .fillColor(R.color.purple_500);

        mLocationManager.requestLocationUpdates(location -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "location updates starts");
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 17f)));
                    if (mapCircle != null) {
                        mapCircle.setCenter(latLng);
//                        mapCircle.remove();
//                        mapCircle = googleMap.addCircle(circleOption.center(latLng));
                    }
                    mapCircle = googleMap.addCircle(circleOption.center(latLng));
                }
            });
        });

//        runOnUiThread(() -> mLocationManager.requestLocationUpdates(location -> {
//            Log.i(TAG, "location updates starts");
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 18f)));
//            mapCircle = googleMap.addCircle(circleOption.center(latLng));
//        }));
//        mLocationManager.startLocationUpdates(location -> {
//            Log.i(TAG, "location updates starts");
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
////            Log.i(TAG, latLng.toString());
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 18f)));
//            mapCircle = googleMap.addCircle(circleOption.center(latLng));
////            if (mapCircle != null) {
////                mapCircle.setCenter(latLng);
////            } else {
////                circleOption.center(latLng);
////                mapCircle = googleMap.addCircle(circleOption);
////            }
//        });
    }
}
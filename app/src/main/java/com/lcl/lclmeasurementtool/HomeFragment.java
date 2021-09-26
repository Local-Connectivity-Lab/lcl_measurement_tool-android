package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lcl.lclmeasurementtool.Managers.CellularChangeListener;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;
import com.lcl.lclmeasurementtool.databinding.HomeFragmentBinding;

import java.time.ZoneId;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private HomeFragmentBinding binding;
    private FragmentActivity activity;
    private Context context;
    public static final String TAG = "MAIN_FRAGMENT";

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;
    Location testLocation;
    GoogleMap map;

    private boolean isTestStarted;
    private boolean isCellularConnected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        this.activity = getActivity();
        this.context = getContext();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCellularManager.isSimCardAbsence()) {
            UIUtils.showDialog(this.context,
                    R.string.sim_missing,
                    R.string.sim_missing_message,
                    android.R.string.ok,
                    (dialog, which) -> {
                        this.activity.finishAndRemoveTask();
                        System.exit(0);
                    },
                    -1,
                    null
            );
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // prepare necessary information managers
        mNetworkManager = NetworkManager.getManager(this.context);
        mCellularManager = CellularManager.getManager(this.context);
        mLocationManager = LocationServiceManager.getManager(this.context);
        locationServiceListener = new LocationServiceListener(this.context, getLifecycle());
        getLifecycle().addObserver(locationServiceListener);

        this.isTestStarted = false;
        this.isCellularConnected = false;

        // update and listen to signal strength changes
        updateSignalStrengthTexts(mCellularManager.getSignalStrengthLevel(), mCellularManager.getDBM());
        mCellularManager.listenToSignalStrengthChange(new CellularChangeListener() {
            @Override
            public void onChange(SignalStrengthLevel level, int dBm) {
                updateSignalStrengthTexts(level, dBm);
                String curTime = TimeUtils.getTimeStamp(ZoneId.systemDefault());
                mLocationManager.getLastLocation(location -> {
                    LatLng latLng = LocationUtils.toLatLng(location);
//                    db.signalStrengthDAO().insert(new SignalStrength(curTime, dBm, level.getLevelCode(), latLng));
                });
            }
        });

        // enable map
        SupportMapFragment mapFragment = (SupportMapFragment) this.activity.getSupportFragmentManager()
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
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    ///////////////////// HELPER /////////////////////////
    private void updateSignalStrengthTexts(SignalStrengthLevel level, int dBm) {
        this.activity.runOnUiThread(() -> {
            binding.SignalStrengthValue.setText(String.valueOf(dBm));
            binding.SignalStrengthStatus.setText(level.getName());
            binding.SignalStrengthUnit.setText(UnitUtils.SIGNAL_STRENGTH_UNIT);
            binding.SignalStrengthIndicator.setColorFilter(level.getColor(this.context));
        });
    }

    private void setUpFAB() {
        FloatingActionButton fab = binding.fab;
        CircularProgressIndicator progressIndicator = binding.getRoot().findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.INVISIBLE);
        fab.setColorFilter(ContextCompat.getColor(this.context, R.color.purple_500));
        fab.setOnClickListener(button -> {

            if (!this.isCellularConnected) {
                // raise alert telling user to enable cellular data
                Log.e(TAG, "not connected to cellular network");


                UIUtils.showDialog(this.context,
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
                fab.setColorFilter(ContextCompat.getColor(this.context, R.color.purple_500));
                progressIndicator.setActivated(this.isTestStarted);
                progressIndicator.setVisibility(this.isTestStarted ? View.INVISIBLE : View.VISIBLE);

                // TODO: init/cancel ping and iperf based in iTestStart
                this.mLocationManager.getLastLocation(testLocation::set);
                LatLng testLatLng = LocationUtils.toLatLng(testLocation);
                this.map.addMarker(new MarkerOptions().position(testLatLng).draggable(false));

                this.isTestStarted = !isTestStarted;
                Toast.makeText(this.context, "test starts: " + this.isTestStarted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFAB(boolean state) {
        this.activity.runOnUiThread(() -> {
            FloatingActionButton fab = binding.fab;
            fab.setSelected(state);
            fab.setImageResource(R.drawable.start);
            fab.setColorFilter(state ? ContextCompat.getColor(this.context, R.color.purple_500) :
                    ContextCompat.getColor(this.context, R.color.light_gray));

//             TODO: cancel ping and iperf if started
//            if (isTestStarted) {
            // cancel test
//            }

            this.isTestStarted = false;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        this.map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMaxZoomPreference(20.0f);
        googleMap.setMinZoomPreference(6.0f);
    }
}

package com.lcl.lclmeasurementtool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.BaseProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
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
import com.lcl.lclmeasurementtool.Functionality.NetworkTestViewModel;
import com.lcl.lclmeasurementtool.databinding.HomeFragmentBinding;

import java.time.ZoneId;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private HomeFragmentBinding binding;
    private FragmentActivity activity;
    private Context context;
    public static final String TAG = "MAIN_FRAGMENT";
    private static final int SIGNAL_THRESHOLD = 5;

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;
    Location testLocation;
    GoogleMap map;

    private boolean isTestStarted;
    private boolean isCellularConnected;
    private NetworkTestViewModel mNetworkTestViewModel;
    private ConnectivityViewModel connectivityViewModel;
    private SignalViewModel signalViewModel;

    private int prevSignalStrength = 0;
    private double prevPing = -1.0;
    private double prevUpload = -1.0;
    private double prevDownload = -1.0;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // prepare necessary information managers
        mNetworkManager = NetworkManager.getManager(this.context);
        mCellularManager = CellularManager.getManager(this.context);
        mLocationManager = LocationServiceManager.getManager(this.context);
        locationServiceListener = new LocationServiceListener(this.context, getLifecycle());
        mNetworkTestViewModel = new NetworkTestViewModel(this.context);
        mNetworkTestViewModel.getmSavedIperfDownInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        mNetworkTestViewModel.getmSavedIperfUpInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        mNetworkTestViewModel.getmSavedPingInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        connectivityViewModel = new ViewModelProvider(this).get(ConnectivityViewModel.class);
        signalViewModel = new ViewModelProvider(this).get(SignalViewModel.class);

        getLifecycle().addObserver(locationServiceListener);

        this.isTestStarted = false;
        this.isCellularConnected = false;

        // update and listen to signal strength changes
        updateSignalStrengthTexts(mCellularManager.getSignalStrengthLevel(), mCellularManager.getDBM());
        mCellularManager.listenToSignalStrengthChange((level, dBm) -> {
            updateSignalStrengthTexts(level, dBm);

            if (prevSignalStrength != 0 && Math.abs(prevSignalStrength - dBm) > SIGNAL_THRESHOLD) {
                prevSignalStrength = dBm;
                // TODO: Daniel add signal strength data to db based on threshold
                String ts = TimeUtils.getTimeStamp(ZoneId.of("PST"));

                mLocationManager.getLastLocation(location -> {
                    LatLng latLng = LocationUtils.toLatLng(location);
                    signalViewModel.insert(new SignalStrength(ts, dBm, level.getLevelCode(), latLng));
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

        setupTestView();
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

    private void setupTestView() {
        binding.upload.icon.setImageResource(R.drawable.upload);
        binding.upload.data.setText("0.0 Mbit");
        binding.upload.data.setTextColor(this.activity.getColor(R.color.light_gray));
        binding.download.icon.setImageResource(R.drawable.download);
        binding.download.data.setText("0.0 Mbit");
        binding.download.data.setTextColor(this.activity.getColor(R.color.light_gray));
        binding.ping.icon.setImageResource(R.drawable.ping);
        binding.ping.data.setText("0.0 ms");
        binding.ping.data.setTextColor(this.activity.getColor(R.color.light_gray));
    }

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
        CircularProgressIndicator progressIndicator = binding.progressIndicator;
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
                if (this.isTestStarted) {
                    progressIndicator.setShowAnimationBehavior(BaseProgressIndicator.SHOW_OUTWARD);
                } else {
                    progressIndicator.setHideAnimationBehavior(BaseProgressIndicator.HIDE_INWARD);
                }
                progressIndicator.setVisibility(this.isTestStarted ? View.INVISIBLE : View.VISIBLE);

                if (this.isTestStarted) {
                    mNetworkTestViewModel.cancel();
                    setupTestView();
                } else {
                    mNetworkTestViewModel.run();
                }

                // TODO: init/cancel ping and iperf based in iTestStart
                // TODO: Daniel retrieve location and add a marker on the map
//                this.mLocationManager.getLastLocation(testLocation::set);
//                LatLng testLatLng = LocationUtils.toLatLng(testLocation);
//                this.map.addMarker(new MarkerOptions().position(testLatLng).draggable(false));

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
            this.isTestStarted = false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private void parseWorkInfo(List<WorkInfo> workInfoList) {
        // if there are no matching work info, do nothing
        if (workInfoList == null || workInfoList.isEmpty()) return;

        WorkInfo workInfo = workInfoList.get(0);
        Data progress = workInfo.getProgress();
        Data output = workInfo.getOutputData();
        switch (workInfo.getState()) {
            case FAILED:

                batchConnectivityReset();
                if (output.size() == 1) {
                    boolean isTestCancelled = output.getBoolean("IS_CANCELLED", false);
                    Toast.makeText(this.context, "test is cancelled: " + isTestCancelled, Toast.LENGTH_SHORT).show();
                }

                if (isTestStarted) {
                    UIUtils.showDialog(context, R.string.error, R.string.iperf_error, android.R.string.ok, null, android.R.string.cancel, null);
                    mNetworkTestViewModel.cancel();
                    binding.progressIndicator.hide();
                    setupTestView();
                    // TODO: update based on network condition
                    updateFAB(true);
                }
            case RUNNING:
                if (progress.size() == 0) break;
                else if (!workInfo.getTags().contains("PING")) {
                    String bandWidth = progress.getString("INTERVAL_BANDWIDTH");
                    boolean isDownModeInProgress = progress.getBoolean("IS_DOWN_MODE", false);
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = (isDownModeInProgress) ? this.activity.findViewById(R.id.download).findViewById(R.id.data) : this.activity.findViewById(R.id.upload).findViewById(R.id.data);
                        speedTest.setTextColor(this.activity.getColor(R.color.light_gray));
                        speedTest.setText(bandWidth);
                    });
                }
            case SUCCEEDED:
                if (output.size() == 0) break;
                String finalResult = output.getString("FINAL_RESULT");
                if (workInfo.getTags().contains("PING")) {
                    if (finalResult == null) break;
                    prevPing = Double.parseDouble(finalResult.split(" ")[0]);
                    this.activity.runOnUiThread(() -> {
                        TextView pingTest = binding.ping.data;
                        pingTest.setTextColor(this.activity.getColor(R.color.white));
                        pingTest.setText(finalResult);
                    });
                } else {
                    if (finalResult == null) break;
                    boolean isDownModeInSucceeded = output.getBoolean("IS_DOWN_MODE", false);
                    if (isDownModeInSucceeded) {
                        prevDownload = Double.parseDouble(finalResult.split(" ")[0]);
                    } else {
                        prevUpload = Double.parseDouble(finalResult.split(" ")[0]);
                    }
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = (isDownModeInSucceeded) ? binding.download.data : binding.upload.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.white));
                        speedTest.setText(finalResult);
                        if (workInfo.getTags().contains("IPERF_UP")) {
                            // TODO: update according to network status
                            binding.progressIndicator.setProgress(100, true);
                            binding.progressIndicator.hide();
                            updateFAB(true);

                            if (isConnectivityAllSet()) {
                                String ts = TimeUtils.getTimeStamp(ZoneId.of("PST"));
                                mLocationManager.getLastLocation(location -> {
                                    LatLng latLng = LocationUtils.toLatLng(location);
                                    connectivityViewModel.insert(new Connectivity(ts, prevPing, prevUpload, prevDownload, latLng));
                                });
                            }
                        }
                    });
                }
        }
    }

    private void batchConnectivityReset() {
        prevDownload = -1.0;
        prevPing = -1.0;
        prevUpload = -1.0;
    }

    private boolean isConnectivityAllSet() {
        return prevDownload != -1.0 && prevPing != -1.0 && prevUpload != -1.0;
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

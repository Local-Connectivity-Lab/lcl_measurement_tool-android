package com.lcl.lclmeasurementtool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.BaseProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jsoniter.output.JsonStream;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.Functionality.UploadViewModel;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.KeyStoreManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;
import com.lcl.lclmeasurementtool.Utils.UIUtils;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;
import com.lcl.lclmeasurementtool.Functionality.NetworkTestViewModel;
import com.lcl.lclmeasurementtool.databinding.HomeFragmentBinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private HomeFragmentBinding binding;
    private FragmentActivity activity;
    private Context context;
    public static final String TAG = "MAIN_FRAGMENT";
    private static final int SIGNAL_THRESHOLD = 2;
    private static final int UPLOAD_CONNECTIVITY = 0;
    private static final int UPLOAD_SIGNAL = 1;
    private String uuid;

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;
    KeyStoreManager mKeyStoreManager;

    private boolean isTestStarted;
    private boolean isCellularConnected;
    private NetworkTestViewModel mNetworkTestViewModel;
    private ConnectivityViewModel connectivityViewModel;
    private SignalViewModel signalViewModel;
    private UploadViewModel uploadViewModelSignalStrength;
    private UploadViewModel uploadViewModelConnectivity;

    private int prevSignalStrength = 0;
    private double prevPing = -1.0;
    private double prevUpload = -1.0;
    private double prevDownload = -1.0;

    private WaitDialog waitDialog;

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
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // prepare necessary information managers
        mNetworkManager = NetworkManager.getManager(this.context);
        mCellularManager = CellularManager.getManager(this.context);
        mLocationManager = LocationServiceManager.getManager(this.context);
        try {
            mKeyStoreManager = KeyStoreManager.getInstance();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationServiceListener = new LocationServiceListener(this.context, getLifecycle());
        mNetworkTestViewModel = new NetworkTestViewModel(this.context);
        mNetworkTestViewModel.getmSavedIperfDownInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        mNetworkTestViewModel.getmSavedIperfUpInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        mNetworkTestViewModel.getmSavedPingInfo().observe(getViewLifecycleOwner(), this::parseWorkInfo);
        connectivityViewModel = new ViewModelProvider(this).get(ConnectivityViewModel.class);
        signalViewModel = new ViewModelProvider(this).get(SignalViewModel.class);
        uploadViewModelSignalStrength = new ViewModelProvider(this).get(UploadViewModel.class);
        uploadViewModelConnectivity = new ViewModelProvider(this).get(UploadViewModel.class);

        uuid = this.activity.getPreferences(Context.MODE_PRIVATE)
                .getString(getString(R.string.USER_UUID), "");

        getLifecycle().addObserver(locationServiceListener);

        this.isTestStarted = false;
        this.isCellularConnected = false;

        // update and listen to signal strength changes
        prevSignalStrength = mCellularManager.getDBM();
        updateSignalStrengthTexts(mCellularManager.getSignalStrengthLevel(), prevSignalStrength);
        mCellularManager.listenToSignalStrengthChange((level, dBm) -> {

            Log.e(TAG, "" + dBm);
            updateSignalStrengthTexts(level, dBm);

            if (prevSignalStrength != 0 && Math.abs(prevSignalStrength - dBm) >= SIGNAL_THRESHOLD) {
                prevSignalStrength = dBm;
                String ts = TimeUtils.getTimeStamp(ZoneId.of("PST"));

                mLocationManager.getLastLocation(location -> {
                    LatLng latLng = LocationUtils.toLatLng(location);

                    Map<String, Object> map = new HashMap<>();
                    map.put("latitude", latLng.latitude);
                    map.put("longitude", latLng.longitude);
                    map.put("timestamp", ts);
                    map.put("dBm", dBm);
                    map.put("level_code", level.getLevelCode());
                    map.put("cell_id", "");
                    map.put("device_id", uuid);

                    String json = JsonStream.serialize(map);
                    PrivateKey sk = mKeyStoreManager.getPrivateKey();
                    if (sk == null) {
                        Log.e(TAG, "unable to get sk");
                        this.activity.finishAndRemoveTask();
                    }
                    byte[] message = SecurityUtils.sign(json.getBytes(StandardCharsets.UTF_8), sk, SecurityUtils.SHA256ECDSA);

                    Map<String, Object> uploadMap = new HashMap<>();
                    uploadMap.put("message", message);
                    byte[] pk = mKeyStoreManager.getPublicKey();
                    if (pk.length == 0) {
                        Log.e(TAG, "unable to get pk");
                        this.activity.finishAndRemoveTask();
                    }
                    uploadMap.put("pk", SecurityUtils.digest(pk, SecurityUtils.SHA256));


                    uploadViewModelSignalStrength.loadData(uploadMap, UPLOAD_SIGNAL);
                    uploadViewModelSignalStrength.upload();
                    signalViewModel.insert(new SignalStrength(ts, dBm, level.getLevelCode(), latLng));
                });
            }
        });


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
            if (binding.SignalStrengthValue != null) {
                binding.SignalStrengthValue.setText(String.valueOf(dBm));
                binding.SignalStrengthStatus.setText(level.getName());
            }
            binding.SignalStrengthUnit.setText(UnitUtils.SIGNAL_STRENGTH_UNIT);
            binding.SignalStrengthIndicator.setColorFilter(level.getColor(this.context));
        });
    }

    private void setUpFAB() {
        FloatingActionButton fab = binding.fab;
//        CircularProgressIndicator progressIndicator = binding.progressIndicator;
//        progressIndicator.setVisibility(View.INVISIBLE);


        fab.setColorFilter(ContextCompat.getColor(this.context, R.color.purple_500));
        fab.setOnClickListener(button -> {

            if (!this.isCellularConnected) {
                // raise alert telling user to enable cellular data
                Log.e(TAG, "not connected to cellular network");

                MessageDialog.build()
                        .setTitle(R.string.cellular_on_title)
                        .setMessage(R.string.cellular_on_message)
                        .setButtonOrientation(LinearLayout.VERTICAL)
                        .setOkButton(R.string.settings, (baseDialog, v) -> {
                            Intent networkSettings = new Intent(Settings.ACTION_SETTINGS);
                            networkSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(networkSettings);
                            return true;
                        }).setOkButton(android.R.string.cancel, (baseDialog, v) -> false).show();

            } else {
                ((FloatingActionButton) button).setImageResource( this.isTestStarted ? R.drawable.start : R.drawable.stop );
                fab.setColorFilter(ContextCompat.getColor(this.context, R.color.purple_500));
//                progressIndicator.setActivated(this.isTestStarted);
                setupTestView();
                if (this.isTestStarted) {
                    WaitDialog.dismiss();
//                    progressIndicator.setShowAnimationBehavior(BaseProgressIndicator.SHOW_OUTWARD);
                    mNetworkTestViewModel.cancel();
                } else {
                    WaitDialog.show("Running Tests...");
//                    progressIndicator.setHideAnimationBehavior(BaseProgressIndicator.HIDE_INWARD);
                    mNetworkTestViewModel.run();
                }
//                progressIndicator.setVisibility(this.isTestStarted ? View.GONE : View.VISIBLE);

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
                    MessageDialog.show(R.string.error, R.string.iperf_error, android.R.string.ok);
                    mNetworkTestViewModel.cancel();
//                    binding.progressIndicator.hide();
                    WaitDialog.dismiss();
                    TipDialog.show("Test Failed.", WaitDialog.TYPE.ERROR);
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
                    Log.i(TAG, "ping is: " + prevPing);
                    this.activity.runOnUiThread(() -> {
                        TextView pingTest = binding.ping.data;
                        pingTest.setTextColor(this.activity.getColor(R.color.white));
                        pingTest.setText(finalResult);
                    });
                } else {

                    if (finalResult == null) break;
                    boolean isDownModeInSucceeded = output.getBoolean("IS_DOWN_MODE", false);
                    if (isDownModeInSucceeded && !finalResult.equals("")) {
                        prevDownload = Double.parseDouble(finalResult.split(" ")[0]);
                    } else {
                        prevUpload = Double.parseDouble(finalResult.split(" ")[0]);
                    }
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = (isDownModeInSucceeded) ? binding.download.data : binding.upload.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.white));
                        speedTest.setText(finalResult);
                        if (workInfo.getTags().contains("IPERF_UP")) {

//                            binding.progressIndicator.setProgress(100, true);
//                            binding.progressIndicator.setVisibility(View.GONE);
                            WaitDialog.dismiss();
                            TipDialog.show("Test Completed!", WaitDialog.TYPE.SUCCESS);
                            updateFAB(true);

                            if (isConnectivityAllSet()) {
                                Log.i(TAG, "prepare for upload");
                                String ts = TimeUtils.getTimeStamp(ZoneId.of("PST"));

                                mLocationManager.getLastLocation(location -> {
                                    LatLng latLng = LocationUtils.toLatLng(location);

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("latitude", latLng.latitude);
                                    map.put("longitude", latLng.longitude);
                                    map.put("timestamp", ts);
                                    map.put("upload_speed", prevUpload);
                                    map.put("download_speed", prevDownload);
                                    map.put("data_since_last_report", "");
                                    map.put("ping", prevPing);
                                    map.put("cell_id", "");
                                    map.put("device_id", uuid);

                                    String json = JsonStream.serialize(map);
                                    PrivateKey sk = mKeyStoreManager.getPrivateKey();
                                    if (sk == null) {
                                        Log.e(TAG, "unable to get sk");
                                        this.activity.finishAndRemoveTask();
                                    }
                                    byte[] message = SecurityUtils.sign(json.getBytes(StandardCharsets.UTF_8), sk, SecurityUtils.SHA256ECDSA);

                                    Map<String, Object> uploadMap = new HashMap<>();
                                    uploadMap.put("message", message);
                                    byte[] pk = mKeyStoreManager.getPublicKey();
                                    if (pk.length == 0) {
                                        Log.e(TAG, "unable to get pk");
                                        this.activity.finishAndRemoveTask();
                                    }
                                    uploadMap.put("pk", SecurityUtils.digest(pk, SecurityUtils.SHA256));


                                    uploadViewModelConnectivity.loadData(uploadMap, UPLOAD_CONNECTIVITY);
                                    uploadViewModelConnectivity.upload();
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
}

package com.lcl.lclmeasurementtool;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkCapabilities;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsoniter.output.JsonStream;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.lcl.lclmeasurementtool.Constants.NetworkConstants;
import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.Functionality.NetworkTestViewModel;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Managers.UploadManager;
import com.lcl.lclmeasurementtool.Models.ConnectivityMessageModel;
import com.lcl.lclmeasurementtool.Models.MeasurementDataModel;
import com.lcl.lclmeasurementtool.Models.SignalStrengthMessageModel;
import com.lcl.lclmeasurementtool.Utils.DecoderException;
import com.lcl.lclmeasurementtool.Utils.ECDSA;
import com.lcl.lclmeasurementtool.Utils.Hex;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;
import com.lcl.lclmeasurementtool.databinding.HomeFragmentBinding;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private HomeFragmentBinding binding;
    private FragmentActivity activity;
    private Context context;
    public static final String TAG = "MAIN_FRAGMENT";
    private static final int SIGNAL_THRESHOLD = 2;
    private String device_id;

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;

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
        SharedPreferences preferences = this.activity.getPreferences(MODE_PRIVATE);
        device_id = preferences.getString("device_id", "unknown");
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private boolean systemReady() {
        SharedPreferences preferences = this.activity.getPreferences(MODE_PRIVATE);
        return (!preferences.contains("sigma_t") || !preferences.contains("pk_a") || !preferences.contains("sk_t"));
    }

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
        prevSignalStrength = mCellularManager.getDBM();
        updateSignalStrengthTexts(mCellularManager.getSignalStrengthLevel(), prevSignalStrength);
        mCellularManager.listenToSignalStrengthChange((level, dBm) -> {
            if (systemReady()) return;

            Log.e(TAG, "" + dBm);
            updateSignalStrengthTexts(level, dBm);

            if (prevSignalStrength != 0 && Math.abs(prevSignalStrength - dBm) >= SIGNAL_THRESHOLD) {
                prevSignalStrength = dBm;
                String ts = TimeUtils.getTimeStamp(ZoneId.of("America/Los_Angeles"));
                String cell_id = mCellularManager.getCellID();
                byte[][] result = retrieveKeysInformation();
                if (result.length == 0) {
                    exitWhenFailure("Keys are compromised. Please rescan the QR Code");
                }

                byte[] h_pkr = result[0];
                byte[] sk_t = result[1];
                mLocationManager.getLastLocation(location -> {
                    LatLng latLng = LocationUtils.toLatLng(location);
                    SignalStrengthMessageModel signalStrengthMessageModel =
                            new SignalStrengthMessageModel(
                                    latLng.latitude,
                                    latLng.longitude,
                                    ts,
                                    dBm,
                                    level.getLevelCode(),
                                    cell_id,
                                    device_id);
                    signalViewModel.insert(new SignalStrength(ts, dBm, level.getLevelCode(), latLng));
                    uploadData(signalStrengthMessageModel, sk_t, h_pkr, NetworkConstants.SIGNAL_ENDPOINT);
                });
            }
        });

        setupTestView();
        setUpFAB();


        this.mNetworkManager.addNetworkChangeListener(new NetworkChangeListener() {

            // Cellular
            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on cellular available");
                isCellularConnected = true;
                setFABToInitialState();
            }

            @Override
            public void onLost() {
                Log.e(TAG, "on cellular lost");
                isCellularConnected = false;
                setFABToDisabledState();
            }

            @Override
            public void onUnavailable() {
                Log.e(TAG, "on cellular unavailable");
                isCellularConnected = false;
                setFABToDisabledState();
            }

            @Override
            public void onCellularNetworkChanged(NetworkCapabilities capabilities) {
            }

        }, new NetworkChangeListener() {

            // WIFI
            @Override
            public void onAvailable() {
                Log.i(TAG, "from call back on wifi available");
                isCellularConnected = false;
                setFABToDisabledState();
            }

            @Override
            public void onUnavailable() {
            }

            @Override
            public void onLost() {
            }

            @Override
            public void onCellularNetworkChanged(NetworkCapabilities capabilities) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        binding = null;
    }

    /////////////////////// SIGNAL //////////////////////
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


    /////////////////////////// Measurement ////////////////////////
    private void setFABToInitialState() {
        isTestStarted = false;
        activity.runOnUiThread(() -> {
            binding.fab.setSelected(true);
            binding.fab.setImageResource(R.drawable.start);
            binding.fab.setColorFilter(ContextCompat.getColor(this.context, R.color.purple_500));
        });
    }

    private void setFABToStartedState() {
        isTestStarted = true;
        resetConnectivities();
        activity.runOnUiThread(() -> {
            setupTestView();
            binding.fab.setImageResource(R.drawable.stop);
        });
    }

    private void setFABToDisabledState() {
        activity.runOnUiThread(() -> {
            binding.fab.setSelected(false);
            binding.fab.setImageResource(R.drawable.start);
            binding.fab.setColorFilter(ContextCompat.getColor(this.context, R.color.light_gray));
            isTestStarted = false;
            setupTestView();
            resetConnectivities();
        });
    }

    private void resetConnectivities() {
        prevDownload = -1.0;
        prevPing = -1.0;
        prevUpload = -1.0;
    }

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


    private void setUpFAB() {
        FloatingActionButton fab = binding.fab;
        if (isCellularConnected) {
            setFABToInitialState();
        } else {
            setFABToDisabledState();
        }

        fab.setOnClickListener(button -> {
            if (!this.isCellularConnected) {
                // raise alert telling user to enable cellular data
                Log.e(TAG, "not connected to cellular network");
                setFABToDisabledState();
                MessageDialog.build()
                        .setTitle(R.string.cellular_on_title)
                        .setMessage(R.string.cellular_on_message)
                        .setButtonOrientation(LinearLayout.VERTICAL)
                        .setOkButton(R.string.settings, (baseDialog, v) -> {
                            Intent networkSettings = new Intent(Settings.ACTION_SETTINGS);
                            networkSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(networkSettings);
                            return false;
                        }).setOkButton(android.R.string.cancel, (baseDialog, v) -> false).show();

            } else {
                if (this.isTestStarted) {
                    setFABToInitialState();
                    mNetworkTestViewModel.cancel();
                    PopTip.show("Test canceled.");
                } else {
                    setFABToStartedState();
                    mNetworkTestViewModel.run();
                    PopTip.show("Test started");
                }

                this.isTestStarted = !isTestStarted;
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void parseWorkInfo(List<WorkInfo> workInfoList) {
        // if there are no matching work info, do nothing
        if (workInfoList == null || workInfoList.isEmpty()) return;

        WorkInfo workInfo = workInfoList.get(0);
        Set<String> tags = workInfo.getTags();
        WorkInfo.State state = workInfo.getState();
        Data progress = workInfo.getProgress();
        Data output = workInfo.getOutputData();

        if (state == WorkInfo.State.SUCCEEDED && output.size() == 0) return;

        if (tags.contains("PING")) {
            switch (state) {
                case FAILED:
                case CANCELLED:
                    setFABToInitialState();
                    mNetworkTestViewModel.cancel();
                    MessageDialog.show(R.string.error, R.string.measurement_test_error, android.R.string.ok);
                    break;
                case RUNNING:
                    // do nothing
                    break;
                case SUCCEEDED:
                    String finalResult = output.getString("FINAL_RESULT");
                    if (finalResult == null || finalResult.isEmpty()) {
                        setFABToInitialState();
                        mNetworkTestViewModel.cancel();
                        return;
                    }

                    prevPing = Double.parseDouble(finalResult.split(" ")[0]);
                    Log.i(TAG, "ping is: " + prevPing);
                    this.activity.runOnUiThread(() -> {
                        TextView pingTest = binding.ping.data;
                        pingTest.setTextColor(this.activity.getColor(R.color.white));
                        pingTest.setText(finalResult);
                    });
                    break;
            }
        } else if (tags.contains("IPERF_UP")) {
            switch (state) {
                case CANCELLED:
                case FAILED:
                    setFABToInitialState();
                    mNetworkTestViewModel.cancel();
                    MessageDialog.show(R.string.error, R.string.measurement_test_error, android.R.string.ok);
                    break;
                case RUNNING:
                    String bandWidth = progress.getString("INTERVAL_BANDWIDTH");
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = binding.upload.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.light_gray));
                        speedTest.setText(bandWidth);
                    });
                    break;
                case SUCCEEDED:
                    String finalResult = output.getString("FINAL_RESULT");
                    if (finalResult == null || finalResult.isEmpty()) {
                        setFABToInitialState();
                        mNetworkTestViewModel.cancel();
                        return;
                    }

                    prevUpload = Double.parseDouble(finalResult.split(" ")[0]);
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = binding.upload.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.white));
                        speedTest.setText(finalResult);
                        PopTip.show("Test completed");
                    });

                    if (isTestCompleted() && isTestStarted) {
                        Log.i(TAG, "prepare for upload");
                        if (systemReady()) return;
                        String ts = TimeUtils.getTimeStamp(ZoneId.of("America/Los_Angeles"));
                        String cell_id = mCellularManager.getCellID();
                        byte[][] result = retrieveKeysInformation();
                        if (result.length == 0) {
                            exitWhenFailure("Keys are compromised. Please rescan the QR Code");
                        }

                        byte[] h_pkr = result[0];
                        byte[] sk_t = result[1];

                        mLocationManager.getLastLocation(location -> {
                            LatLng latLng = LocationUtils.toLatLng(location);
                            ConnectivityMessageModel connectivityMessageModel =
                                    new ConnectivityMessageModel(
                                            latLng.latitude,
                                            latLng.longitude,
                                            ts,
                                            prevUpload,
                                            prevDownload,
                                            prevPing, cell_id, device_id);
                            connectivityViewModel.insert(new Connectivity(ts, prevPing, prevUpload, prevDownload, latLng));
                            uploadData(connectivityMessageModel, sk_t, h_pkr, NetworkConstants.CONNECTIVITY_ENDPOINT);
                        });
                    }

                    setFABToInitialState();
                    break;
            }

        } else if (tags.contains("IPERF_DOWN")) {
            switch (state) {
                case CANCELLED:
                case FAILED:
                    setFABToInitialState();
                    mNetworkTestViewModel.cancel();
                    MessageDialog.show(R.string.error, R.string.measurement_test_error, android.R.string.ok);
                    break;
                case RUNNING:
                    String bandWidth = progress.getString("INTERVAL_BANDWIDTH");
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = binding.download.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.light_gray));
                        speedTest.setText(bandWidth);
                    });
                    break;
                case SUCCEEDED:
                    String finalResult = output.getString("FINAL_RESULT");
                    if (finalResult == null || finalResult.isEmpty()) {
                        setFABToInitialState();
                        mNetworkTestViewModel.cancel();
                        return;
                    }
                    prevDownload = Double.parseDouble(finalResult.split(" ")[0]);
                    this.activity.runOnUiThread(() -> {
                        TextView speedTest = binding.download.data;
                        speedTest.setTextColor(this.activity.getColor(R.color.white));
                        speedTest.setText(finalResult);
                    });
                    break;
            }
        } else {
            // unknown worker
            PopTip.show("Unknown test.");
        }
    }

    ///////////////////// HELPER /////////////////////////
    private boolean isTestCompleted() {
        return prevDownload != -1.0 && prevPing != -1.0 && prevUpload != -1.0;
    }

    private void uploadData(MeasurementDataModel data, byte[] sk_t, byte[] h_pkr, String endpoint) throws NoSuchAlgorithmException,
            InvalidKeySpecException, SignatureException, InvalidKeyException, JsonProcessingException, NoSuchProviderException {

        byte[] serialized = data.serializeToBytes();

        byte[] sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(sk_t));

        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("M", Hex.encodeHexString(serialized));
        uploadMap.put("sigma_m", Hex.encodeHexString(sig_m));
        uploadMap.put("h_pkr", Hex.encodeHexString(h_pkr));

        // upload data
        UploadManager upload = UploadManager.Builder()
                .addPayload(JsonStream.serialize(uploadMap))
                .addEndpoint(endpoint);
        try {
            upload.post();
        } catch (IOException e) {
            showMessageOnFailure();
        }
    }

    private void showMessageOnFailure() {
        TipDialog.show("Cannot connect the server. Please retry or contact the administrator", WaitDialog.TYPE.ERROR);
    }

    private byte[][] retrieveKeysInformation() {
        SharedPreferences preferences = this.activity.getPreferences(MODE_PRIVATE);
        if (!preferences.contains("h_pkr") || !preferences.contains("sk_t")) {
            exitWhenFailure("Key information missing");
        }
        byte[] h_pkr;
        byte[] sk_t;
        try {
            h_pkr = Hex.decodeHex(preferences.getString("h_pkr", ""));
            sk_t = Hex.decodeHex(preferences.getString("sk_t", ""));
        } catch (DecoderException e) {
            e.printStackTrace();
            return new byte[0][];
        }

        if (h_pkr.length == 0 || sk_t.length == 0) {
            preferences.edit().clear().apply();
            exitWhenFailure("Keys are compromised. Please rescan the QR Code");
        }
        return new byte[][]{h_pkr, sk_t};
    }

    private void exitWhenFailure(String message) {
        MessageDialog.show("Error", message, "ok").setOkButton((baseDialog, v) -> {
            activity.finishAndRemoveTask();
            System.exit(1);
            return true;
        });
    }
}

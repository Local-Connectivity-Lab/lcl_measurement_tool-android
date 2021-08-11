package com.lcl.lclmeasurementtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcl.lclmeasurementtool.Functionality.Iperf;
import com.lcl.lclmeasurementtool.Functionality.IperfListener;
import com.lcl.lclmeasurementtool.Functionality.IperfStats;
import com.lcl.lclmeasurementtool.Managers.CellularChangeListener;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity<mCellularManager> extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    private Context context;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        String appFileDirectory = getCacheDir().getAbsolutePath();
        String executableFilePath = appFileDirectory + "/iperf3";

        File cmdFile = new File(executableFilePath);
        if (cmdFile.exists()) {
            cmdFile.setExecutable(true, true);
        } else {

            try {
                OutputStream out = new FileOutputStream(cmdFile);
                FileUtils.copyToFile(getAssets().open("iperf3"), cmdFile);
                cmdFile.setExecutable(true, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Process process = Runtime.getRuntime().exec(executableFilePath + " -version");
            process.waitFor();
            int exitVal = process.exitValue();
            if (exitVal == 0) {
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String output;

                while ((output = buffer.readLine()) != null) {
                    Log.i(TAG, output);
                }
            } else {
                Log.e(TAG, "failed");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

//        Iperf iperf = new Iperf();
//       iperf.setServerIPAddress("192.168.1.30");
//       iperf.setNumSecondsForTest(60);
//       iperf.start(new IperfListener() {
//           @Override
//           public void onError(Exception e) {
//               Log.e(TAG, e.getMessage());
//           }
//
//           @Override
//           public void onStart() {
//               Log.i(TAG, "iperf starts");
//
//           }
//
//           @Override
//           public void onFinished(IperfStats stats) {
//               Log.i(TAG, "iperf finishes");
//               Log.i(TAG, stats.getFullOutput());
//
//           }
//       });
    }


//        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            Toast.makeText(this, "Enable location services for accurate data", Toast.LENGTH_SHORT).show();
//        }


/*

        NetworkManager mNetworkManager = new NetworkManager(this);
        mCellularManager = CellularManager.getManager(this);

        if (!mNetworkManager.isCellularConnected()) {
            updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
            updateFAB(false);
        }

        mNetworkManager.addNetworkChangeListener(new NetworkManager.NetworkChangeListener() {
            @Override
            public void onAvailable() {
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
*/
    private void updateSignalStrengthTexts(SignalStrengthLevel level, int dBm) {
        runOnUiThread(() -> {
            TextView signalStrengthValue = findViewById(R.id.SignalStrengthValue);
            TextView signalStrengthStatus = findViewById(R.id.SignalStrengthStatus);
            ImageView signalStrengthIndicator = findViewById(R.id.SignalStrengthIndicator);
            signalStrengthValue.setText(String.valueOf(dBm));
            signalStrengthStatus.setText(level.getName());
            signalStrengthIndicator.setColorFilter(level.getColor(context));
        });
    }

    private void updateFAB(boolean state) {
        runOnUiThread(() -> {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setEnabled(state);
            fab.setBackgroundColor(state ? ContextCompat.getColor(this, R.color.white) :
                    ContextCompat.getColor(this, R.color.light_gray));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        mNetworkManager.removeAllNetworkChangeListeners();
    }
}
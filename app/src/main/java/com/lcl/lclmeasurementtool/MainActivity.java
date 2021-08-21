package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcl.lclmeasurementtool.Functionality.Iperf;
import com.lcl.lclmeasurementtool.Functionality.Iperf3Callback;
import com.lcl.lclmeasurementtool.Functionality.Iperf3Client;
import com.lcl.lclmeasurementtool.Functionality.Iperf3Config;
import com.lcl.lclmeasurementtool.Functionality.IperfListener;
import com.lcl.lclmeasurementtool.Functionality.IperfStats;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// https://blog.csdn.net/China_Style/article/details/109660170
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


        Iperf3Client iperf3Client = new Iperf3Client(new Iperf3Callback() {
            @Override
            public void onConnecting(String destHost, int destPort) {
                Log.i(TAG, "on connecting");
            }

            @Override
            public void onConnected(String localAddr, int localPort, String destAddr, int destPort) {
                Log.i(TAG, "connected to testing server");
            }

            @Override
            public void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown) {

            }

            @Override
            public void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown) {
                Log.i(TAG, "result is " + timeStart + " " + timeEnd + " " + sendBytes + " " + bandWidth);
            }

            @Override
            public void onError(String errMsg) {
                Log.e(TAG, errMsg);
            }
        });

        Iperf3Config config = new Iperf3Config();
        config.mServerAddr = "iperf.biznetnetworks.com";
        config.mServerPort = 5203;
        config.parallels = 1;
        config.isDownMode = false;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iperf3Client.exec(config);
            }
        });
    }

        /*try {
         //     Process process = Runtime.getRuntime().exec(executableFilePath + " -version");
            Process process = Runtime.getRuntime().exec(executableFilePath + " -c speedtest.iveloz.net.br -b 1M");

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
        }*/

//        Iperf iperf = new Iperf();
//
//        iperf.setServerIPAddress("speedtest.iveloz.net.br");
//        iperf.setNumSecondsForTest(60);
//        iperf.start(new IperfListener() {
//            @Override
//            public void onError(Exception e) {
//                Log.e(TAG, e.getMessage());
//            }
//
//            @Override
//            public void onStart() {
//                Log.i(TAG, "iperf starts");
//
//            }
//
//            @Override
//            public void onFinished(IperfStats stats) {
//                Log.i(TAG, "iperf finishes");
//                Log.i(TAG, stats.getFullOutput());
//
//            }
//        }, executableFilePath);
//    }


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

//    private String setupIperf() {
//        String appFileDirectory = getFilesDir().getAbsolutePath();
//        String executableFilePath = appFileDirectory + "/iperf3";
//
//        File cmdFile = new File(executableFilePath);
//        if (cmdFile.exists()) {
//            cmdFile.setExecutable(true, true);
//        } else {
//
//            try {
//                OutputStream out = new FileOutputStream(cmdFile);
//                FileUtils.copyToFile(getAssets().open("iperf3"), cmdFile);
//                cmdFile.setExecutable(true, true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return executableFilePath;
//    }
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
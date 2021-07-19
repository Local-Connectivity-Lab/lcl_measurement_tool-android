package com.lcl.lclmeasurementtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.UnitUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    private Context context;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;

    private boolean isTestStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        isTestStarted = false;


//        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            Toast.makeText(this, "Enable location services for accurate data", Toast.LENGTH_SHORT).show();
//        }




        mNetworkManager = new NetworkManager(this);
        mCellularManager = CellularManager.getManager(this);

        if (!mNetworkManager.isCellularConnected()) {
            updateSignalStrengthTexts(SignalStrengthLevel.NONE, 0);
        }

        setUpFAB();
        updateFAB(mNetworkManager.isCellularConnected());

        mNetworkManager.addNetworkChangeListener(new NetworkManager.NetworkChangeListener() {
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
            ((FloatingActionButton) button).setImageResource( isTestStarted ? R.drawable.start : R.drawable.stop );
            fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));

            // TODO: init/cancel ping and iperf based in iTestStart

            isTestStarted = !isTestStarted;
            Toast.makeText(this, "test starts: " + isTestStarted, Toast.LENGTH_SHORT).show();
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

            isTestStarted = false;
        });
    }


    // TODO: update FAB Icon and State when tests are done


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        mNetworkManager.removeAllNetworkChangeListeners();
    }
}
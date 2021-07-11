package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkManager mNetworkManager = new NetworkManager(this);
        mCellularManager = CellularManager.getManager(this);

        TextView tv = (TextView) findViewById(R.id.signalStrengthStatus);

        mNetworkManager.addNetworkChangeListener(new NetworkManager.NetworkChangeListener() {
            @Override
            public void onAvailable() {
//                Log.i(TAG, "The cellular network is now available");
                mCellularManager.listenToSignalStrengthChange(tv);
            }

            @Override
            public void onLost() {
//                Log.i(TAG, "The cellular network is now lost");
                mCellularManager.stopListening();
                tv.setText(SignalStrengthLevel.NONE.getName());
            }

            @Override
            public void onUnavailable() {
//                Log.i(TAG, "The cellular network is unavailable");
            }

            @Override
            public void onCellularNetworkChanged(boolean isConnected) {
//                Log.i(TAG, "The cellular network is connected? " + isConnected);
            }
        });

//        if (mNetworkManager.isCellularConnected()) {
//            mCellularManager.listenToSignalStrengthChange(tv);
//        } else {
//            Toast.makeText(this, "You are not connected via cellular", Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        mNetworkManager.removeAllNetworkChangeListeners();
    }
}
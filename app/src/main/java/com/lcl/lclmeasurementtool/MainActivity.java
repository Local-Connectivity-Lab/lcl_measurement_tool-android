package com.lcl.lclmeasurementtool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;

public class MainActivity extends AppCompatActivity {

    CellularManager mCellularManager;
    NetworkManager mNetworkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkManager mNetworkManager = new NetworkManager(this);
        mCellularManager = CellularManager.getManager(this);

        TextView tv = (TextView) findViewById(R.id.signalStrengthStatus);

        if (mNetworkManager.isCellularConnected()) {
            mCellularManager.listenToSignalStrengthChange(tv);
        } else {
            Toast.makeText(this, "You are not connected via cellular", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
    }
}
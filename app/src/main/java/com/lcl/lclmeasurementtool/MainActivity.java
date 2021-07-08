package com.lcl.lclmeasurementtool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.lcl.lclmeasurementtool.Functionality.Ping;
import com.lcl.lclmeasurementtool.Functionality.PingStats;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the following code calling into ping needs to be moved into the right place
        try {

            Log.i("LCL_PING", "starting ping");

            // TODO: pinging local host because other hosts are not reachable at the moment.

            Ping pingObj = new Ping ("127.0.0.1", 5);
            String output = pingObj.launchPing();
            String packetLoss = pingObj.getPacketLoss();
            PingStats pingStats = pingObj.getLatency();

            Log.i("LCL_PING", "PACKET LOSS = " + packetLoss);
            Log.i("LCL_PING", "Max Latency = " + pingStats.getMaxLatency());
            Log.i("LCL_PING","Average Latency = " + pingStats.getAverageLatency());
            Log.i("LCL_PING","Min latency = " + pingStats.getMinLatency());
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
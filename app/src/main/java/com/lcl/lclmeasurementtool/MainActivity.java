package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.lcl.lclmeasurementtool.Managers.CellularManager;
import com.lcl.lclmeasurementtool.Managers.LocationServiceListener;
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;
    LocationServiceListener locationServiceListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNetworkManager = NetworkManager.getManager(this);
        mCellularManager = CellularManager.getManager(this);
        mLocationManager = LocationServiceManager.getManager(this);

        locationServiceListener = new LocationServiceListener(this);
        getLifecycle().addObserver(locationServiceListener);

        TextView tv = (TextView) findViewById(R.id.signalStrengthStatus);

        mNetworkManager.addNetworkChangeListener(new NetworkChangeListener() {
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
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        mNetworkManager.removeAllNetworkChangeListeners();
        getLifecycle().removeObserver(locationServiceListener);
    }


    ////////////////// HELPER FUNCTION ///////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and we
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.i(TAG, "Location permission granted");
            } else {
                // Permission denied.

                // Notify the user via a dialog that they have rejected a core permission for the
                // app, which makes the Activity useless.
                UIUtils.showDialog(this,
                        R.string.location_message_title,
                        R.string.permission_denied_explanation,
                        R.string.settings,
                        (dialogInterface, actionID) -> {

                    // Build intent that displays the App settings screen.
                    Intent intent = new Intent();
                    intent.setAction(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }, android.R.string.cancel, null);
            }
        }
    }

    /**
     * Fetch the last location from the device
     */
    private void getLastLocation() {
        mLocationManager.getLastLocation();
    }

}
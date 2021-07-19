package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
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
        getLifecycle().removeObserver(locationServiceListener);
    }


    ////////////////// HELPER FUNCTION ///////////////////////

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.i(TAG, "Location permission granted");
//                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                UIUtils.showDialog(this, R.string.location_message_title, R.string.permission_denied_explanation, R.string.settings,
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

    private void getLastLocation() {
        mLocationManager.getLastLocation();
    }

}
package com.lcl.lclmeasurementtool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.lcl.lclmeasurementtool.Managers.LocationServiceManager;
import com.lcl.lclmeasurementtool.Managers.NetworkChangeListener;
import com.lcl.lclmeasurementtool.Managers.NetworkManager;
import com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    CellularManager mCellularManager;
    NetworkManager mNetworkManager;
    LocationServiceManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNetworkManager = NetworkManager.getManager(this);
        mCellularManager = CellularManager.getManager(this);
        mLocationManager = LocationServiceManager.getManager(this);

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
    protected void onStart() {
        super.onStart();

        if (!mLocationManager.isLocationServiceEnabled()) {
            requestLocationPermission();
        }

        if (!mLocationManager.isLocationModeOn()) {

            // TODO turn off start FAB

            showDialog(R.string.location_message_title, R.string.enable_location_message,
                    R.string.go_to_setting,
                    (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)),
                    android.R.string.cancel,
                    null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellularManager.stopListening();
        mNetworkManager.removeAllNetworkChangeListeners();
    }


    ////////////////// HELPER FUNCTION ///////////////////////

    private void requestLocationPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {

            showDialog(R.string.location_message_title, R.string.permission_rationale,
                    android.R.string.ok,
                    (a, b) -> startLocationPermissionRequest(),
                    android.R.string.cancel, null);
//            showSnackbar(R.string.permission_rationale,
//                    android.R.string.ok,
//                    v -> startLocationPermissionRequest());
        } else {
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void showDialog(int title, final int messageID,
                            final int positiveMessageID,
                            DialogInterface.OnClickListener positiveListener,
                            final int negativeMessageID,
                            DialogInterface.OnClickListener negativeListener) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(messageID)
                .setPositiveButton(positiveMessageID, positiveListener)
                .setNegativeButton(negativeMessageID, negativeListener)
                .show();
    }

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
                showDialog(R.string.location_message_title, R.string.permission_denied_explanation, R.string.settings,
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
//                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
//                        view -> {
//                            // Build intent that displays the App settings screen.
//                            Intent intent = new Intent();
//                            intent.setAction(
//                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package",
//                                    BuildConfig.APPLICATION_ID, null);
//                            intent.setData(uri);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        });
            }
        }
    }

    private void getLastLocation() {
        mLocationManager.getLastLocation();
    }

}
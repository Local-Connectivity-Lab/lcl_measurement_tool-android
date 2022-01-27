package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.lcl.lclmeasurementtool.MainActivity;
import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

public class LocationServiceListener implements LifecycleObserver {

    private static final String TAG = "LOCATION_SERVICE_LISTENER";

    // permission code
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // the Location service object
    private final LocationServiceManager mLocationManager;

    // the context of the Application
    private final Context context;

    // the life cycle object
    private final Lifecycle lifecycle;

    // the lock that controls the check on location mode
    private boolean checkLocationModeLock = false;

    /**
     * Initialize a LocationService Listener using the current context of the Application
     *
     * @param context the context of the application
     */
    public LocationServiceListener(@NonNull Context context, Lifecycle lifecycle) {
        this.context = context;
        this.mLocationManager = LocationServiceManager.getManager(this.context);
        this.lifecycle = lifecycle;
    }


    /**
     * Check the location permission during on_resume in app's lifecycle
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationPermission() {
        if (!this.mLocationManager.isLocationPermissionGranted()) {
            requestLocationPermission();
        }
    }

    /**
     * Check the location service during on_resume in app's lifecycle
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationMode() {
        if (!this.mLocationManager.isLocationModeOn() && lifecycle.getCurrentState().equals(Lifecycle.State.RESUMED) && !checkLocationModeLock) {
            checkLocationModeLock = true;
            MessageDialog.build()
                    .setTitle(R.string.location_message_title)
                    .setMessage(R.string.enable_location_message)
                    .setOkButton(R.string.go_to_setting, (baseDialog, v) -> {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        checkLocationModeLock = false;
                        return false;
                    })
                    .setOkButton(android.R.string.cancel).show();
        }
    }

    /**
     * Request location permission to users
     */
    private void requestLocationPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            MessageDialog.build()
                    .setTitle(R.string.location_message_title)
                    .setMessage(R.string.permission_rationale)
                    .setOkButton(android.R.string.ok, (baseDialog, v) -> {
                        startLocationPermissionRequest();
                        return false;
                    })
                    .setOkButton(android.R.string.cancel).show();
        } else {
            startLocationPermissionRequest();
        }
    }

    /**
     * Start the permission request
     */
    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}

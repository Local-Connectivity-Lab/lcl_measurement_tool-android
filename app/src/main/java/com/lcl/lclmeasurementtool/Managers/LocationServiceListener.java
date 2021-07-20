package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

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
        this.mLocationManager = LocationServiceManager.getManager(context);
        this.lifecycle = lifecycle;
    }


    /**
     * Check the location permission during on_resume in app's lifecycle
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationPermission() {
        if (!mLocationManager.isLocationPermissionGranted()) {
            requestLocationPermission();
        }
    }

    /**
     * Check the location service during on_resume in app's lifecycle
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationMode() {
        if (!mLocationManager.isLocationModeOn() && lifecycle.getCurrentState().equals(Lifecycle.State.RESUMED) && !checkLocationModeLock) {
            System.out.println(1);
            checkLocationModeLock = true;
            // TODO turn off start FAB if canceled
            UIUtils.showDialog(context, R.string.location_message_title, R.string.enable_location_message,
                    R.string.go_to_setting,
                    (paramDialogInterface, paramInt) -> {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        checkLocationModeLock = false;
                    },
                    android.R.string.cancel,
                    null);
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
            UIUtils.showDialog(context, R.string.location_message_title, R.string.permission_rationale,
                    android.R.string.ok,
                    (a, b) -> startLocationPermissionRequest(),
                    android.R.string.cancel, null);
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

package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.lcl.lclmeasurementtool.R;
import com.lcl.lclmeasurementtool.Utils.UIUtils;

public class LocationServiceListener implements LifecycleObserver {


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private LocationServiceManager mLocationManager;
    private Context context;

    public LocationServiceListener(Context context) {
        this.context = context;
        this.mLocationManager = LocationServiceManager.getManager(context);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationPermission() {
        if (!mLocationManager.isLocationPermissionGranted()) {
            requestLocationPermission();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void checkLocationMode() {
        if (!mLocationManager.isLocationModeOn()) {

            // TODO turn off start FAB if canceled
            UIUtils.showDialog(context, R.string.location_message_title, R.string.enable_location_message,
                    R.string.go_to_setting,
                    (paramDialogInterface, paramInt) -> context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)),
                    android.R.string.cancel,
                    null);
        }
    }

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

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}

package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.lcl.lclmeasurementtool.R;

import java.lang.ref.WeakReference;

public class LocationServiceManager {

    private static final String TAG = "LOCATION_MANAGER";
    private static LocationServiceManager locationServiceManager = null;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationManager locationManager;
    private WeakReference<Context> context;
    private Location mLastLocation;

    private LocationServiceManager(@NonNull Context context) {
        this.context = new WeakReference<>(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context.get());
    }

    public static LocationServiceManager getManager(@NonNull Context context) {
        if (locationServiceManager == null) {
            locationServiceManager = new LocationServiceManager(context.getApplicationContext());
        }
        return locationServiceManager;
    }

    public boolean isLocationModeOn() {
        return locationManager.isLocationEnabled();
    }

    public boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(context.get(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     */
    @SuppressWarnings("MissingPermission")
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener((Activity) context.get(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastLocation = task.getResult();

                        Log.i(TAG, String.valueOf(mLastLocation.getLatitude()));
                        Log.i(TAG, String.valueOf(mLastLocation.getLongitude()));
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.getException());
                        Toast.makeText(context.get(), context.get().getText(R.string.no_location_detected), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}

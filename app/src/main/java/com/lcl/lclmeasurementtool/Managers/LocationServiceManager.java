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
import com.lcl.lclmeasurementtool.R;

import java.lang.ref.WeakReference;

public class LocationServiceManager {

    private static final String TAG = "LOCATION_MANAGER";

    // the location service manager instance
    private static LocationServiceManager locationServiceManager = null;

    // the fused location client provided by Google Play Service
    private final FusedLocationProviderClient mFusedLocationClient;

    // the instance of the location manager
    private final LocationManager locationManager;

    // the weak reference of the current context of the Application
    private final WeakReference<Context> context;

    // the location object that contains the information of user's location
    private Location mLastLocation;

    /**
     * Initialize a Location Service Manager following the context
     *
     * @param context  the context of the current activity
     */
    private LocationServiceManager(@NonNull Context context) {
        this.context = new WeakReference<>(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context.get());
    }

    /**
     * Return a LocationService Manager instance following the given context.
     *
     * @param context  the context of the current activity
     * @return a location service manager instance
     */
    public static LocationServiceManager getManager(@NonNull Context context) {
        if (locationServiceManager == null) {
            locationServiceManager = new LocationServiceManager(context.getApplicationContext());
        }
        return locationServiceManager;
    }

    /**
     * Return whether the device's location mode is on.
     * @return whether the device's location mode is on.
     */
    public boolean isLocationModeOn() {
        return locationManager.isLocationEnabled();
    }

    /**
     * Return whether the device's location permission is granted.
     *
     * @return whether the device's location permission is granted
     */
    public boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(context.get(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Retrieve the last location from the device.
     * If last location is null, a new location request will be initiated.
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

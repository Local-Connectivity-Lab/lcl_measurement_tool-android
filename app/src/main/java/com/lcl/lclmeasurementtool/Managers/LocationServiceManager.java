package com.lcl.lclmeasurementtool.Managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.lcl.lclmeasurementtool.R;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class LocationServiceManager {

    private static final String TAG = "LOCATION_MANAGER";

    // the location service manager instance
    private static LocationServiceManager locationServiceManager = null;

    // the fused location client provided by Google Play Service
    private FusedLocationProviderClient mFusedLocationClient;

    // the instance of the location manager
    private LocationManager locationManager;

    // the weak reference of the current context of the Application
    private final WeakReference<Context> context;

    // the location object that contains the information of user's location
    private Location mLastLocation;

    private LocationCallback callback;

    /**
     * Initialize a Location Service Manager following the context
     *
     * @param context  the context of the current activity
     */
    private LocationServiceManager(@NonNull Context context) {
        this.context = new WeakReference<>(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Return a LocationService Manager instance following the given context.
     *
     * @param context  the context of the current activity
     * @return a location service manager instance
     */
    public static LocationServiceManager getManager(@NonNull Context context) {
        if (locationServiceManager == null) {
            locationServiceManager = new LocationServiceManager(context);
        }
        return locationServiceManager;
    }

    /**
     * Return whether the device's location mode is on.
     * @return whether the device's location mode is on.
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public boolean isLocationModeOn() {
        if (locationManager != null) {
            return locationManager.isLocationEnabled();
        }
        return false;
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
    public void getLastLocation(LocationUpdatesListener listener) {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener((Activity) this.context.get(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastLocation = task.getResult();
                        try {
                            listener.onUpdate(mLastLocation);
                        } catch (CertificateException | DecoderException | InvalidKeySpecException | InvalidKeyException | SignatureException | UnrecoverableEntryException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.getException());
                        Toast.makeText(this.context.get(), context.get().getText(R.string.no_location_detected), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressWarnings("MissingPermission")
    public void requestLocationUpdates(LocationUpdatesListener listener) {
        Criteria locationUpdateCriteria = new Criteria();
        locationUpdateCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        locationUpdateCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    listener.onUpdate(location);
                } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | UnrecoverableEntryException | SignatureException | InvalidKeyException | InvalidKeySpecException | DecoderException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.e(TAG, provider + " has been disabled");
            }
        };
        new Thread(() -> {
            Looper.prepare();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, (float) 10.0, locationListener, Looper.myLooper());
//          locationManager.requestLocationUpdates(5000, 10, locationUpdateCriteria, locationListener, Looper.myLooper());
            Looper.loop();
        }).start();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(LocationUpdatesListener listener) {
        LocationRequest locationRequest = LocationRequest
                .create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(5000)
                .setInterval(20000)
                .setSmallestDisplacement(5);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    // update the map
                    try {
                        listener.onUpdate(location);
                    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | UnrecoverableEntryException | SignatureException | InvalidKeyException | InvalidKeySpecException | DecoderException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
    }

    /**
     * Stop receiving location information.
     */
    public void stop() {
        mFusedLocationClient.removeLocationUpdates(callback);
        this.locationManager = null;
        this.mFusedLocationClient = null;
    }
}

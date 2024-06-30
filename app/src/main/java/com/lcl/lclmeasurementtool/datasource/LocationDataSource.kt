package com.lcl.lclmeasurementtool.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lcl.lclmeasurementtool.location.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class LocationDataSource @Inject constructor(
    @ApplicationContext private val context: Context
): LocationService {
    companion object {
        const val LOCATION_INTERVAL = 10000L
        const val TAG = "LocationDataSource"
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val currentLocationRequest = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).setDurationMillis(LOCATION_INTERVAL).build()


    @SuppressLint("MissingPermission")
    override fun lastLocation() = callbackFlow {
        if (!XXPermissions.isGranted(context, Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)) {
            XXPermissions
                .with(context)
                .permission(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
                .request { permissions, allGranted ->
                    if (!allGranted) {
                        TODO("Show message to the user")
                    }
                }
        }

        val callback = OnSuccessListener<Location> { task -> trySend(task) }

        fusedLocationProviderClient
            .lastLocation
            .addOnSuccessListener(callback)

        awaitClose {

        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() = callbackFlow<Location> {
        if (!XXPermissions.isGranted(context, Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)) {
            XXPermissions
                .with(context)
                .permission(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
                .request { permissions, allGranted ->
                    if (!allGranted) {
                        TODO("Show message to the user")
                    }
                }
        }
        val callback = OnSuccessListener<Location> { task -> trySend(task) }
        fusedLocationProviderClient.getCurrentLocation(currentLocationRequest, CancellationTokenSource().token).addOnSuccessListener(callback)
    }
}
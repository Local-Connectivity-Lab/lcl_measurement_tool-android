package com.lcl.lclmeasurementtool.datasource

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.lcl.lclmeasurementtool.model.datamodel.Site
import com.lcl.lclmeasurementtool.telephony.SignalStrengthMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SignalStrengthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
): SignalStrengthMonitor {

    private val telephonyManager = context.getSystemService<TelephonyManager>()

    @SuppressLint("MissingPermission")
    override fun getCellID(): String {
        return when (val info = telephonyManager?.allCellInfo?.firstOrNull()) {
            null -> "unknown"
            is CellInfoGsm -> {
                info.cellIdentity.cid.toString()
            }
            is CellInfoLte -> {
                info.cellIdentity.ci.toString()
            }

            is CellInfoCdma -> {
                val cellIdentity = info.cellIdentity
                String.format(
                    "%04x%04x%04x",
                    cellIdentity.systemId,
                    cellIdentity.networkId,
                    cellIdentity.basestationId
                )
            }

            is CellInfoWcdma -> {
                info.cellIdentity.cid.toString()
            }
            else -> "unknown"
        }
    }

    override fun getCellIDFromSite(latitude: Double, longitude: Double, sites: List<Site>): String? {
        // Find the nearest site
        val nearestSite = sites.minByOrNull { site ->
            calculateDistance(latitude, longitude, site.latitude, site.longitude)
        }
        
        // Check if point is within site boundaries or within reasonable distance
        nearestSite?.let { site ->
            val distance = calculateDistance(latitude, longitude, site.latitude, site.longitude)
            
            // If boundaries are defined, check if point is inside
            if (site.boundaries != null && site.boundaries.isNotEmpty()) {
                if (isPointInPolygon(latitude, longitude, site.boundaries)) {
                    return site.cellIds?.firstOrNull() ?: site.name
                }
            } else {
                // Otherwise use distance threshold (e.g., 500 meters)
                if (distance < 0.5) { // 500 meters in km
                    return site.cellIds?.firstOrNull() ?: site.name
                }
            }
        }
        
        return null
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun isPointInPolygon(latitude: Double, longitude: Double, polygon: List<List<Double>>): Boolean {
        var inside = false
        var j = polygon.size - 1
        
        for (i in polygon.indices) {
            val xi = polygon[i][0]
            val yi = polygon[i][1]
            val xj = polygon[j][0]
            val yj = polygon[j][1]
            
            val intersect = ((yi > longitude) != (yj > longitude)) &&
                    (latitude < (xj - xi) * (longitude - yi) / (yj - yi) + xi)
            if (intersect) inside = !inside
            j = i
        }
        
        return inside
    }

    @OptIn(FlowPreview::class)
    override val signalStrength = callbackFlow<SignalStrength> {
//        val telephonyManager = context.getSystemService<TelephonyManager>()
        val executor = Executors.newSingleThreadExecutor()

        if (Build.VERSION.SDK_INT >= 31) {
            val callback = object: TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(s: SignalStrength) {
                    channel.trySend(s)
                }
            }

            telephonyManager?.registerTelephonyCallback(executor, callback)

            awaitClose {
                telephonyManager?.unregisterTelephonyCallback(callback)
                executor.shutdown()
            }
        } else {
            @Suppress("OVERRIDE_DEPRECATION")
            val callback = object : PhoneStateListener(executor) {
                override fun onSignalStrengthsChanged(s: SignalStrength) {
                    super.onSignalStrengthsChanged(s)
                    channel.trySend(s)
                }
            }

            telephonyManager?.listen(callback, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

            awaitClose {
                telephonyManager?.listen(callback, 0)
                executor.shutdown()
            }
        }
    }.conflate().sample(5000L).distinctUntilChanged()
}

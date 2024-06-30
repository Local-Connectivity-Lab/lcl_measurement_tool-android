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
import com.lcl.lclmeasurementtool.telephony.SignalStrengthMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

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

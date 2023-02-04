package com.lcl.lclmeasurementtool.datasource

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class SignalStrengthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @OptIn(FlowPreview::class)
    val signalStrength = callbackFlow {
        val telephonyManager = context.getSystemService<TelephonyManager>()
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
                override fun onSignalStrengthsChanged(s: SignalStrength?) {
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
    }.conflate().sample(15000L).distinctUntilChanged()
}

package com.lcl.lclmeasurementtool.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.getSystemService
import com.lcl.lclmeasurementtool.constants.SimCardConstants
import com.lcl.lclmeasurementtool.networking.SimStateMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

class SimStateMonitorDataSource @Inject constructor(
    @ApplicationContext private val context: Context
): SimStateMonitor {
    companion object {
        const val SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    }

    override val isSimCardInserted: Flow<Boolean> = callbackFlow {
        val intentFilter = IntentFilter()
        val telephonyManager = context.getSystemService<TelephonyManager>()
        intentFilter.addAction(SIM_STATE_CHANGED)

        val receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == SIM_STATE_CHANGED) {
                    val states = intent.getStringExtra(SimCardConstants.INTENT_KEY_ICC_STATE)
                    if (states != SimCardConstants.INTENT_VALUE_ICC_LOADED &&
                        states != SimCardConstants.INTENT_VALUE_ICC_IMSI &&
                        states != SimCardConstants.INTENT_VALUE_ICC_READY) {
                        Log.d("SimStateDataSource", "simState doesnt match: $states")
                        channel.trySend(false)
                    } else {
                        channel.trySend(true)
                    }
                }
            }
        }

        context.registerReceiver(receiver, intentFilter)

        channel.trySend(telephonyManager.isSimCardInserted())

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.conflate()

    private fun TelephonyManager?.isSimCardInserted() = when(this) {
        null -> {
            Log.d("SimStateDataSource", "simState not initialized")
            false
        }
        else -> {
            Log.d("SimStateDataSource", "simState is $simState")
            simState == TelephonyManager.SIM_STATE_READY
        }
    }

}
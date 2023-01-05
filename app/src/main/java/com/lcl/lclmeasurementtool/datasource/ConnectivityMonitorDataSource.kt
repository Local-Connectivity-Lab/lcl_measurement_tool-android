package com.lcl.lclmeasurementtool.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.content.getSystemService
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

class ConnectivityMonitorDataSource @Inject constructor(
    @ApplicationContext private val context: Context) : NetworkMonitor{

    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        val callback = object: NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("ConnectivityMonitorDataSource", "available")
//                channel.trySend(connectivityManager.isCurrentlyConnected())
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d("ConnectivityMonitorDataSource", "lost")
//                channel.trySend(connectivityManager.isCurrentlyConnected())
                channel.trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasWIFI = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                Log.d("ConnectivityMonitorDataSource", "changed")
                Log.d("ConnectivityMonitorDataSource", "hasWIFI: $hasWIFI")
                channel.trySend(!hasWIFI)
//                channel.trySend(connectivityManager.isCurrentlyConnected())
            }
        }

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(),
            callback
        )

        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.conflate()

    @Suppress("DEPRECATION")
    private fun ConnectivityManager?.isCurrentlyConnected() = when(this) {
        null -> false
        else -> activeNetwork?.let(::getNetworkCapabilities)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
    }
}
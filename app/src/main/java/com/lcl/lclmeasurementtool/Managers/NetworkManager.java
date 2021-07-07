package com.lcl.lclmeasurementtool.Managers;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * NetworkManager manages all network related information, including but not limited to
 * connectivity states, active network information(wifi, cellular) and
 * listen to changes in network states.
 */
public class NetworkManager {

    // LOG TAG constant
    private static final String LOG_TAG = "NETWORK_MANAGER_TAG";

    // the connectivity manager object that keeps track of all information
    // related to phone's connectivyt states.
    private ConnectivityManager connectivityManager;

    // the network capabilities object that stores everything that the
    // current device supports with regards to networking.
    private NetworkCapabilities capabilities;

    // boolean value that keeps track of the cellular connectivity state.
    private boolean isCellularConnected;

    /**
     * Initialize a Network Manager object following the context of current device.
     * @param context the Context object of the current device
     */
    public NetworkManager(Context context) {
        this.connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = this.connectivityManager.getActiveNetwork();
        this.capabilities = this.connectivityManager.getNetworkCapabilities(network);
        this.isCellularConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    /**
     * Returns the cellular connectivity state of the current device.
     * @return true if the current device is connected to the internet via cellular; false otherwise.
     */
    public boolean isCellularConnected() {
        return isCellularConnected;
    }
}

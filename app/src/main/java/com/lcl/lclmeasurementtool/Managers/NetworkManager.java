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

    private static final String LOG_TAG = "NETWORK_MANAGER_TAG";

    // TODO: finish comment
    private ConnectivityManager connectivityManager;
    private NetworkCapabilities capabilities;
    private boolean isCellularConnected;

    /**
     * TODO: finish comment
     * @param context
     */
    public NetworkManager(Context context) {
        this.connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = this.connectivityManager.getActiveNetwork();
        this.capabilities = this.connectivityManager.getNetworkCapabilities(network);
        this.isCellularConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    public boolean isCellularConnected() {
        return isCellularConnected;
    }
}

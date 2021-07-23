package com.lcl.lclmeasurementtool.Managers;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;


/**
 * NetworkManager manages all network related information, including but not limited to
 * connectivity states, active network information(wifi, cellular) and
 * listen to changes in network states.
 *
 * @see <a href="https://developer.android.com/training/basics/network-ops/reading-network-state#java">reading network state</a>
 */
public class NetworkManager {

    // LOG TAG constant
    private static final String TAG = "NETWORK_MANAGER_TAG";

    private static NetworkManager networkManager = null;

    // A List of registered ColorChangeListeners
    private final List<NetworkChangeListener> mNetworkChangeListeners;

    // A Network Callback object
    private ConnectivityManager.NetworkCallback networkCallback;

    // the connectivity manager object that keeps track of all information
    // related to phone's connectivity states.
    private final ConnectivityManager connectivityManager;

    // current device supports with regards to networking.
    private final NetworkCapabilities capabilities;

    /**
     * Initialize a Network Manager object following the context of current device.
     * @param context the Context object of the current device
     */
    private NetworkManager(@NonNull Context context) {
        this.connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // the network object that encapsulates all info regarding Network
        Network network = this.connectivityManager.getActiveNetwork();
        this.capabilities = this.connectivityManager.getNetworkCapabilities(network);
        this.mNetworkChangeListeners = new ArrayList<>();
    }

    /**
     * Retrieve the network manager object from current context.
     * @return a network manager
     */
    public static NetworkManager getManager(@NonNull Context context) {
        if (networkManager == null) {
            networkManager = new NetworkManager(context);
        }

        return networkManager;
    }

    /**
     * Registers a new listener
     *
     * @param networkChangeListener a new listener (should not be null).
     */
    public void addNetworkChangeListener(@NonNull NetworkChangeListener networkChangeListener) {
        this.mNetworkChangeListeners.add(networkChangeListener);

        NetworkRequest request = new NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        this.networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.i(TAG, "current network is " + network);
                mNetworkChangeListeners.forEach(NetworkChangeListener::onAvailable);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                Log.e(TAG, "The default network changed capabilities: " + networkCapabilities);
                mNetworkChangeListeners.forEach(l -> l.onCellularNetworkChanged(
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                ));
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.e(TAG, "The default network lost. Previous one is " + network);
                mNetworkChangeListeners.forEach(NetworkChangeListener::onLost);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.e(TAG, "The default network is unavailable");
                mNetworkChangeListeners.forEach(NetworkChangeListener::onUnavailable);
            }
        };

        this.connectivityManager.registerNetworkCallback(request, this.networkCallback);
    }

    /**
     * Remove a specified listener
     *
     * @param networkChangeListener the listener to be removed (should not be null)
     */
    public void removeNetworkChangeListener(@NonNull NetworkChangeListener networkChangeListener) {
        this.mNetworkChangeListeners.remove(networkChangeListener);
    }

    /**
     * Remove all registered listeners
     */
    public void removeAllNetworkChangeListeners() {
        this.mNetworkChangeListeners.clear();
        this.connectivityManager.unregisterNetworkCallback(this.networkCallback);
    }

    /**
     * Returns the current cellular connectivity state of the current device when this method gets called.
     * @return true if the current device is connected to the internet via cellular; false otherwise.
     */
    public boolean isCellularConnected() {
        return this.capabilities != null &&
                this.capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    public int getLinkDownstreamBandwidthKbps() {
        if (this.capabilities != null) {
            return this.capabilities.getLinkDownstreamBandwidthKbps();
        }

        return 0;
    }

    public int getLinkUpstreamBandwidthKbps() {
        if (this.capabilities != null) {
            return this.capabilities.getLinkUpstreamBandwidthKbps();
        }

        return 0;
    }
}

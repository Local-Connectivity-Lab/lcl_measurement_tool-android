package com.lcl.lclmeasurementtool.Managers;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

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

    // Registered NetworkChangeListener for cellular and WIFI
    private NetworkChangeListener mCellularNetworkChangeListener;
    private NetworkChangeListener mDefaultNetworkChangeListener;


    // Network Callback object
    private ConnectivityManager.NetworkCallback cellularNetworkCallback;
    private ConnectivityManager.NetworkCallback defaultNetworkCallback;

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
        mCellularNetworkChangeListener = null;
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
     * Registers new listeners
     *
     * @param cellularNetworkChangeListener    a new listener (should not be null) listens to cellular network changes.
     * @param defaultNetworkChangeListener     a new listener (should not be null) listens to default network changes.
     */
    public void addNetworkChangeListener(@NonNull NetworkChangeListener cellularNetworkChangeListener, @NonNull NetworkChangeListener defaultNetworkChangeListener) {
        this.mCellularNetworkChangeListener = cellularNetworkChangeListener;
        this.mDefaultNetworkChangeListener = defaultNetworkChangeListener;

        NetworkRequest cellularRequest = new NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        this.cellularNetworkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                mCellularNetworkChangeListener.onAvailable();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                mCellularNetworkChangeListener.onCellularNetworkChanged(capabilities);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                mCellularNetworkChangeListener.onLost();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                mCellularNetworkChangeListener.onUnavailable();
            }
        };

        this.defaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.e(TAG, "wifi is on");
                    mDefaultNetworkChangeListener.onAvailable();
                } else {
                    Log.i(TAG, "cellular is on");
                    mCellularNetworkChangeListener.onAvailable();
                }
            }
        };

        this.connectivityManager.registerNetworkCallback(cellularRequest, this.cellularNetworkCallback);
        this.connectivityManager.registerDefaultNetworkCallback(this.defaultNetworkCallback);
    }

    /**
     * Remove a specified listener
     */
    public void removeNetworkChangeListener() {
        this.mCellularNetworkChangeListener = null;
        this.mDefaultNetworkChangeListener = null;
    }

    /**
     * Remove all registered listeners and unregister callbacks
     */
    public void stopListenToNetworkChanges() {
        this.mCellularNetworkChangeListener = null;
        this.mDefaultNetworkChangeListener = null;
        this.connectivityManager.unregisterNetworkCallback(this.cellularNetworkCallback);
        this.connectivityManager.unregisterNetworkCallback(this.defaultNetworkCallback);
    }

    /**
     * Returns the current cellular connectivity state of the current device when this method gets called.
     * @return true if the current device is connected to the internet via cellular; false otherwise.
     */
    public boolean isCellularConnected() {
        return this.capabilities != null &&
                this.capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    /**
     * Retrieve the downstream bandwidth in Kbps
     *
     * @return the downstream bandwidth in Kbps as an integer. If no cellular capability, return 0.
     */
    public int getLinkDownstreamBandwidthKbps() {
        if (this.capabilities != null) {
            return this.capabilities.getLinkDownstreamBandwidthKbps();
        }

        return 0;
    }

    /**
     * Retrieve the upstream bandwidth in Kbps
     *
     * @return the upstream bandwidth in Kbps as an integer. If no cellular capability, return 0.
     */
    public int getLinkUpstreamBandwidthKbps() {
        if (this.capabilities != null) {
            return this.capabilities.getLinkUpstreamBandwidthKbps();
        }

        return 0;
    }
}

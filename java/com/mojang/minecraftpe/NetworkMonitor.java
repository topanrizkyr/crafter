package com.mojang.minecraftpe;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import java.util.HashMap;
import java.util.HashSet;

public class NetworkMonitor {
    private static final int NETWORK_CATEGORY_ETHERNET = 0;
    private static final int NETWORK_CATEGORY_OTHER = 2;
    private static final int NETWORK_CATEGORY_WIFI = 1;
    private HashMap<Integer, HashSet<Network>> mAvailableNetworksPerCategory;
    private Context mContext;

    private native void nativeUpdateNetworkStatus(boolean z, boolean z2, boolean z3);

    public NetworkMonitor(Context context) {
        this.mContext = context;
        HashMap<Integer, HashSet<Network>> hashMap = new HashMap<>();
        this.mAvailableNetworksPerCategory = hashMap;
        hashMap.put(0, new HashSet<>());
        this.mAvailableNetworksPerCategory.put(1, new HashSet<>());
        this.mAvailableNetworksPerCategory.put(2, new HashSet<>());
        _addNetworkCallbacksForTransport(3, 0);
        _addNetworkCallbacksForTransport(1, 1);
        _addNetworkCallbacksForTransport(0, 2);
        _addNetworkCallbacksForTransport(2, 2);
        if (Build.VERSION.SDK_INT >= 31) {
            _addNetworkCallbacksForTransport(8, 2);
        }
    }

    private void _addNetworkCallbacksForTransport(int i, final int i2) {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(_createNetworkRequestForTransport(i), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                ((HashSet) NetworkMonitor.this.mAvailableNetworksPerCategory.get(Integer.valueOf(i2))).add(network);
                NetworkMonitor.this._updateStatus();
            }

            @Override
            public void onLost(Network network) {
                ((HashSet) NetworkMonitor.this.mAvailableNetworksPerCategory.get(Integer.valueOf(i2))).remove(network);
                NetworkMonitor.this._updateStatus();
            }
        });
    }

    private NetworkRequest _createNetworkRequestForTransport(int i) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addCapability(16);
        builder.addTransportType(i);
        return builder.build();
    }

    private void _updateStatus() {
        nativeUpdateNetworkStatus(!this.mAvailableNetworksPerCategory.get(0).isEmpty(), !this.mAvailableNetworksPerCategory.get(1).isEmpty(), true ^ this.mAvailableNetworksPerCategory.get(2).isEmpty());
    }
}

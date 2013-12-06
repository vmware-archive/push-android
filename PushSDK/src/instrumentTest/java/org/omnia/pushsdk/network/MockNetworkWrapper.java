package org.omnia.pushsdk.network;

import android.content.Context;

import com.xtreme.network.INetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestLauncher;

public class MockNetworkWrapper implements NetworkWrapper {

    private INetworkRequestLauncher networkRequestLauncher;
    private final boolean isNetworkAvailable;

    public MockNetworkWrapper() {
        isNetworkAvailable = true;
    }

    public MockNetworkWrapper(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    @Override
    public INetworkRequestLauncher getNetworkRequestLauncher() {
        if (networkRequestLauncher == null) {
            networkRequestLauncher = new MockNetworkRequestLauncher();
        }
        return networkRequestLauncher;
    }

    @Override
    public boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable;
    }
}

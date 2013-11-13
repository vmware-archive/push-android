package com.gopivotal.pushlib.network;

import android.content.Context;

import com.xtreme.network.INetworkRequestLauncher;
import com.xtreme.network.NetworkRequestLauncher;

public class NetworkWrapperImpl implements NetworkWrapper {

    @Override
    public INetworkRequestLauncher getNetworkRequestLauncher() {
        return NetworkRequestLauncher.getInstance();
    }

    @Override
    public boolean isNetworkAvailable(Context context) {
        return NetworkUtil.getInstance(context).isNetworkAvailable();
    }

}

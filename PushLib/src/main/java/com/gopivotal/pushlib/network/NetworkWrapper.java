package com.gopivotal.pushlib.network;

import android.content.Context;

import com.xtreme.network.INetworkRequestLauncher;

public interface NetworkWrapper {

    INetworkRequestLauncher getNetworkRequestLauncher();

    boolean isNetworkAvailable(Context context);
}

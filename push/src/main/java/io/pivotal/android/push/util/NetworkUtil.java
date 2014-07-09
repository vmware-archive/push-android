/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * General purpose network helper methods.
 *
 */
public class NetworkUtil {

    private static NetworkUtil instance;
    private Context applicationContext;

    public static NetworkUtil getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new NetworkUtil(applicationContext);
        }
        return instance;
    }

    private NetworkUtil(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Checks if the device thinks it is connected to an active network.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
    }
}

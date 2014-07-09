/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkWrapperImpl implements NetworkWrapper {

    @Override
    public boolean isNetworkAvailable(Context context) {
        return NetworkUtil.getInstance(context).isNetworkAvailable();
    }

    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

}

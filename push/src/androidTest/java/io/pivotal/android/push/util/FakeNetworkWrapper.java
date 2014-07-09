/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FakeNetworkWrapper implements NetworkWrapper {

    private final boolean isNetworkAvailable;

    public FakeNetworkWrapper() {
        isNetworkAvailable = true;
    }

    @Override
    public boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable;
    }

    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return new FakeHttpURLConnection(url);
    }
}

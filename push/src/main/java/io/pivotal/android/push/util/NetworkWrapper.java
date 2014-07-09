/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface NetworkWrapper {

    boolean isNetworkAvailable(Context context);

    HttpURLConnection getHttpURLConnection(URL url) throws IOException;
}

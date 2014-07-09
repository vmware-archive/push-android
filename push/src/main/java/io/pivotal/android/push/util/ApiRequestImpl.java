/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiRequestImpl {

    protected NetworkWrapper networkWrapper;

    protected ApiRequestImpl(NetworkWrapper networkWrapper) {
        verifyArguments(networkWrapper);
        saveArguments(networkWrapper);
    }

    private void verifyArguments(NetworkWrapper networkWrapper) {
        if (networkWrapper == null) {
            throw new IllegalArgumentException("networkWrapper may not be null");
        }
    }

    private void saveArguments(NetworkWrapper networkWrapper) {
        this.networkWrapper = networkWrapper;
    }

    protected HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        final HttpURLConnection urlConnection = networkWrapper.getHttpURLConnection(url);
        urlConnection.setReadTimeout(60000);
        urlConnection.setConnectTimeout(60000);
        urlConnection.setChunkedStreamingMode(0);
        return urlConnection;
    }

    protected void writeOutput(String requestBodyData, OutputStream outputStream) throws IOException {
        final byte[] bytes = requestBodyData.getBytes();
        for (byte b : bytes) {
            outputStream.write(b);
        }
        outputStream.close();
    }

    protected String readInput(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];

        while (true) {
            final int numberBytesRead = inputStream.read(buffer);
            if (numberBytesRead < 0) {
                break;
            }
            byteArrayOutputStream.write(buffer, 0, numberBytesRead);
        }

        final String str = new String(byteArrayOutputStream.toByteArray());
        return str;
    }

    protected boolean isFailureStatusCode(int statusCode) {
        return (statusCode < 200 || statusCode >= 300);
    }

}

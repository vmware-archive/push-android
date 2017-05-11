/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public class FakeHttpURLConnection extends HttpsURLConnection {

    private static int responseCode;
    private static String responseData;
    private static String receivedHttpMethod;
    private static IOException connectionException;
    private static boolean willThrowConnectionException;
    private static Map<String, String> requestProperties;
    private static URL url;
    private static ByteArrayOutputStream outputStream;
    private static boolean didCallSetSSLSocketFactory;

    protected FakeHttpURLConnection(URL url) {
        super(url);
        FakeHttpURLConnection.url = url;
        FakeHttpURLConnection.requestProperties = new HashMap<>();
    }

    public static void setResponseCode(int responseCode) {
        FakeHttpURLConnection.responseCode = responseCode;
    }

    public static void setResponseData(String responseData) {
        FakeHttpURLConnection.responseData = responseData;
    }

    public static void setConnectionException(IOException connectionException) {
        FakeHttpURLConnection.connectionException = connectionException;
    }

    public static void willThrowConnectionException(boolean willThrowConnectionException) {
        FakeHttpURLConnection.willThrowConnectionException = willThrowConnectionException;
    }

    public static String getReceivedHttpMethod() {
        return FakeHttpURLConnection.receivedHttpMethod;
    }

    public static Map<String, String> getRequestPropertiesMap() {
        return FakeHttpURLConnection.requestProperties;
    }

    public static URL getReceivedURL() {
        return FakeHttpURLConnection.url;
    }

    public static void reset() {
        FakeHttpURLConnection.url = null;
        FakeHttpURLConnection.responseCode = 0;
        FakeHttpURLConnection.responseData = null;
        FakeHttpURLConnection.requestProperties = null;
        FakeHttpURLConnection.receivedHttpMethod = null;
        FakeHttpURLConnection.connectionException = null;
        FakeHttpURLConnection.outputStream = null;
        FakeHttpURLConnection.willThrowConnectionException = false;
        FakeHttpURLConnection.didCallSetSSLSocketFactory = false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        if (FakeHttpURLConnection.willThrowConnectionException) {
            throw FakeHttpURLConnection.connectionException;
        }
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        FakeHttpURLConnection.receivedHttpMethod = method;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        FakeHttpURLConnection.outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (responseCode != 200) {
            throw new FileNotFoundException();
        }

        if (responseData != null) {
            return new ByteArrayInputStream(responseData.getBytes());
        } else {
            return null;
        }
    }

    @Override
    public int getResponseCode() {
        return FakeHttpURLConnection.responseCode;
    }

    @Override
    public void addRequestProperty(String field, String newValue) {
        super.addRequestProperty(field, newValue);
        FakeHttpURLConnection.requestProperties.put(field, newValue);
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sf) {
        super.setSSLSocketFactory(sf);
        FakeHttpURLConnection.didCallSetSSLSocketFactory = true;
    }

    @Override
    public String getCipherSuite() {
        return null;
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return new Certificate[0];
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return new Certificate[0];
    }

    public static byte[] getRequestData() {
        return FakeHttpURLConnection.outputStream.toByteArray();
    }

    public static boolean didCallSetSSLSocketFactory() {
        return FakeHttpURLConnection.didCallSetSSLSocketFactory;
    }
}

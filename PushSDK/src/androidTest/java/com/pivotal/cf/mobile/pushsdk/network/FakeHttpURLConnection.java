package com.pivotal.cf.mobile.pushsdk.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class FakeHttpURLConnection extends HttpURLConnection {

    private static int responseCode;
    private static String responseData;
    private static String receivedHttpMethod;
    private static IOException connectionException;
    private static boolean willThrowConnectionException;
    private static URL url;

    protected FakeHttpURLConnection(URL url) {
        super(url);
        FakeHttpURLConnection.url = url;
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

    public static URL getReceivedURL() {
        return FakeHttpURLConnection.url;
    }

    public static void reset() {
        FakeHttpURLConnection.url = null;
        FakeHttpURLConnection.responseCode = 0;
        FakeHttpURLConnection.responseData = null;
        FakeHttpURLConnection.receivedHttpMethod = null;
        FakeHttpURLConnection.connectionException = null;
        FakeHttpURLConnection.willThrowConnectionException = false;
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
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData.getBytes());
        return inputStream;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

}

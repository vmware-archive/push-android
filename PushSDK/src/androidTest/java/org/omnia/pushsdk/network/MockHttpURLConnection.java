package org.omnia.pushsdk.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockHttpURLConnection extends HttpURLConnection {

    private static int responseCode;
    private static String responseData;
    private static IOException connectionException;
    private static boolean willThrowConnectionException;

    protected MockHttpURLConnection(URL url) {
        super(url);
    }

    public static void setResponseCode(int responseCode) {
        MockHttpURLConnection.responseCode = responseCode;
    }

    public static void setResponseData(String responseData) {
        MockHttpURLConnection.responseData = responseData;
    }

    public static void setConnectionException(IOException connectionException) {
        MockHttpURLConnection.connectionException = connectionException;
    }

    public static void willThrowConnectionException(boolean willThrowConnectionException) {
        MockHttpURLConnection.willThrowConnectionException = willThrowConnectionException;
    }

    public static void reset() {
        MockHttpURLConnection.responseCode = 0;
        MockHttpURLConnection.responseData = null;
        MockHttpURLConnection.connectionException = null;
        MockHttpURLConnection.willThrowConnectionException = false;
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
        if (MockHttpURLConnection.willThrowConnectionException) {
            throw MockHttpURLConnection.connectionException;
        }
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

package com.xtreme.network;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;

public class MockNetworkResponse extends NetworkResponse {

    public static final int NO_STATUS_CODE = -1;

    private final InputStream inputStream;
    private final StatusLine statusLine;

    MockNetworkResponse(InputStream inputStream, final int statusCode) {
        super(null);
        this.inputStream = inputStream;
        if (statusCode != NO_STATUS_CODE) {
            this.statusLine = new StatusLine() {

                @Override
                public int getStatusCode() {
                    return statusCode;
                }

                @Override
                public String getReasonPhrase() {
                    return null;
                }

                @Override
                public ProtocolVersion getProtocolVersion() {
                    return null;
                }
            };
        } else {
            this.statusLine = null;
        }
    }

    @Override
    public StatusLine getStatus() {
        return statusLine;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }
}

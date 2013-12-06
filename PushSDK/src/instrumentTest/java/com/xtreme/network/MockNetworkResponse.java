/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

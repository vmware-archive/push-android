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

package com.pivotal.cf.mobile.pushsdk.gcm;

import android.content.Context;

import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.io.IOException;

/**
 * API request for unregistering a device with the Google Cloud Messaging (GCM)
 */
public class GcmUnregistrationApiRequestImpl implements GcmUnregistrationApiRequest {

    private Context context;
    private GcmProvider gcmProvider;
    private GcmUnregistrationListener listener;

    public GcmUnregistrationApiRequestImpl(Context context, GcmProvider gcmProvider) {
        verifyArguments(context, gcmProvider);
        saveArguments(context, gcmProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
    }

    private void saveArguments(Context context, GcmProvider gcmProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
    }

    @Override
    public void startUnregistration(GcmUnregistrationListener listener) {
        verifyUnregistrationArguments(listener);
        saveUnregistrationArguments(listener);
        executeUnregistration();
    }

    private void verifyUnregistrationArguments(GcmUnregistrationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void saveUnregistrationArguments(GcmUnregistrationListener listener) {
        this.listener = listener;
    }

    private void executeUnregistration() {
        try {
            gcmProvider.unregister();
            PushLibLogger.i("Device unregistered with GCM.");
            listener.onGcmUnregistrationComplete();

        } catch (IOException ex) {
            PushLibLogger.ex("Error unregistering device with GCM:", ex);
            listener.onGcmUnregistrationFailed(ex.getLocalizedMessage());
        }
    }

    @Override
    public GcmUnregistrationApiRequest copy() {
        return new GcmUnregistrationApiRequestImpl(context, gcmProvider);
    }
}

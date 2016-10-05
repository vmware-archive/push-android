/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public class FakeGcmUnregistrationApiRequest implements GcmUnregistrationApiRequest {
    private FakeGcmProvider gcmProvider;

    public FakeGcmUnregistrationApiRequest(FakeGcmProvider gcmProvider) {
        this.gcmProvider = gcmProvider;
    }

    @Override
    public void startUnregistration(GcmUnregistrationListener listener) {
        try {
            gcmProvider.unregister();
            listener.onGcmUnregistrationComplete();
        } catch (Exception e) {
            listener.onGcmUnregistrationFailed(e.getLocalizedMessage());
        }
    }

    @Override
    public GcmUnregistrationApiRequest copy() {
        return new FakeGcmUnregistrationApiRequest(gcmProvider);
    }
}

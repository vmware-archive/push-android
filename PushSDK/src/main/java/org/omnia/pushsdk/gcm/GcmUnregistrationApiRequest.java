package org.omnia.pushsdk.gcm;

public interface GcmUnregistrationApiRequest {
    void startUnregistration(GcmUnregistrationListener listener);
    GcmUnregistrationApiRequest copy();
}

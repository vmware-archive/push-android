package com.gopivotal.pushlib.gcm;

public interface GcmUnregistrationApiRequest {
    void startUnregistration(GcmUnregistrationListener listener);
    GcmUnregistrationApiRequest copy();
}

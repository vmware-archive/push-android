package org.omnia.pushsdk.gcm;

public class GcmRegistrationApiRequestProvider {

    private final GcmRegistrationApiRequest dummyRequest;

    public GcmRegistrationApiRequestProvider(GcmRegistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public GcmRegistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}

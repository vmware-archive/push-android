package com.gopivotal.pushlib.backend;

import com.gopivotal.pushlib.PushLibParameters;

public class FakeBackEndRegistrationApiRequest implements BackEndRegistrationApiRequest {

    private final FakeBackEndRegistrationApiRequest originatingRequest;
    private final String backEndDeviceRegistrationId;
    private final boolean willBeSuccessfulRequest;
    private boolean wasRegisterCalled = false;

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationId) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = true;
    }

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationId, boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakeBackEndRegistrationApiRequest(FakeBackEndRegistrationApiRequest originatingRequest, String backEndDeviceRegistrationId, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startDeviceRegistration(String gcmDeviceRegistrationId, PushLibParameters parameters, BackEndRegistrationListener listener) {
        wasRegisterCalled = true;
        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationId);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end registration failed fakely");
        }
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new FakeBackEndRegistrationApiRequest(this, backEndDeviceRegistrationId, willBeSuccessfulRequest);
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }
}

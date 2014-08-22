/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;

public class FakeBackEndRegistrationApiRequest implements BackEndRegistrationApiRequest {

    private final FakeBackEndRegistrationApiRequest originatingRequest;
    private final String backEndDeviceRegistrationIdFromServer;
    private final boolean willBeSuccessfulRequest;
    private boolean wasRegisterCalled = false;
    private boolean isNewRegistration;
    private boolean isUpdateRegistration;

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationIdFromServer) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = true;
    }

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakeBackEndRegistrationApiRequest(FakeBackEndRegistrationApiRequest originatingRequest, String backEndDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startNewDeviceRegistration(String gcmDeviceRegistrationId,
                                           Set<String> savedTags,
                                           RegistrationParameters parameters,
                                           BackEndRegistrationListener listener) {

        wasRegisterCalled = true;
        isNewRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isNewRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationIdFromServer);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end new registration failed fakely");
        }
    }

    @Override
    public void startUpdateDeviceRegistration(String gcmDeviceRegistrationId,
                                              String previousBackEndDeviceRegistrationId,
                                              Set<String> savedTags,
                                              RegistrationParameters parameters,
                                              BackEndRegistrationListener listener) {
        wasRegisterCalled = true;
        isUpdateRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isUpdateRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationIdFromServer);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end update registration failed fakely");
        }
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new FakeBackEndRegistrationApiRequest(this, backEndDeviceRegistrationIdFromServer, willBeSuccessfulRequest);
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    public boolean isNewRegistration() {
        return isNewRegistration;
    }

    public boolean isUpdateRegistration() {
        return isUpdateRegistration;
    }
}

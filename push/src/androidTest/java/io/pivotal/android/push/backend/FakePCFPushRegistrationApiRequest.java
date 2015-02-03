/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;

public class FakePCFPushRegistrationApiRequest implements PCFPushRegistrationApiRequest {

    private final FakePCFPushRegistrationApiRequest originatingRequest;
    private final String pcfPushDeviceRegistrationIdFromServer;
    private final boolean willBeSuccessfulRequest;
    private boolean wasRegisterCalled = false;
    private boolean isNewRegistration;
    private boolean isUpdateRegistration;

    public FakePCFPushRegistrationApiRequest(String pcfPushDeviceRegistrationIdFromServer) {
        this.originatingRequest = null;
        this.pcfPushDeviceRegistrationIdFromServer = pcfPushDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = true;
    }

    public FakePCFPushRegistrationApiRequest(String pcfPushDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.pcfPushDeviceRegistrationIdFromServer = pcfPushDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakePCFPushRegistrationApiRequest(FakePCFPushRegistrationApiRequest originatingRequest, String pcfPushDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.pcfPushDeviceRegistrationIdFromServer = pcfPushDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startNewDeviceRegistration(String gcmDeviceRegistrationId,
                                           Set<String> savedTags,
                                           RegistrationParameters parameters,
                                           PCFPushRegistrationListener listener) {

        wasRegisterCalled = true;
        isNewRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isNewRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onPCFPushRegistrationSuccess(pcfPushDeviceRegistrationIdFromServer);
        } else {
            listener.onPCFPushRegistrationFailed("Fake PCF Push new registration failed fakely");
        }
    }

    @Override
    public void startUpdateDeviceRegistration(String gcmDeviceRegistrationId,
                                              String previousPCFPushDeviceRegistrationId,
                                              Set<String> savedTags,
                                              RegistrationParameters parameters,
                                              PCFPushRegistrationListener listener) {
        wasRegisterCalled = true;
        isUpdateRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isUpdateRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onPCFPushRegistrationSuccess(pcfPushDeviceRegistrationIdFromServer);
        } else {
            listener.onPCFPushRegistrationFailed("Fake PCF Push update registration failed fakely");
        }
    }

    @Override
    public PCFPushRegistrationApiRequest copy() {
        return new FakePCFPushRegistrationApiRequest(this, pcfPushDeviceRegistrationIdFromServer, willBeSuccessfulRequest);
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

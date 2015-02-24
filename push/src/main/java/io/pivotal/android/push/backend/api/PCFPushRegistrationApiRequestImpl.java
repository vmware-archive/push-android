/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.model.api.BasePCFPushApiRegistrationRequestData;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPostRequestData;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPutRequestData;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationResponseData;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.TagsHelper;
import io.pivotal.android.push.util.Util;

/**
 * API request for registering a device with the Pivotal CF Mobile Services Push server.
 */
public class PCFPushRegistrationApiRequestImpl extends ApiRequestImpl implements PCFPushRegistrationApiRequest {

    private Context context;

    public PCFPushRegistrationApiRequestImpl(Context context, NetworkWrapper networkWrapper) {
        super(networkWrapper);
        verifyArguments(context);
        saveArguments(context);
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        this.context = context;
    }

    @Override
    public void startNewDeviceRegistration(String gcmDeviceRegistrationId,
                                           Set<String> savedTags,
                                           PushParameters parameters,
                                           PCFPushRegistrationListener listener) {

        verifyNewRegistrationArguments(gcmDeviceRegistrationId, parameters, listener);
        handleRequest(gcmDeviceRegistrationId, null, savedTags, parameters, listener, false);
    }

    @Override
    public void startUpdateDeviceRegistration(String gcmDeviceRegistrationId,
                                              String pcfPushDeviceRegistrationId,
                                              Set<String> savedTags,
                                              PushParameters parameters,
                                              PCFPushRegistrationListener listener) {

        verifyUpdateRegistrationArguments(gcmDeviceRegistrationId, pcfPushDeviceRegistrationId, parameters, listener);
        handleRequest(gcmDeviceRegistrationId, pcfPushDeviceRegistrationId, savedTags, parameters, listener, true);
    }

    private void verifyNewRegistrationArguments(String gcmDeviceRegistrationId,
                                                PushParameters parameters,
                                                PCFPushRegistrationListener listener) {

        if (gcmDeviceRegistrationId == null) {
            throw new IllegalArgumentException("gcmDeviceRegistrationId may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void verifyUpdateRegistrationArguments(String gcmDeviceRegistrationId,
                                                   String pcfPushDeviceRegistrationId,
                                                   PushParameters parameters,
                                                   PCFPushRegistrationListener listener) {

        verifyNewRegistrationArguments(gcmDeviceRegistrationId, parameters, listener);
        if (pcfPushDeviceRegistrationId == null) {
            throw new IllegalArgumentException("pcfPushDeviceRegistrationId may not be null");
        }
    }

    private void handleRequest(String gcmDeviceRegistrationId,
                               String previousPCFPushDeviceRegistrationId,
                               Set<String> savedTags,
                               PushParameters parameters,
                               PCFPushRegistrationListener listener,
                               boolean isUpdate) {

        OutputStream outputStream = null;
        try {
            final URL url = getURL(isUpdate, previousPCFPushDeviceRegistrationId, parameters);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod(getRequestMethod(isUpdate));
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue(parameters));
            urlConnection.connect();

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());

            final String requestBodyData = getRequestBodyData(
                    gcmDeviceRegistrationId,
                    savedTags,
                    parameters,
                    isUpdate);

            Logger.v("Making network request to register this device with the PCF Push server: " + requestBodyData);
            writeOutput(requestBodyData, outputStream);

            final int statusCode = urlConnection.getResponseCode();

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final String responseString = readInput(inputStream);

            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, responseString, listener);

        } catch (Exception e) {
            Logger.ex("PCF Push device registration attempt failed", e);
            listener.onPCFPushRegistrationFailed(e.getLocalizedMessage());
        }

        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private URL getURL(boolean isUpdate,
                       String previousPCFPushDeviceRegistrationId,
                       PushParameters parameters) throws MalformedURLException {

        if (isUpdate) {
            return new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_REGISTRATION_REQUEST_ENDPOINT + "/" +  previousPCFPushDeviceRegistrationId);
        } else {
            return new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_REGISTRATION_REQUEST_ENDPOINT);
        }
    }

    private String getRequestMethod(boolean isUpdate) {
        if (isUpdate) {
            return "PUT";
        } else {
            return "POST";
        }
    }

    public void onSuccessfulNetworkRequest(int statusCode,
                                           String responseString,
                                           final PCFPushRegistrationListener listener) {

        if (isFailureStatusCode(statusCode)) {
            Logger.e("PCF Push server registration failed: server returned HTTP status " + statusCode);
            listener.onPCFPushRegistrationFailed("PCF Push server returned HTTP status " + statusCode);
            return;
        }

        if (responseString == null) {
            Logger.e("PCF Push server registration failed: server response empty");
            listener.onPCFPushRegistrationFailed("PCF Push server response empty");
            return;
        }

        final Gson gson = new Gson();
        final PCFPushApiRegistrationResponseData responseData;
        try {
            responseData = gson.fromJson(responseString, PCFPushApiRegistrationResponseData.class);
            if (responseData == null) {
                throw new Exception("unable to parse server response");
            }
        } catch (Exception e) {
            Logger.e("PCF Push server registration failed: " + e.getLocalizedMessage());
            listener.onPCFPushRegistrationFailed(e.getLocalizedMessage());
            return;
        }

        final String deviceUuid = responseData.getDeviceUuid();
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            Logger.e("PCF Push server registration failed: did not return device_uuid");
            listener.onPCFPushRegistrationFailed("PCF Push server did not return device_uuid");
            return;
        }

        Util.saveIdToFilesystem(context, deviceUuid, "device_uuid");

        Logger.i("PCF Push Server registration succeeded.");
        listener.onPCFPushRegistrationSuccess(deviceUuid);
    }

    private String getRequestBodyData(String deviceRegistrationId,
                                      Set<String> savedTags,
                                      PushParameters parameters,
                                      boolean isUpdate) {


        final BasePCFPushApiRegistrationRequestData data = getPCFPushApiRegistrationRequestData(
                deviceRegistrationId,
                savedTags,
                parameters,
                isUpdate);

        final Gson gson = new Gson();
        return gson.toJson(data);
    }

    private BasePCFPushApiRegistrationRequestData getPCFPushApiRegistrationRequestData(String deviceRegistrationId,
                                                                                       Set<String> savedTags,
                                                                                       PushParameters parameters,
                                                                                       boolean isUpdate) {

        final BasePCFPushApiRegistrationRequestData data;
        if (isUpdate) {
            final PCFPushApiRegistrationPutRequestData putData = new PCFPushApiRegistrationPutRequestData();
            putData.setTags(getTags(savedTags, parameters));
            data = putData;
        } else {
            final PCFPushApiRegistrationPostRequestData postData = new PCFPushApiRegistrationPostRequestData();
            postData.setOs("android");
            postData.setTags(parameters.getTags());
            data = postData;
        }

        if (parameters.getDeviceAlias() == null) {
            data.setDeviceAlias("");
        } else {
            data.setDeviceAlias(parameters.getDeviceAlias());
        }
        data.setDeviceModel(Build.MODEL);
        data.setDeviceManufacturer(Build.MANUFACTURER);
        data.setOsVersion(Build.VERSION.RELEASE);
        data.setRegistrationToken(deviceRegistrationId);
        return data;
    }

    private PCFPushApiRegistrationPutRequestData.Tags getTags(Set<String> savedTags,
                                                              PushParameters parameters) {

        final TagsHelper tagsHelper = new TagsHelper(savedTags, parameters.getTags());
        return new PCFPushApiRegistrationPutRequestData.Tags(tagsHelper.getSubscribeTags(), tagsHelper.getUnsubscribeTags());
    }

    @Override
    public PCFPushRegistrationApiRequest copy() {
        return new PCFPushRegistrationApiRequestImpl(context, networkWrapper);
    }
}
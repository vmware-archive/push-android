/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.geofence.GeofenceEngine;

/**
 * Saves preferences to the SharedPreferences on the filesystem.
 */
public class PushPreferencesFCM extends PushPreferences {

    private static final String PROPERTY_FCM_TOKEN_ID = "fcm_token_id";

    public PushPreferencesFCM(Context context) {
        super(context);
    }

    public String getFcmTokenId() {
        return getSharedPreferences().getString(PROPERTY_FCM_TOKEN_ID, null);
    }

    public void setFcmTokenId(String fcmTokenId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_FCM_TOKEN_ID, fcmTokenId);
        editor.commit();
    }
}
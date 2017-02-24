/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import io.pivotal.android.push.service.GeofenceService;
import io.pivotal.android.push.util.Logger;

public class GeofenceBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.setup(context);
        ComponentName comp = new ComponentName(context.getPackageName(), GeofenceService.getGeofenceServiceClass(context).getName());
        // Start the service, keeping the device awake while it is launching.
        WakefulBroadcastReceiver.startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}

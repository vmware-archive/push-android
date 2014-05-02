package com.pivotal.cf.mobile.analyticssdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.analyticssdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.util.Logger;

// Received whenever the phone boots up
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent) {
        if (!Logger.isSetup()) {
            Logger.setup(context);
        }
        Logger.fd("Device boot detected for package '%s'.", context.getPackageName());

        startEventService(context);
    }

    private void startEventService(Context context) {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        final Intent intent = EventService.getIntentToRunJob(context, job);
        context.startService(intent);
    }
}

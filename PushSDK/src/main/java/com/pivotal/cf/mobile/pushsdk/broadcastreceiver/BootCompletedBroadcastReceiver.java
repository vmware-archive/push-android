package com.pivotal.cf.mobile.pushsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.pushsdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.pushsdk.service.EventService;
import com.pivotal.cf.mobile.pushsdk.util.Const;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

// Received whenever the phone boots up
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent) {
        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(context, Const.TAG_NAME);
        }
        PushLibLogger.fd("Device boot detected for package '%s'.", context.getPackageName());

        startEventService(context);
    }

    private void startEventService(Context context) {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        final Intent intent = EventService.getIntentToRunJob(context, job);
        context.startService(intent);
    }
}

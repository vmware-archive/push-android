package io.pivotal.android.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.Set;

import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.TimeProvider;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Logger.setup(context);
        Logger.i("Pivotal CF Push SDK received boot completed message.");
        reregisterGeofences(context);

        if (Pivotal.getAreAnalyticsEnabled(context)) {
            Logger.fd("Device boot detected for package '%s'. Starting AnalyticsEventService.", context.getPackageName());
            startEventService(context);
        } else {
            Logger.fd("Device boot detected for package '%s'. Analytics is disabled.", context.getPackageName());
        }
    }

    private void reregisterGeofences(final Context context) {
        final PushPreferencesProvider preferences = new PushPreferencesProviderImpl(context);
        final boolean areGeofencesEnabled = preferences.areGeofencesEnabled();
        if (areGeofencesEnabled) {
            final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void[] params) {
                    Logger.i("Reregistering current geofences.");
                    final GeofenceRegistrar geofenceRegistrar = new GeofenceRegistrar(context);
                    final FileHelper fileHelper = new FileHelper(context);
                    final TimeProvider timeProvider = new TimeProvider();
                    final GeofencePersistentStore geofencePersistentStore = new GeofencePersistentStore(context, fileHelper);
                    final GeofenceEngine engine = new GeofenceEngine(geofenceRegistrar, geofencePersistentStore, timeProvider, preferences);
                    final Set<String> tags = preferences.getTags();
                    engine.reregisterCurrentLocations(tags);
                    return (Void)null;
                }
            };
            asyncTask.execute();
        }
    }

    private void startEventService(Context context) {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
        context.startService(intent);
    }
}

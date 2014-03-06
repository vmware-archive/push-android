/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.prefs.RealPreferencesProvider;
import org.omnia.pushsdk.util.PushLibLogger;

public class GcmIntentService extends IntentService {

    // TODO - write unit tests to cover this class

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            notifyApplication(intent);
        } else {
            PushLibLogger.i("Received message with no content.");
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void notifyApplication(Intent gcmIntent) {
        final String broadcastName = getBroadcastName();
        if (broadcastName != null) {
            final Intent intent = new Intent();
            intent.setAction(broadcastName);
            intent.putExtra("gcm_intent", gcmIntent);
            sendBroadcast(intent);
        }
    }

    private String getBroadcastName() {
        // TODO - find a way to get the FakePreferencesProvider here in unit tests
        final PreferencesProvider preferencesProvider = new RealPreferencesProvider(this);
        final String packageName = preferencesProvider.loadPackageName();
        if (packageName == null) {
            return null;
        } else {
            final String broadcastName = packageName + ".omniapushsdk.RECEIVE_PUSH";
            return broadcastName;
        }
    }
}

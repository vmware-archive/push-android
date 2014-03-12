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
import android.os.ResultReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.prefs.RealPreferencesProvider;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.concurrent.Semaphore;

public class GcmIntentService extends IntentService {

    public static String KEY_RESULT_RECEIVER = "result_receiver";
//    public static String KEY_RESULT_BUNDLE = "result_bundle";

    public static int NO_RESULT = -1;
    public static int RESULT_EMPTY_INTENT = 100;

    // Use by unit tests
    /* package */ static Semaphore semaphore = null;

    private ResultReceiver resultReceiver = null;


    // TODO - write unit tests to cover this class

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {

            doHandleIntent(intent);

        } finally {

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            if (intent != null) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }

            // If unit tests are running then release them so that they can continue
            if (GcmIntentService.semaphore != null) {
                GcmIntentService.semaphore.release();
            }
        }
    }

    private void doHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }
        resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);

        if (isBundleEmpty(intent)) {
            PushLibLogger.i("Received message with no content.");
            sendResult(RESULT_EMPTY_INTENT);
        } else {
            notifyApplication(intent);
        }
    }

    private boolean isBundleEmpty(Intent intent) {

        final Bundle extras = intent.getExtras();

        if (extras == null || extras.size() <= 0) {
            return true;
        }

        if (extras.keySet().contains(KEY_RESULT_RECEIVER)) {
            // The result receiver extra is only used during unit tests so we don't
            // count it as a populated intent in this case.
            return true;
        }

        return false;
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

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            resultReceiver.send(resultCode, null);
        }
    }
}

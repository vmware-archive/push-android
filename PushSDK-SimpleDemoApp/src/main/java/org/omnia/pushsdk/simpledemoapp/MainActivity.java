package org.omnia.pushsdk.simpledemoapp;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.omnia.pushsdk.PushLib;
import org.omnia.pushsdk.RegistrationParameters;
import org.omnia.pushsdk.registration.RegistrationListener;

public class MainActivity extends ActionBarActivity {

    // Set to your "Project Number" on your Google Cloud project
    private static final String GCM_SENDER_ID = "961895792376";

    // Set to your "Release UUID", as provided by the Omnia console
    private static final String RELEASE_UUID = "8a72b0a3-148d-4b3f-b47b-b3a07dd536ca";

    // Set to your "Release Secret" as provided by the Omnia console
    private static final String RELEASE_SECRET = "d6928667-0192-4c10-b642-744d9671d290";

    // Set to your own defined alias for this device.  May not be null.  May be empty.
    private static final String DEVICE_ALIAS = "test_device_alias";

    private TextView label;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        label = (TextView) findViewById(R.id.label);
        registerForPushNotifications();
    }

    // Note that this demonstration registers for push notification when the onResume method is called
    // to make displaying the results on the UI easier.  In a real application it is more likely that
    // you would register for push messages in your Application object
    private void registerForPushNotifications() {

        // Create the parameters object
        final RegistrationParameters parameters = new RegistrationParameters(GCM_SENDER_ID, RELEASE_UUID, RELEASE_SECRET, DEVICE_ALIAS);
        try {

            // Register for push notifications.  The listener itself is optional (may be null).
            final PushLib pushLib = PushLib.init(this);
            pushLib.startRegistration(parameters, new RegistrationListener() {

                @Override
                public void onRegistrationComplete() {
                    queueLogMessage("Registration successful.");
                }

                @Override
                public void onRegistrationFailed(String reason) {
                    queueLogMessage("Registration failed. Reason is '" + reason + "'.");
                }
            });
        } catch (Exception e) {
            queueLogMessage("Registration failed: " + e.getLocalizedMessage());
        }
    }

    private void queueLogMessage(final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                label.setText(label.getText() + "\n" + message);
            }
        });
    }
}

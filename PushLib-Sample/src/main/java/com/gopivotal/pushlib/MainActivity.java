package com.gopivotal.pushlib;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.gopivotal.pushlib.registration.RegistrationListener;
import com.xtreme.commons.ThreadUtil;

public class MainActivity extends ActionBarActivity {

    private static final String GCM_SENDER_ID = "816486687340";
    private static final String RELEASE_UUID = "efb9783f-a160-4cec-abf1-b51bca14b991";
    private static final String RELEASE_SECRET = "d0bbddc5-f534-4a95-bb49-d90c8e8aec8c";
    private static final String DEVICE_ALIAS = "android_test_device_alias";

    TextView outputText;

    private PushLib pushLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView) findViewById(R.id.outputText);
        outputText.setText("");
        addLogMessage("Starting registration...");

        final PushLibParameters parameters = new PushLibParameters(GCM_SENDER_ID, RELEASE_UUID, RELEASE_SECRET, DEVICE_ALIAS);
        pushLib = PushLib.init(this, parameters);
        pushLib.startRegistration(new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                addLogMessage("Registration successful.");
            }

            @Override
            public void onRegistrationFailed(String reason) {
                addLogMessage("Registration failed. Reason is '" + reason + "'.");
            }
        });
    }

    private void addLogMessage(final String message) {
        if (ThreadUtil.isUIThread()) {
            outputText.setText(outputText.getText() + message + "\n");
        } else {
            ThreadUtil.getUIThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    outputText.setText(outputText.getText() + message + "\n");
                }
            });
        }
    }

}

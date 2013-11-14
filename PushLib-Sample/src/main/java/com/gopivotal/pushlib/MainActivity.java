package com.gopivotal.pushlib;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.xtreme.commons.ThreadUtil;

public class MainActivity extends ActionBarActivity {

    private static final String SENDER_ID = "816486687340";

    TextView outputText;

    private PushLib pushLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView) findViewById(R.id.outputText);
        outputText.setText("");
        addLogMessage("Starting registration...");

        pushLib = PushLib.init(this, SENDER_ID);
        pushLib.startRegistration(new GcmRegistrationListener() {
            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {
                addLogMessage("Registration successful. Registration ID is '" + gcmDeviceRegistrationId + "'.");
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {
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

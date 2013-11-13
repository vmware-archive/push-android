package com.gopivotal.pushlib;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.gopivotal.pushlib.gcm.GcmRegistrarListener;
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
        pushLib.startRegistration(new GcmRegistrarListener() {
            @Override
            public void onRegistrationComplete(String deviceRegistrationId) {
                addLogMessage("Registration successful. Registration ID is '" + deviceRegistrationId + "'.");
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

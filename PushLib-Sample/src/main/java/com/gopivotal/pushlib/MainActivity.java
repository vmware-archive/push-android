package com.gopivotal.pushlib;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private static final String SENDER_ID = "816486687340";

    TextView outputText;

    private PushLib pushLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView) findViewById(R.id.outputText);
        outputText.setText("Ready\n");

        pushLib = PushLib.init(this, SENDER_ID);
    }


}

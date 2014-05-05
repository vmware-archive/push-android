package com.pivotal.cf.mobile.analyticssdk.sample.activity;

import android.os.Bundle;

import com.pivotal.cf.mobile.analyticssdk.AnalyticsSDK;
import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;

public class MainActivity extends BaseMainActivity {

    private AnalyticsSDK analyticsSDK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Test log message");
        }
        analyticsSDK = AnalyticsSDK.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}

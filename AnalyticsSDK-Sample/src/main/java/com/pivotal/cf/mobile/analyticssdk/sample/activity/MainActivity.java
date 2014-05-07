package com.pivotal.cf.mobile.analyticssdk.sample.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pivotal.cf.mobile.analyticssdk.AnalyticsParameters;
import com.pivotal.cf.mobile.analyticssdk.AnalyticsSDK;
import com.pivotal.cf.mobile.analyticssdk.database.DatabaseEventsStorage;
import com.pivotal.cf.mobile.analyticssdk.sample.R;
import com.pivotal.cf.mobile.analyticssdk.sample.util.Settings;
import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;
import com.pivotal.cf.mobile.common.sample.activity.BaseSettingsActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends BaseMainActivity {

    private AnalyticsSDK analyticsSDK;

    protected Class<? extends BaseSettingsActivity> getSettingsActivity() {
        return SettingsActivity.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Log Event\" button to log a test event.");
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentBaseRowColour();
        setupAnalyticsSDK();
    }

    private void setupAnalyticsSDK() {
        try {
            final AnalyticsParameters analyticsParameters = getAnalyticsParameters();
            analyticsSDK = AnalyticsSDK.getInstance(this);
            analyticsSDK.setParameters(analyticsParameters);
        } catch (IllegalArgumentException e) {
            addLogMessage("Not able to initialize Analytics SDK: " + e.getMessage());
        }
    }

    private AnalyticsParameters getAnalyticsParameters() {
        final boolean isAnalyticsEnabled = Settings.isAnalyticsEnabled(this);
        final URL baseServerUrl = getAnalyticsBaseServerUrl();
        final AnalyticsParameters parameters = new AnalyticsParameters(isAnalyticsEnabled, baseServerUrl);
        return parameters;
    }

    private URL getAnalyticsBaseServerUrl() {
        final String baseServerUrl = Settings.getAnalyticsBaseServerUrl(this);
        try {
            return new URL(baseServerUrl);
        } catch (MalformedURLException e) {
            addLogMessage("Invalid analytics base server URL: '" + baseServerUrl + "'.");
            return null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_clear_events:
                clearEvents();
                break;

            case R.id.action_log_event:
                logEvent();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void clearEvents() {
        if (Settings.isAnalyticsEnabled(this)) {
            addLogMessage("Clearing all events.");
            final DatabaseEventsStorage eventsStorage = new DatabaseEventsStorage();
            eventsStorage.reset();
        } else {
            addLogMessage("Cannot clear events if analytics are disabled.");
        }
    }

    private void logEvent() {
        updateCurrentBaseRowColour();
        analyticsSDK.logEvent("user_event");
    }
}

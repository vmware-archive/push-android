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
import com.pivotal.cf.mobile.analyticssdk.sample.util.Preferences;
import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;
import com.pivotal.cf.mobile.common.sample.activity.BasePreferencesActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends BaseMainActivity {

    private AnalyticsSDK analyticsSDK;

    protected Class<? extends BasePreferencesActivity> getPreferencesActivity() {
        return PreferencesActivity.class;
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
        final boolean isAnalyticsEnabled = Preferences.isAnalyticsEnabled(this);
        final URL baseServerUrl = getAnalyticsBaseServerUrl();
        final AnalyticsParameters parameters = new AnalyticsParameters(isAnalyticsEnabled, baseServerUrl);
        return parameters;
    }

    private URL getAnalyticsBaseServerUrl() {
        final String baseServerUrl = Preferences.getAnalyticsBaseServerUrl(this);
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

            case R.id.action_log_error_with_data:
                logEventWithData();
                break;

            case R.id.action_log_error:
                logError();
                break;

            case R.id.action_log_app_foregrounded:
                logAppForegrounded();
                break;

            case R.id.action_log_app_backgrounded:
                logAppBackgrounded();
                break;

            case R.id.action_log_exception:
                logException();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void clearEvents() {
        if (Preferences.isAnalyticsEnabled(this)) {
            addLogMessage("Clearing all events.");
            final DatabaseEventsStorage eventsStorage = new DatabaseEventsStorage();
            eventsStorage.reset();
        } else {
            addLogMessage("Cannot clear events if analytics are disabled.");
        }
    }

    private void logEvent() {
        updateCurrentBaseRowColour();
        analyticsSDK.getEventLogger().logEvent("user_event");
    }

    private void logEventWithData() {
        updateCurrentBaseRowColour();
        final HashMap<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("field", "value");
        analyticsSDK.getEventLogger().logEvent("user_event", eventData);
    }

    private void logError() {
        updateCurrentBaseRowColour();
        analyticsSDK.getEventLogger().logError("TEST_ERROR_ID", "TEST_ERROR_MESSAGE");
    }

    private void logException() {
        updateCurrentBaseRowColour();
        analyticsSDK.getEventLogger().logException("TEST_ERROR_ID", "TEST_ERROR_MESSAGE", new Exception("TEST_EXCEPTION_MESSAGE"));
    }

    private void logAppForegrounded() {
        updateCurrentBaseRowColour();
        analyticsSDK.getEventLogger().logApplicationForegrounded();
    }

    private void logAppBackgrounded() {
        updateCurrentBaseRowColour();
        analyticsSDK.getEventLogger().logApplicationBackgrounded();
    }
}

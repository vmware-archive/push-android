package com.pivotal.cf.mobile.analyticssdk.sample.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pivotal.cf.mobile.analyticssdk.AnalyticsSDK;
import com.pivotal.cf.mobile.analyticssdk.database.DatabaseEventsStorage;
import com.pivotal.cf.mobile.analyticssdk.sample.R;
import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;

public class MainActivity extends BaseMainActivity {

    private AnalyticsSDK analyticsSDK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Log Event\" button to log a test event.");
        }
        analyticsSDK = AnalyticsSDK.init(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        addLogMessage("Clearing all events.");
        final DatabaseEventsStorage eventsStorage = new DatabaseEventsStorage();
        eventsStorage.reset();
    }

    private void logEvent() {
        analyticsSDK.logEvent("user_event");
    }
}

package io.pivotal.android.push.prefs;

import android.test.AndroidTestCase;

import java.util.Properties;

import io.pivotal.android.push.version.Version;

public class PushPreferencesTest extends AndroidTestCase {

    private PushPreferences preferences;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = new PushPreferences(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        preferences.clear();
    }

    public void testAreAnalyticsEnabled1() {
        preferences.setAreAnalyticsEnabled(false);
        assertFalse(preferences.areAnalyticsEnabled());
    }
}

package io.pivotal.android.push.prefs;

import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.pivotal.android.push.version.Version;

public class PushPreferencesProviderImplTest extends AndroidTestCase {

    private PushPreferencesProviderImpl preferences;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = new PushPreferencesProviderImpl(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        preferences.clear();
        Pivotal.setProperties(null);
    }

    public void testDefaultRequestHeadersShouldBeEmpty() {
        final Map<String, String> requestHeaders = preferences.getRequestHeaders();
        assertNotNull(requestHeaders);
        assertTrue(requestHeaders.isEmpty());
    }

    public void testSettingRequestHeadersToNullShouldGiveUsEmptyRequestHeaders() {
        preferences.setRequestHeaders(null);
        final Map<String, String> requestHeaders = preferences.getRequestHeaders();
        assertNotNull(requestHeaders);
        assertTrue(requestHeaders.isEmpty());
    }

    public void testSetRequestHeadersToEmpty() {
        final Map<String, String> requestHeaders = new HashMap<>();
        preferences.setRequestHeaders(requestHeaders);

        final Map<String, String> savedHeaders = preferences.getRequestHeaders();
        assertNotNull(savedHeaders);
        assertTrue(savedHeaders.isEmpty());
    }

    public void testSetRequestHeaders() {
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("SUMMER", "IS THE BEST");
        requestHeaders.put("FALL", "CAN BE PRETTY");
        requestHeaders.put("WINTER", "IS ONLY OKAY");
        requestHeaders.put("SPRING", "GOT THE RAIN");
        preferences.setRequestHeaders(requestHeaders);

        final Map<String, String> savedHeaders = preferences.getRequestHeaders();
        assertNotNull(savedHeaders);
        assertEquals(4, savedHeaders.size());
        assertEquals("IS THE BEST", savedHeaders.get("SUMMER"));
        assertEquals("CAN BE PRETTY", savedHeaders.get("FALL"));
        assertEquals("IS ONLY OKAY", savedHeaders.get("WINTER"));
        assertEquals("GOT THE RAIN", savedHeaders.get("SPRING"));
    }
    
    public void testSetSuccessiveRequestHeaders() {
        final Map<String, String> requestHeaders1 = new HashMap<>();
        requestHeaders1.put("SUMMER", "IS THE BEST");
        preferences.setRequestHeaders(requestHeaders1);

        final Map<String, String> savedHeaders1 = preferences.getRequestHeaders();
        assertNotNull(savedHeaders1);
        assertEquals(1, savedHeaders1.size());
        assertEquals("IS THE BEST", savedHeaders1.get("SUMMER"));

        final Map<String, String> requestHeaders2 = new HashMap<>();
        requestHeaders2.put("WINTER", "IS ONLY OKAY");
        preferences.setRequestHeaders(requestHeaders2);

        final Map<String, String> savedHeaders2 = preferences.getRequestHeaders();
        assertNotNull(savedHeaders2);
        assertEquals(1, savedHeaders2.size());
        assertEquals("IS ONLY OKAY", savedHeaders2.get("WINTER"));
    }

    public void testAreAnalyticsEnabled1() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "false");
        Pivotal.setProperties(properties);

        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled2() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(null);
        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled3() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.0"));
        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled4() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.2"));
        assertTrue(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled5() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.3"));
        assertTrue(preferences.areAnalyticsEnabled());
    }

}

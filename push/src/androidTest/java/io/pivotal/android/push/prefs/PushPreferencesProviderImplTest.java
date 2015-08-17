package io.pivotal.android.push.prefs;

import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

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
}

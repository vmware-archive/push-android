package io.pivotal.android.push.prefs;


import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InMemoryPushRequestHeadersTest {

    private InMemoryPushRequestHeaders pushRequestHeaders;

    @Before
    public void setup() {
        pushRequestHeaders = new InMemoryPushRequestHeaders();
    }

    @After
    public void cleanup() {
        pushRequestHeaders.setRequestHeaders(Collections.<String, String>emptyMap());
    }

    @Test
    public void testDefaultRequestHeadersShouldBeEmpty() {
        final Map<String, String> requestHeaders = pushRequestHeaders.getRequestHeaders();
        assertNotNull(requestHeaders);
        assertTrue(requestHeaders.isEmpty());
    }

    @Test
    public void testSettingRequestHeadersToNullShouldGiveUsEmptyRequestHeaders() {
        pushRequestHeaders.setRequestHeaders(null);
        final Map<String, String> requestHeaders = pushRequestHeaders.getRequestHeaders();
        assertNotNull(requestHeaders);
        assertTrue(requestHeaders.isEmpty());
    }

    @Test
    public void testSetRequestHeadersToEmpty() {
        final Map<String, String> requestHeaders = new HashMap<>();
        pushRequestHeaders.setRequestHeaders(requestHeaders);

        final Map<String, String> savedHeaders = pushRequestHeaders.getRequestHeaders();
        assertNotNull(savedHeaders);
        assertTrue(savedHeaders.isEmpty());
    }

    @Test
    public void testSetRequestHeaders() {
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("SUMMER", "IS THE BEST");
        requestHeaders.put("FALL", "CAN BE PRETTY");
        requestHeaders.put("WINTER", "IS ONLY OKAY");
        requestHeaders.put("SPRING", "GOT THE RAIN");
        pushRequestHeaders.setRequestHeaders(requestHeaders);

        final Map<String, String> savedHeaders = pushRequestHeaders.getRequestHeaders();
        assertNotNull(savedHeaders);
        assertEquals(4, savedHeaders.size());
        assertEquals("IS THE BEST", savedHeaders.get("SUMMER"));
        assertEquals("CAN BE PRETTY", savedHeaders.get("FALL"));
        assertEquals("IS ONLY OKAY", savedHeaders.get("WINTER"));
        assertEquals("GOT THE RAIN", savedHeaders.get("SPRING"));
    }

    @Test
    public void testSetSuccessiveRequestHeaders() {
        final Map<String, String> requestHeaders1 = new HashMap<>();
        requestHeaders1.put("SUMMER", "IS THE BEST");
        pushRequestHeaders.setRequestHeaders(requestHeaders1);

        final Map<String, String> savedHeaders1 = pushRequestHeaders.getRequestHeaders();
        assertNotNull(savedHeaders1);
        assertEquals(1, savedHeaders1.size());
        assertEquals("IS THE BEST", savedHeaders1.get("SUMMER"));

        final Map<String, String> requestHeaders2 = new HashMap<>();
        requestHeaders2.put("WINTER", "IS ONLY OKAY");
        pushRequestHeaders.setRequestHeaders(requestHeaders2);

        final Map<String, String> savedHeaders2 = pushRequestHeaders.getRequestHeaders();
        assertNotNull(savedHeaders2);
        assertEquals(1, savedHeaders2.size());
        assertEquals("IS ONLY OKAY", savedHeaders2.get("WINTER"));
    }
}

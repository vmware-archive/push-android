/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PivotalTest {

    @After
    public void tearDown() throws Exception {
        Pivotal.setProperties(null);
    }

    @Test
    public void testGetSucceeds() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();
        properties.setProperty(key, value);

        Pivotal.setProperties(properties);
        assertEquals(value, Pivotal.getRequiredProperty(InstrumentationRegistry.getContext(), key));
    }

    @Test
    public void testGetFails() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();

        Pivotal.setProperties(properties);

        try {
            assertEquals(value, Pivotal.getRequiredProperty(InstrumentationRegistry.getContext(), key));
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetPlatformUuid() {
        assertEquals("test_platform_uuid", Pivotal.getPlatformUuid(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testGetPlatformSecret() {
        assertEquals("test_platform_secret", Pivotal.getPlatformSecret(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testGetServiceUrl() {
        assertEquals("http://example.com", Pivotal.getServiceUrl(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testGetTrustedCertificateNames() {
        final List<String> names = Pivotal.getPinnedSslCertificateNames(InstrumentationRegistry.getContext());
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("CATS"));
        assertTrue(names.contains("DOGS"));
        assertTrue(names.contains("certificate.der"));
    }

    @Test
    public void testDefaultSslCertValidationMode1() {
        setSslCertValidationModeInProperties(null);
        assertEquals(Pivotal.SslCertValidationMode.DEFAULT, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testDefaultSslCertValidationMode2() {
        setSslCertValidationModeInProperties("default");
        assertEquals(Pivotal.SslCertValidationMode.DEFAULT, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testTrustAllSslCertValidationMode1() {
        setSslCertValidationModeInProperties("trustall");
        assertEquals(Pivotal.SslCertValidationMode.TRUST_ALL, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testTrustAllSslCertValidationMode2() {
        setSslCertValidationModeInProperties("trust_all");
        assertEquals(Pivotal.SslCertValidationMode.TRUST_ALL, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testPinnedSslCertValidationMode() {
        setSslCertValidationModeInProperties("pinned");
        assertEquals(Pivotal.SslCertValidationMode.PINNED, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testCallbackSslCertValidationMode() {
        setSslCertValidationModeInProperties("callback");
        assertEquals(Pivotal.SslCertValidationMode.CALLBACK, Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testInvalidSslCertValidationMode() {
        setSslCertValidationModeInProperties("invalid value chimps");
        try {
            Pivotal.getSslCertValidationMode(InstrumentationRegistry.getContext());
            fail("Should not have succeeded");
        } catch(IllegalArgumentException e) {
            // throw expected
        }
    }

    @Test
    public void testPersistRequestHeadersNotInPropertiesFile() {
        Pivotal.setProperties(null);
        assertTrue(Pivotal.getPersistRequestHeaders(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testPersistRequestHeadersSetToTrue() {
        setPersistRequestHeaders(true);
        assertTrue(Pivotal.getPersistRequestHeaders(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testPersistRequestHeadersSetToFalse() {
        setPersistRequestHeaders(false);
        assertFalse(Pivotal.getPersistRequestHeaders(InstrumentationRegistry.getContext()));
    }

    @Test
    public void testAreAnalyticsEnabled() {
        assertTrue(Pivotal.getAreAnalyticsEnabled(InstrumentationRegistry.getContext()));
    }

    private void setPersistRequestHeaders(boolean persistRequestHeader) {
        final Properties p = new Properties();
        p.put("pivotal.push.persistRequestHeaders", Boolean.toString(persistRequestHeader));
        Pivotal.setProperties(p);
    }

    private void setSslCertValidationModeInProperties(String sslCertValidationMode) {
        final Properties p = new Properties();
        if (sslCertValidationMode != null) {
            p.put("pivotal.push.sslCertValidationMode", sslCertValidationMode);
        }
        Pivotal.setProperties(p);
    }
}
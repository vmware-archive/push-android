/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.test.AndroidTestCase;

import java.util.List;
import java.util.Properties;

public class PivotalTest extends AndroidTestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        Pivotal.setProperties(null);
    }

    public void testGetSucceeds() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();
        properties.setProperty(key, value);

        Pivotal.setProperties(properties);
        assertEquals(value, Pivotal.getRequiredProperty(getContext(), key));
    }

    public void testGetFails() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();

        Pivotal.setProperties(properties);

        try {
            assertEquals(value, Pivotal.getRequiredProperty(getContext(), key));
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e);
        }
    }

    public void testGetPlatformUuid() {
        assertEquals("test_platform_uuid", Pivotal.getPlatformUuid(getContext()));
    }

    public void testGetPlatformSecret() {
        assertEquals("test_platform_secret", Pivotal.getPlatformSecret(getContext()));
    }

    public void testGetGcmSenderId() {
        assertEquals("test_gcm_sender_id", Pivotal.getGcmSenderId(getContext()));
    }

    public void testGetServiceUrl() {
        assertEquals("http://example.com", Pivotal.getServiceUrl(getContext()));
    }

    public void testGetGeofencesEnabled() {
        assertEquals(true, Pivotal.getGeofencesEnabled(getContext()));
    }

    public void testGetTrustedCertificateNames() {
        final List<String> names = Pivotal.getPinnedSslCertificateNames(getContext());
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("CATS"));
        assertTrue(names.contains("DOGS"));
        assertTrue(names.contains("certificate.der"));
    }

    public void testTrustAllSslCertificates() {
        assertTrue(Pivotal.isTrustAllSslCertificates(getContext()));
    }
}
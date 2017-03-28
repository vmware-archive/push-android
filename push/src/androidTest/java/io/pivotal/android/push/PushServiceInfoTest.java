package io.pivotal.android.push;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.pivotal.android.push.prefs.Pivotal;

import static org.junit.Assert.*;

public class PushServiceInfoTest {

    List<String> certificateNames;
    @Before
    public void setup() {
        certificateNames = new ArrayList<String>(){{
            add("cert1");
            add("cert2");
        }};
    }

    @Test
    public void builder_setsObjectValues() throws Exception {
        PushServiceInfo serviceInfo = PushServiceInfo.Builder()
                .setServiceUrl("https://some-serviceurl.com")
                .setPlatformUuid("some-platform-uuid")
                .setPlatformSecret("some-platform-secret")
                .setSSLCertValidationMode(Pivotal.SslCertValidationMode.TRUST_ALL)
                .setPinnedCertificateNames(certificateNames)
                .setAnalyticsEnabled(false)
                .build();

        assertEquals("https://some-serviceurl.com", serviceInfo.getServiceUrl());
        assertEquals("some-platform-uuid", serviceInfo.getPlatformUuid());
        assertEquals("some-platform-secret", serviceInfo.getPlatformSecret());
        assertEquals(Pivotal.SslCertValidationMode.TRUST_ALL, serviceInfo.getSslCertValidationMode());
        assertEquals(certificateNames, serviceInfo.getPinnedSslCertificateNames());
        assertEquals(false, serviceInfo.areAnalyticsEnabled());
    }

    @Test
    public void builder_setsDefaultFields() throws Exception {
        PushServiceInfo serviceInfo = PushServiceInfo.Builder()
                .setServiceUrl("https://some-serviceurl.com")
                .setPlatformUuid("some-platform-uuid")
                .setPlatformSecret("some-platform-secret")
                .build();

        assertEquals(Pivotal.SslCertValidationMode.DEFAULT, serviceInfo.getSslCertValidationMode());
        assertTrue(serviceInfo.getPinnedSslCertificateNames().isEmpty());
        assertEquals(true, serviceInfo.areAnalyticsEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_setsWithoutPlatformUuid() throws Exception {
        PushServiceInfo serviceInfo = PushServiceInfo.Builder()
                .setServiceUrl("https://some-serviceurl.com")
                .setPlatformSecret("some-platform-secret")
                .setSSLCertValidationMode(Pivotal.SslCertValidationMode.TRUST_ALL)
                .setPinnedCertificateNames(certificateNames)
                .setAnalyticsEnabled(false)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_setsWithoutPlatformSecret() throws Exception {
        PushServiceInfo serviceInfo = PushServiceInfo.Builder()
                .setServiceUrl("https://some-serviceurl.com")
                .setPlatformUuid("some-platform-uuid")
                .setSSLCertValidationMode(Pivotal.SslCertValidationMode.TRUST_ALL)
                .setPinnedCertificateNames(certificateNames)
                .setAnalyticsEnabled(false)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_setsWithoutServieUrl() throws Exception {
        PushServiceInfo serviceInfo = PushServiceInfo.Builder()
                .setPlatformUuid("some-platform-uuid")
                .setPlatformSecret("some-platform-secret")
                .setSSLCertValidationMode(Pivotal.SslCertValidationMode.TRUST_ALL)
                .setPinnedCertificateNames(certificateNames)
                .setAnalyticsEnabled(false)
                .build();
    }

}
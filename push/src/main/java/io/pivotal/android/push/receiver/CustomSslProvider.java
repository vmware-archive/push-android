package io.pivotal.android.push.receiver;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public interface CustomSslProvider {
    SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException;
    HostnameVerifier getHostnameVerifier();
}

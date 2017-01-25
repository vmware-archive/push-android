package io.pivotal.android.push.prefs;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryPushRequestHeaders extends PushRequestHeaders {

    private final static Map<String, String> requestHeaders = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public Map<String, String> getRequestHeaders() {
        return new HashMap<>(InMemoryPushRequestHeaders.requestHeaders);
    }

    @Override
    public void setRequestHeaders(@NonNull Map<String, String> requestHeaders) {
        InMemoryPushRequestHeaders.requestHeaders.clear();
        if (requestHeaders != null) {
            InMemoryPushRequestHeaders.requestHeaders.putAll(requestHeaders);
        }
    }
}

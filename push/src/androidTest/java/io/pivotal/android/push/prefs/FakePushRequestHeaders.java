package io.pivotal.android.push.prefs;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Map;

public class FakePushRequestHeaders extends PushRequestHeaders {

    @NonNull
    @Override
    public Map<String, String> getRequestHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public void setRequestHeaders(@NonNull Map<String, String> requestHeaders) {
        // Do nothing
    }
}

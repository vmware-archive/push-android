package io.pivotal.android.push.prefs;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

public abstract class PushRequestHeaders {

    public static PushRequestHeaders getInstance(Context context) {
        PushRequestHeaders requestHeaders;
        if (Pivotal.getPersistRequestHeaders(context)) {
            requestHeaders = new PersistedPushRequestHeaders(context);
        } else {
            requestHeaders = new InMemoryPushRequestHeaders();
        }

        return requestHeaders;
    }

    @NonNull
    public abstract Map<String, String> getRequestHeaders();

    public abstract void setRequestHeaders(@NonNull final Map<String, String> requestHeaders);
}

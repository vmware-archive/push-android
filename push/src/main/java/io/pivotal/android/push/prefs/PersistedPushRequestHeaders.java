package io.pivotal.android.push.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

class PersistedPushRequestHeaders extends PushRequestHeaders {

    private final Context context;
    private static final String REQUEST_HEADERS_TAG_NAME = "PivotalCFMSPushRequestHeaders";

    PersistedPushRequestHeaders(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    public Map<String, String> getRequestHeaders() {
        final Map<String, ?> prefsAll = getSharedPreferencesForRequestHeaders().getAll();
        final HashMap<String, String> result = new HashMap<>(prefsAll.size());
        for (Map.Entry<String, ?> entry : prefsAll.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof String) {
                result.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return result;
    }

    public void setRequestHeaders(@NonNull final Map<String, String> requestHeaders) {
        final SharedPreferences prefs = getSharedPreferencesForRequestHeaders();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    editor.putString(entry.getKey(), entry.getValue());
                }
            }
        }
        editor.commit();
    }

    @NonNull
    private SharedPreferences getSharedPreferencesForRequestHeaders() {
        return context.getSharedPreferences(REQUEST_HEADERS_TAG_NAME, Context.MODE_PRIVATE);
    }
}

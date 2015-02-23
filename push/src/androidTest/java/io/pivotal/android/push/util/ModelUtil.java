package io.pivotal.android.push.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;

public class ModelUtil {

    public static PCFPushGeofenceResponseData getPCFPushGeofenceResponseData(Context context, String filename) throws IOException {
        return getJson(context, filename, new TypeToken<PCFPushGeofenceResponseData>() {});
    }

    public static PCFPushGeofenceDataList getPCFPushGeofenceDataList(Context context, String filename) throws IOException {
        return getJson(context, filename, new TypeToken<PCFPushGeofenceDataList>() {});
    }

    public static <T> T getJson(Context context, String filename, TypeToken<T> typeToken) throws IOException {
        InputStream is = null;
        try {
            is = context.getAssets().open(filename);
            final InputStreamReader reader = new InputStreamReader(is);
            final Gson gson = GsonUtil.getGson();
            return gson.fromJson(reader, typeToken.getType());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}

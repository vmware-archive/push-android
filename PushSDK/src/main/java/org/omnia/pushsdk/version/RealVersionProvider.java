package org.omnia.pushsdk.version;

import android.content.Context;

import org.omnia.pushsdk.util.Util;

public class RealVersionProvider implements VersionProvider {

    private final Context context;

    public RealVersionProvider(Context context) {
        this.context = context;
    }

    @Override
    public int getAppVersion() {
        return Util.getAppVersion(context);
    }
}

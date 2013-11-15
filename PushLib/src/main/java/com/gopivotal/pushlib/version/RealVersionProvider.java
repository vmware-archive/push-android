package com.gopivotal.pushlib.version;

import android.content.Context;

import com.gopivotal.pushlib.util.Util;

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

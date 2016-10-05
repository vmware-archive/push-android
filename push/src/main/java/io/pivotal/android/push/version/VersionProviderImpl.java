/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.version;

import android.content.Context;

import io.pivotal.android.push.util.Util;

public class VersionProviderImpl implements VersionProvider {

    private final Context context;

    public VersionProviderImpl(Context context) {
        this.context = context;
    }

    @Override
    public int getAppVersion() {
        return Util.getAppVersion(context);
    }
}

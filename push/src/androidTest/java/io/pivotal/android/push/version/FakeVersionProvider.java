/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.version;

public class FakeVersionProvider implements VersionProvider {

    private final int appVersion;

    public FakeVersionProvider(int appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public int getAppVersion() {
        return appVersion;
    }
}

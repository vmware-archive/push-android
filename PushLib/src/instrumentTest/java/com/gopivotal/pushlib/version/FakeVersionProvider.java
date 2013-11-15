package com.gopivotal.pushlib.version;

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
